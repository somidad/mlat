# Multilateration on Python

This is a MATLAB function for 2D or 3D multilateration using gradient descent.

## Usage

```matlab
W = 9;
L = 9;
H = 3;
anchors = [
  0, 0, H;
  W, 0, H;
  W, L, H;
  0, L, H
];
node = [W * rand, L * rand, H * rand];
ranges = nan(1, size(anchors, 1));
err = 0.5;
ranges_with_error = nan(size(ranges));
for i = 1:size(anchors, 1)
  ranges(i) = norm(anchors(i, :) - node);
  ranges_with_error(i) = ranges(i) + 2 * err * (rand - 0.5);
end
% TODO: You need to define search space boundary to prevent UNEXPECTED RESULT
% If not, search space boundary is defined as a cube constrained to
% minimum and maximum coordinates of x, y, z of anchors
% If anchors are in the same plane, i.e., all anchors have the same (similar)
% coordinate of at least one axes, you MUST define search space boundary
% So, defining search space boundary is all up to you
bounds = [];
for i = 1:size(anchors, 1)
  bounds(0, i) = min(anchors(:, i)); % minimum boundary of ith axis
  bounds(1, i) = max(anchors(:, i)); % maximum boundary of ith axis
end
% hard coded minimum height (0 m) of search boundary
bounds(0, end) = 0;

[estimator, result_table] = mlat.do_mlat(anchors, ranges_with_error,
                                         'bounds', bounds);

fprintf('Anchors');
disp(anchors);
fprintf('Node');
disp(node);
fprintf('Ranges');
disp(ranges);
fprintf('Ranges with error');
disp(ranges_with_error);
fprintf('Estimator');
disp(estimator);
fprintf('Full result');
disp(result_table);
```
