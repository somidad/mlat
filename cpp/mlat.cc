#include "mlat.h"

#include <chrono>
#include <limits>
#include <random>
#include <eigen3/Eigen/Dense>

namespace mlat {
  MLAT::GdescentResult::GdescentResult(int n, int dim) {
    estimator_candidate = Eigen::ArrayXXd::Zero(n, dim);
    error = Eigen::ArrayXd::Zero(n);
  }

  Eigen::VectorXd getRowAsVector(Eigen::MatrixXd mat, int i) {
    auto mrow = mat.row(i);
    Eigen::VectorXd row(Eigen::Map<Eigen::VectorXd>(
                                                mrow.data(),
                                                mrow.cols() * mrow.rows()));
    return row;
  }

  double
  MLAT::d(Eigen::VectorXd p1, Eigen::VectorXd p2) {
    return (p1 - p2).norm();
  }

  MLAT::GdescentResult
  MLAT::gdescent(Eigen::ArrayXXd anchors_in, Eigen::VectorXd ranges_in,
                 Eigen::ArrayXXd bounds_in,
                 int n_trial, double alpha,
                 double time_threshold) {
    std::random_device rand;
    std::default_random_engine rande(rand());
    std::uniform_real_distribution<> randunif(0, 1);

    int n = anchors_in.rows();
    int dim = anchors_in.cols();
    GdescentResult gdescent_result(n_trial, dim);

    Eigen::ArrayXXd bounds_in_copy;
    if (bounds_in.rows() == 0) {
      bounds_in_copy = Eigen::ArrayXXd::Zero(0, dim);
    } else {
      bounds_in_copy = bounds_in;
    }
    Eigen::ArrayXXd bounds_temp(anchors_in.rows() + bounds_in_copy.rows(),
                                dim);
    bounds_temp << anchors_in, bounds_in_copy;
    Eigen::ArrayXXd bounds(2, dim);
    for (int i = 0; i < dim; i++) {
      bounds(0, i) = bounds_temp.col(i).minCoeff();
      bounds(1, i) = bounds_temp.col(i).maxCoeff();;
    }

    if (time_threshold == 0) {
      time_threshold = 1.0 / n_trial;
    }

    Eigen::VectorXd ranges(n);
    for (int i = 0; i < n_trial; i++) {
      Eigen::VectorXd estimator0(dim);
      for (int j = 0; j < dim; j++) {
        estimator0(j) = randunif(rande) * (bounds(1, j) - bounds(0, j))
                        + bounds(0, j);
      }
      Eigen::VectorXd estimator(estimator0);

      /* timer */
      auto time_start = std::chrono::steady_clock::now();
      while (true) {
        for (int j = 0; j < n; j++) {
          auto row = getRowAsVector(anchors_in, j);
          ranges(j) = d(row, estimator);
        }
        double error = d(ranges_in, ranges);

        Eigen::VectorXd delta = Eigen::VectorXd::Zero(dim);;
        for (int j = 0; j < n; j++) {
          auto row = getRowAsVector(anchors_in, j);
          delta += (ranges_in(j) - ranges(j)) / ranges(j)
                    * (estimator - row);
        }
        delta *= 2 * alpha;

        Eigen::VectorXd estimator_next(estimator - delta);
        for (int j = 0; j < n; j++) {
          auto row = getRowAsVector(anchors_in, j);
          ranges(j) = d(row, estimator_next);
        }
        double error_next = d(ranges_in, ranges);
        if (error_next < error) {
          estimator = estimator_next;
        } else {
          gdescent_result.estimator_candidate.row(i) = estimator;
          gdescent_result.error(i) = error;
          break;
        }
        /* timer */
        auto time_now = std::chrono::steady_clock::now();
        auto time_elapsed = std::chrono::duration_cast
                            <std::chrono::milliseconds>(time_now - time_start);
        if (time_elapsed.count() > time_threshold * 1000) {
          gdescent_result.error(i) = std::numeric_limits<double>::max();
          break;
        }
      }
    }

    return gdescent_result;
  }

  MLAT::GdescentResult
  MLAT::mlat(Eigen::ArrayXXd anchors_in, Eigen::VectorXd ranges_in,
             Eigen::ArrayXXd bounds_in,
             int n_trial, double alpha,
             double time_threshold) {
    MLAT::GdescentResult gdescent_result = gdescent(anchors_in, ranges_in,
                                                    bounds_in, n_trial,
                                                    alpha, time_threshold);

    int idx = -1;
    double error = std::numeric_limits<double>::max();
    for (int i = 0; i < gdescent_result.error.size(); i++) {
      if (gdescent_result.error(i) < error) {
        idx = i;
        error = gdescent_result.error(i);
      }
    }
    gdescent_result.estimator = gdescent_result.estimator_candidate.row(idx);
    return gdescent_result;
  }
}

