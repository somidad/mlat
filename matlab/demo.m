function [ output_args ] = demo( input_args )
%DEMO Summary of this function goes here
%   Detailed explanation goes here

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

[estimator, result_table] = mlat.do_mlat(anchors, ranges_with_error);

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

end

