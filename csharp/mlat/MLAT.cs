using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

// Distance
using MathNet.Numerics;
// Matrix<>
using MathNet.Numerics.LinearAlgebra;
// DenseMatrix
using MathNet.Numerics.LinearAlgebra.Double;
using MathNet.Numerics.Random;

// StopWatch
using System.Diagnostics;

namespace mlat
{
    class MLAT
    {
        public class GdescentResult
        {
            public Vector<double> estimator { get; set; }
            public Matrix<double> estimator_candidate { get; set; }
            public Vector<double> error { get; set; }

            public GdescentResult(int n, int dim)
            {
                estimator_candidate = DenseMatrix.Build.Dense(n, dim);
                error = DenseVector.Build.Dense(n);
            }
        }

        private static double d(Vector<double> p1, Vector<double> p2)
        {
            return Distance.Euclidean(p1, p2);
        }

        public static GdescentResult gdescent(Matrix<Double> anchors_in, Vector<double> ranges_in,
            Matrix<double> bounds_in = null, int n_trial = 100, double alpha = 0.001, double time_threshold = 0)
        {
            System.Random random = new SystemRandomSource();

            int n = anchors_in.RowCount;
            int dim = anchors_in.ColumnCount;
            GdescentResult gdescent_result = new GdescentResult(n_trial, dim);

            if (bounds_in == null)
            {
                bounds_in = DenseMatrix.Build.Dense(1, dim);
            }
            Matrix<double> bounds_temp = anchors_in.Stack(bounds_in);
            Matrix<double> bounds = DenseMatrix.Build.Dense(2, dim);
            for (int i = 0; i < dim; i++)
            {
                bounds[0, i] = bounds_temp.Column(i).Min();
                bounds[1, i] = bounds_temp.Column(i).Max();
            }

            if (time_threshold == 0)
            {
                time_threshold = 1.0 / n_trial;
            }

            Vector<double> ranges = DenseVector.Build.Dense(n);
            for (int i = 0; i < n_trial; i++)
            {
                Vector<double> estimator0 = DenseVector.Build.Dense(dim);
                for (int j = 0; j < dim; j++)
                {
                    estimator0[j] = random.NextDouble() * (bounds[1, j] - bounds[0, j]) + bounds[0, j];
                }
                Vector<double> estimator = DenseVector.OfVector(estimator0);

                Stopwatch stopwatch = new Stopwatch();
                while (true)
                {
                    for (int j = 0; j < n; j++)
                    {
                        ranges[j] = MLAT.d(anchors_in.Row(j), estimator);
                    }
                    double error = MLAT.d(ranges_in, ranges);

                    Vector<double> delta = DenseVector.Build.Dense(dim);
                    for (int j = 0; j < n; j++)
                    {
                        delta += (ranges_in[j] - ranges[j]) / ranges[j] * (estimator - anchors_in.Row(j));
                    }
                    delta *= 2 * alpha;

                    Vector<double> estimator_next = estimator - delta;
                    for (int j = 0; j < n; j++)
                    {
                        ranges[j] = MLAT.d(anchors_in.Row(j), estimator_next);
                    }
                    double error_next = MLAT.d(ranges_in, ranges);
                    if (error_next < error)
                    {
                        estimator = estimator_next;
                    }
                    else
                    {
                        gdescent_result.estimator_candidate.SetRow(i, estimator);
                        gdescent_result.error[i] = error;
                        break;
                    }
                    if (stopwatch.ElapsedMilliseconds > time_threshold)
                    {
                        gdescent_result.error[i] = double.MaxValue;
                        break;
                    }
                }
            }

            return gdescent_result;
        }

        public static GdescentResult mlat(Matrix<Double> anchors_in, Vector<double> ranges_in,
            Matrix<double> bounds_in = null, int n_trial = 100, double alpha = 0.001, double time_threshold = 0)
        {
            GdescentResult gdescent_result = gdescent(anchors_in, ranges_in, bounds_in, n_trial, alpha, time_threshold);

            int idx = -1;
            double error = double.MaxValue;
            for (int i = 0; i < gdescent_result.error.Count; i++)
            {
                if (gdescent_result.error[i] < error)
                {
                    idx = i;
                    error = gdescent_result.error[i];
                }
            }
            gdescent_result.estimator = gdescent_result.estimator_candidate.Row(idx);
            return gdescent_result;
        }
    }
}
