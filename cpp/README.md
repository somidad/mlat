# Multilateration on C++

This is a C++ namespace for 2D or 3D multilateration using gradient descent.

## Requirements

- C++ 11 or later
- Eigen3

```sh
[sudo] apt-get install libeigen3-dev
```

## Usage

```cpp
#include <iostream>
#include <random>

#include <eigen3/Eigen/Dense>

#include "mlat.h"

using namespace std;
using namespace Eigen;
using namespace mlat;

int main() {
  random_device rand;
  default_random_engine rande(rand());
  uniform_real_distribution<> randunif(0, 1);

  double W = 9, L = 9, H = 3;
  MatrixXd anchors(4, 3);
  anchors << 0, 0, H,
             W, 0, H,
             W, L, H,
             0, L, H;
  VectorXd node(3);
  node << W * randunif(rande),
          L * randunif(rande),
          H * randunif(rande);
  VectorXd ranges(anchors.rows());
  double error = 0.5;
  VectorXd ranges_with_error(anchors.rows());
  for (int i = 0; i < anchors.rows(); i++) {
    ranges(i) = (VectorXd(anchors.row(i)) - node).norm();
    ranges_with_error(i) = ranges(i) + 2 * error * (randunif(rande) - 0.5);
  }

  MLAT::GdescentResult gdescent_result = MLAT::mlat(anchors,
                                                    ranges_with_error);

  cout << "Anchors" << endl;
  cout << anchors <<endl;
  cout << "Node" << endl;
  cout << node << endl;
  cout << "Ranges" << endl;
  cout << ranges << endl;
  cout << "Ranges with error" << endl;
  cout << ranges_with_error << endl;
  cout << "Estimator" << endl;
  cout << gdescent_result.estimator << endl;
  cout << "Full result" << endl;
  cout << gdescent_result.estimator_candidate << endl;
  cout << gdescent_result.error << endl;

  return 0;
}
```
