package kr.ac.postech.monet.psmeasureapp_ble;

import android.util.Log;

import no.uib.cipr.matrix.*;

public class MLAT {
    public class GdescentResult
    {
        // DenseMatrix and DenseVector has double elements by default
        public Vector estimator;
        public DenseMatrix estimator_candidate;
        public DenseVector error;

        public GdescentResult(int n, int dim)
        {
            estimator_candidate = new DenseMatrix(n, dim);
            error = new DenseVector(n);
        }
    }

    private static final int n_trial_default = 100;
    private static final double alpha_default = 0.001;
    private static final double time_threshold_default = 0;

    private int n_trial = n_trial_default;
    private double alpha = alpha_default;
    private double time_threshold = time_threshold_default;

    // Euclidian distance: need to verify
    public static double d(Vector p1, Vector p2)
    {
        //return Distance.Euclidean(p1, p2);

        // to prevent the modification of p1

        DenseVector pp1 = new DenseVector(p1);

        return pp1.add(-1, p2).norm(Vector.Norm.Two); // p1.add(-1, p2) = equivalent to p1-p2
    }

    private void initGdescent()
    {
        this.n_trial = n_trial_default;
        this.alpha = alpha_default;
        this.time_threshold = time_threshold_default;
    }
    public void set_n_trial(int n_)
    {
        this.n_trial = n_;
    }
    public void set_alpha(double alpha_)
    {
        this.alpha = alpha_;
    }
    public void set_time_threshold(double time_threshold_)
    {
        this.time_threshold = time_threshold_;
    }

    public GdescentResult gdescent(Matrix anchors_in, Vector ranges_in, Matrix bounds_in_)
    {
        // As Java doesn't have default parameter value, let n_trial, alpha, and time_threshold as class variable
        // If you want to use customized value of n_trial, alpha or time_threshold, use set_n_trial(), set_alpha() or set_time_threshold() instead

        int n = anchors_in.numRows();
        int dim = anchors_in.numColumns();

        GdescentResult gdescent_result = new GdescentResult(this.n_trial, dim);

        DenseMatrix bounds_in;

        if (bounds_in_ == null)
            bounds_in = new DenseMatrix(1, dim);
        else
            bounds_in = new DenseMatrix(bounds_in_);

        DenseMatrix bounds_temp = StackMatrix(anchors_in, bounds_in);

        /*
        Matrix<double> bounds = DenseMatrix.Build.Dense(2, dim);
        for (int i = 0; i < dim; i++)
        {
            bounds[0, i] = bounds_temp.Column(i).Min();
            bounds[1, i] = bounds_temp.Column(i).Max();
        }
        */
        DenseMatrix bounds = new DenseMatrix(2, dim);
        for (int i = 0; i < bounds_temp.numColumns(); i++)
        {
            double columnMin = 0.0;
            double columnMax = 0.0;

            int columnStartIdx = i * bounds_temp.numRows();
            int columnEndIdx = (i + 1) * bounds_temp.numRows() - 1;

            columnMin = bounds_temp.getData()[columnStartIdx];
            columnMax = bounds_temp.getData()[columnStartIdx];

            for (int j = columnStartIdx + 1; j <= columnEndIdx; j++) {
                if (bounds_temp.getData()[j] < columnMin)
                    columnMin = bounds_temp.getData()[j];
                if (bounds_temp.getData()[j] > columnMax)
                    columnMax = bounds_temp.getData()[j];
            }

            bounds.set(0, i, columnMin);
            bounds.set(1, i, columnMax);
        }

        if (this.time_threshold == 0)
            this.time_threshold = 1.0 / this.n_trial;

        DenseVector ranges = new DenseVector(n);

        for (int i = 0; i < this.n_trial; i++)
        {
            DenseVector estimator0 = new DenseVector(dim);
            for (int j = 0; j < dim; j++)
            {
                estimator0.set(j, Math.random() * (bounds.get(1, j) - bounds.get(0, j)) + bounds.get(0, j));
            }

            DenseVector estimator = new DenseVector(estimator0);

            long startTime = System.currentTimeMillis();
            while (true)
            {
                for (int j = 0; j < n; j++)
                {
                    ranges.set(j, d(getRow(anchors_in, j), estimator));
                }
                double error = d(ranges_in, ranges);

                DenseVector delta = new DenseVector(dim);
                for (int j = 0; j<n; j++)
                {
                    // to prevent estimator unintentionally changed
                    DenseVector estimatorTmp = new DenseVector(estimator);

                    delta.add((ranges_in.get(j) - ranges.get(j)) / ranges.get(j), estimatorTmp.add(-1, getRow(anchors_in, j)));
                }
                delta.scale(2 * this.alpha);

                // to prevent estimator unintentionally changed
                DenseVector estimatorTmp = new DenseVector(estimator);

                Vector estimator_next = estimatorTmp.add(-1, delta);
                for (int j = 0; j < n; j++)
                {
                    ranges.set(j, d(getRow(anchors_in, j), estimator_next));
                }
                double error_next = d(ranges_in, ranges);
                if (error_next < error)
                {
                    estimator = new DenseVector(estimator_next);
                }
                else {
                    gdescent_result.estimator_candidate = setRow(gdescent_result.estimator_candidate, i, estimator);
                    gdescent_result.error.set(i, error);
                    break;
                }
                if (System.currentTimeMillis() - startTime > this.time_threshold)
                {
                    gdescent_result.error.set(i, Double.MAX_VALUE);
                    break;
                }
            }
        }

        // Back to default value
        this.initGdescent();

        return gdescent_result;
    }

