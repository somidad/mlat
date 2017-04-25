# Multilateration on C#

This is a C# namespace for 2D or 3D multilateration using gradient descent.

## Requirements

- C# supporting MathNet Numerics

## Usage

```csharp
using mlat;

// Distance
using MathNet.Numerics;
// Matrix<>
using MathNet.Numerics.LinearAlgebra;
// DenseMatrix
using MathNet.Numerics.LinearAlgebra.Double;
using MathNet.Numerics.Random;
using MathNet.Numerics.Random;

class Demo
{
    static void Main(string[] args)
    {
        System.Random random = new SystemRandomSource();

        double W = 9, L = 9, H = 3;
        Matrix<double> anchors = DenseMatrix.OfArray(new double[,] {
            {0, 0, H},
            {W, 0, H},
            {W, L, H},
            {0, L, H}
        });
        Vector<double> node = DenseVector.OfArray(new double[] {
            W * random.NextDouble(),
            L * random.NextDouble(),
            H * random.NextDouble()
        });
        Vector<double> ranges = Vector<double>.Build.Dense(anchors.RowCount);
        double error = 0.5;
        Vector<double> ranges_with_error = DenseVector.OfVector(ranges);
        for (int i = 0; i < anchors.RowCount; i++)
        {
            ranges[i] = Distance.Euclidean(anchors.Row(i), node);
            ranges_with_error[i] = ranges[i] + 2 * error * (random.NextDouble() - 0.5);
        }

        MLAT.GdescentResult gdescent_result = MLAT.mlat(anchors, ranges_with_error);

        Console.WriteLine("Anchors");
        Console.WriteLine(anchors);
        Console.WriteLine("Node");
        Console.WriteLine(node);
        Console.WriteLine("Ranges");
        Console.WriteLine(ranges);
        Console.WriteLine("Ranges with error");
        Console.WriteLine(ranges_with_error);
        Console.WriteLine("Estimator");
        Console.WriteLine(gdescent_result.estimator);
        Console.WriteLine("Full result");
        Console.WriteLine(gdescent_result.estimator_candidate);
        Console.WriteLine(gdescent_result.error);

        // prevent closing
        Console.ReadLine();
    }
}
```
