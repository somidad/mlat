classdef mlat
  %MLAT Summary of this class goes here
  %   Detailed explanation goes here
  
  methods (Access = public ,Static)
    function result_table = gdescent(anchors_in, ranges_in, varargin)
      % varargin: 'bounds_in', 'n_trial', 'alpha', 'time_threshold'
      bounds_in = [];
      n_trial = 100;
      alpha = 0.001;
      time_threshold = 1/ n_trial;
      for i = 1:2:length(varargin{1})
        switch (varargin{1}{i})
          case 'bounds'
            bounds_in = varargin{1}{i+1};
          case 'trial'
            n_trial = varargin{1}{i+1};
          case 'alpha'
            alpha = varargin{1}{i+1};
          case 'time'
            time_threshold = varargin{1}{i+1};
        end
      end

      [n, dim] = size(anchors_in);
      bounds_temp = [anchors_in; bounds_in];
      bounds(1, :) = min(bounds_temp);
      bounds(2, :) = max(bounds_temp);
      
      ranges = nan(1, n);
      result_table = nan(n_trial, dim + 1);
      
      for i = 1:n_trial
        estimator0 = nan(1, dim);
        for j = 1:dim
          estimator0(j) = (bounds(2, j) - bounds(1, j)) * rand + bounds(1, j);
        end
        estimator = estimator0;
        
        t0 = tic;
        while true
          for j = 1:n
            ranges(j) = norm(anchors_in(j, :) - estimator);
          end
          err = norm(ranges_in - ranges);
          
          delta = zeros(1, dim);
          for j = 1:n
            delta = delta + (ranges_in(j) - ranges(j)) / ranges(j) * (estimator - anchors_in(j, :));
          end
          delta = 2 * alpha * delta;
          
          estimator_next = estimator - delta;
          for j = 1:n
            ranges(j) = norm(anchors_in(j, :) - estimator_next);
          end
          err_next = norm(ranges_in - ranges);
          if err_next < err
            estimator = estimator_next;
          else
            result_table(i, 1:dim) = estimator;
            result_table(i, end) = err;
            break;
          end
          if toc - t0 > time_threshold
            break;
          end
        end
      end
    end

    function [estimator, result_table] = do_mlat(anchors_in, ranges_in, varargin)
      result_table = mlat.gdescent(anchors_in, ranges_in, varargin);
      [~, I] = min(result_table(:, end));
      estimator = result_table(I, 1:end-1);
    end
  end
  
end

