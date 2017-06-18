# Multilateration on JavaScript

This is a JavaScript script for 2D or 3D multilateration using gradient descent.

## Requirements

- Numeric.js

## Usage

```html
<html>
<head>
<script src="numeric-1.2.6.min.js"></script>
<script src="mlat.js"></script>
<script>
  var [W, L, H] = [9, 9, 3]
  var anchors = [[0, 0, H],
                 [W, 0, H],
                 [W, L, H],
                 [0, L, H]]
  var node = [W * Math.random(), L * Math.random(), H * Math.random()]
  var ranges = empty_vec(anchors.length)
  var error = 0.5
  var ranges_with_error = ranges.slice()
  for (var i = 0; i < anchors.length; i++) {
    ranges[i] = d(anchors[i], node)
    ranges_with_error[i] = ranges[i] + 2 * error * (Math.random() - 0.5)
  }

  gdescent_result = mlat(anchors, ranges_with_error)

  console.log('Anchors')
  console.log(anchors)
  console.log('Node')
  console.log(node)
  console.log('Ranges')
  console.log(ranges)
  console.log('Ranges with error')
  console.log(ranges_with_error)
  console.log('Estimator')
  console.log(gdescent_result.estimator)
  console.log('Full result')
  console.log(gdescent_result.estimator_candidate)
  console.log(gdescent_result.error)
</script>
</head>
</html>
```
