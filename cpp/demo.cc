#include <chrono>
#include <iostream>
#include <random>

#include <eigen3/Eigen/Dense>

#include "mlat.h"

using namespace std;
using namespace std::chrono;
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
  // TODO: You need to define search space boundary to prevent UNEXPECTED RESULT
  // If not, search space boundary is defined as a cube constrained to
  // minimum and maximum coordinates of x, y, z of anchors
  // If anchors are in the same plane, i.e., all anchors have the same (similar)
  // coordinate of at least one axes, you MUST define search space boundary
  // So, defining search space boundary is all up to you
  ArrayXXd bounds(2, anchors.cols());
  for (int i = 0; i < anchors.cols(); i++) {
    bounds(0, i) = anchors.col(i).minCoeff(); // minimum boundary of ith axis
    bounds(1, i) = anchors.col(i).maxCoeff(); // maximum boundary of ith axis
  }
  // hard coded minimum height (0 m) of search boundary
  bounds(0, anchors.cols() - 1) = 0;

  auto time_start = steady_clock::now();
  MLAT::GdescentResult gdescent_result = MLAT::mlat(anchors,
                                                    ranges_with_error,
                                                    bounds);
  auto time_end = steady_clock::now();
  auto time_elapsed = duration_cast<milliseconds>(time_end - time_start);
  cout << "Finished in " << time_elapsed.count() << " ms" << endl;

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