    public GdescentResult mlat(Matrix anchors_in, Vector ranges_in, Matrix bounds_in_)
    {
        // As Java doesn't have default parameter value, let n_trial, alpha, and time_threshold as class variable
        // If you want to use customized value of n_trial, alpha or time_threshold, use set_n_trial(), set_alpha() or set_time_threshold() instead

        GdescentResult gdescent_result = this.gdescent(anchors_in, ranges_in, bounds_in_);

        int idx = -1;
        double error = Double.MAX_VALUE;
        for (int i = 0; i < gdescent_result.error.size(); i++)
        {
            if (gdescent_result.error.get(i) < error)
            {
                idx = i;
                error = gdescent_result.error.get(i);
            }
        }

        gdescent_result.estimator = getRow(gdescent_result.estimator_candidate, idx);
        return gdescent_result;
    }

    private static DenseMatrix StackMatrix(Matrix top, Matrix bottom)
    {
        int n1 = top.numRows();
        int n2 = bottom.numRows();

        int dim1 = top.numColumns();
        int dim2 = bottom.numColumns();

        if (dim1 != dim2)
            return null;

        double[] tmp = new double[(n1 + n2) * dim1];

        DenseMatrix top_tmp = new DenseMatrix(top);
        double[] tmp1 = top_tmp.getData();
        for (int i=0; i<tmp1.length; i++) {
            tmp[i + i / n1 * n2] = tmp1[i];
        }

        DenseMatrix bottom_tmp = new DenseMatrix(bottom);
        double[] tmp2 = bottom_tmp.getData();
        for (int i=0; i<tmp2.length; i++) {
            tmp[n1 * (i / n2 + 1) + i] = tmp2[i];
        }

        DenseMatrix result = new DenseMatrix(n1 + n2, dim1, tmp, true);

        return result;
    }

    public static Matrix getMatrix(double[][] matrixDataInRowOrder)
    {
        int numRow = matrixDataInRowOrder.length;
        int numColumn = -1;

        boolean equivalent = true;

        if (numRow > 0)
            numColumn = matrixDataInRowOrder[0].length;

        for (int i = 1; i < numRow; i++) {
            if (matrixDataInRowOrder[i].length != numColumn)
            {
                equivalent = false;
                break;
            }
        }

        if (equivalent == false)
            return null;

        double[] columnOrderData = new double[numRow * numColumn];

        int idx = 0;
        for (int i = 0; i < numColumn; i++) {
            for (int j = 0; j < numRow; j++) {
                columnOrderData[idx] = matrixDataInRowOrder[j][i];
                idx++;
            }
        }

        DenseMatrix result = new DenseMatrix(numRow, numColumn, columnOrderData, true);
        return result;
    }

    public static Vector getRow(Matrix matrix, int row)
    {
        if (row < 0 || row >= matrix.numRows())
            return null;

        DenseVector vector = new DenseVector(matrix.numColumns());

        for (int i = 0; i < matrix.numColumns(); i++)
            vector.set(i, matrix.get(row, i));

        return vector;
    }

    public static DenseMatrix setRow(DenseMatrix matrix, int row, DenseVector vector)
    {
        for (int i = 0; i < vector.size(); i++)
            matrix.set(row, i, vector.get(i));

        return matrix;
    }

    public static String printContents(Matrix matrix)
    {
        String contents = "";

        contents += matrix.getClass().getSimpleName() + " " + matrix.numRows() + "x" + matrix.numColumns() + "-Double";
        contents += "\n";

        for (int i = 0; i < matrix.numRows(); i++)
        {
            for (int j = 0; j < matrix.numColumns(); j++)
                contents += String.format("%.6f", matrix.get(i, j)) + " ";

            contents += "\n";
        }

        return contents;
    }

    public static String printContents(Vector vector)
    {
        String contents = "";

        contents += vector.getClass().getSimpleName() + " " + vector.size() + "-Double";
        contents += "\n";

        for (int i = 0; i < vector.size(); i++)
        {
            contents += String.format("%.6f", vector.get(i)) + "\n";
        }

        return contents;
    }

    /*
    public void demo()
    {
        System.Random random = new SystemRandomSource();

        double W = 9, L = 9, H = 3;
        Matrix<double> anchors = DenseMatrix.OfArray(new double[,] {
            {0, 0, H},
            {W, 0, H},
            {W, L, H},
            {0, L, H}
        });


        // Initial Demo
        DenseMatrix anchors_in = new DenseMatrix(getMatrix(anchors_in_matrix));

        int n = anchors_in.numRows();
        int dim = anchors_in.numColumns();

        DenseMatrix bounds_in = new DenseMatrix(1, dim);

        DenseMatrix bounds_temp = StackMatrix(anchors_in, bounds_in);
        DenseMatrix bounds = new DenseMatrix(2, dim);

        for (int i = 0; i < dim; i++)
        {
            double columnMin = 0.0;
            double columnMax = 0.0;

            int columnStartIdx = i * bounds_temp.numRows();
            int columnEndIdx = (i + 1) * bounds_temp.numRows() - 1;

            columnMin = bounds_temp.getData()[columnStartIdx];
            columnMax = bounds_temp.getData()[columnStartIdx];

            for (int j = columnStartIdx; j <= columnEndIdx; j++) {
                if (bounds_temp.getData()[j] < columnMin)
                    columnMin = bounds_temp.getData()[j];
                if (bounds_temp.getData()[j] > columnMax)
                    columnMax = bounds_temp.getData()[j];
            }

            bounds.set(0, i, columnMin);
            bounds.set(1, i, columnMax);
        }
    }
    */
}
