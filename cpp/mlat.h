#ifndef MLAT_H
#define MLAT_H

#include <vector>
#include <eigen3/Eigen/Dense>

namespace mlat {
  class MLAT {
    public:
      class GdescentResult {
        public:
          Eigen::VectorXd estimator;
          Eigen::ArrayXXd estimator_candidate;
          Eigen::VectorXd error;

          GdescentResult(int n, int dim);
      };

      static GdescentResult gdescent(Eigen::ArrayXXd anchors_in,
                                     Eigen::VectorXd ranges_in,
                                     Eigen::ArrayXXd bounds_in =
                                       Eigen::ArrayXXd::Zero(0, 0),
                                     int n_trial = 100,
                                     double alpha = 0.001,
                                     double time_threshold = 0);
      static GdescentResult mlat(Eigen::ArrayXXd anchors_in,
                                 Eigen::VectorXd ranges_in,
                                 Eigen::ArrayXXd bounds_in =
                                   Eigen::ArrayXXd::Zero(0, 0),
                                 int n_trial = 100,
                                 double alpha = 0.001,
                                 double time_threshold = 0);

    private:
      static double d(Eigen::VectorXd p1, Eigen::VectorXd p2);
  };
}

#endif /* MLAT_H */

