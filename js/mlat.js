/*
 * NOTE: requires numeric.js
 */

function new_vector(n, val) {
  var vec = []
  for (var i = 0; i < n; i++) {
    vec.push(val)
  }
  return vec
}

function new_matrix(dimen, val) {
  var mat = []
  for (var i = 0; i < dimen[0]; i++) {
    mat.push(new_vector(dimen[1], val))
  }
  return mat
}

function match_dim(p1, p2) {
  var dim1 = numeric.dim(p1)
  var dim2 = numeric.dim(p2)
  if (!numeric.same(dim1, dim2)) {
    throw "Dimension mismatch"
  }
}

function d(p1, p2) {
  match_dim(p1, p2)
  return numeric.norm2(numeric.sub(p1, p2))
}

class GdescentResult {
  constructor(n, dim) {
    this.estimator = new_vector(dim)
    this.estimator_candidate = new_matrix([n, dim])
    this.error = new_vector(n)
  }
}

function gdescent(anchors_in, ranges_in, bounds_in,
         n_trial = 100, alpha = 0.001, time_threshold = 0) {
  if (!anchors_in
      || numeric.dim(anchors_in).length != 2 /* 2D matrix */
      || numeric.dim(anchors_in)[0] < 3 /* 3 or more anchors */) {
    throw "Coordinates of 3 or more anchors (2D matrix) must be provided"
  }
  var [n, dim] = numeric.dim(anchors_in)
  if (!ranges_in
      || numeric.dim(ranges_in).length != 1 //* 1D vector */
      || ranges_in.length != n /* match with anchors */) {
    throw "Range measurements for the corresponding anchors (1D vector) must be provided"
  }
  var gdescent_result = new GdescentResult(n_trial, dim)

  if (!bounds_in) {
    var bounds_temp = new_matrix([numeric.dim(anchors_in)[0], dim])
  } else {
    if (numeric.dim(bounds_in).length != 2
        || numeric.dim(bounds_in)[1] != dim) {
      throw "Bounds matrix must have the same column size with the Anchors"
    }
    var bounds_temp = new_matrix([numeric.add(numeric.dim(anchors_in),
      numeric.dim(bounds_in))[0],
      dim])
  }
  for (var i = 0; i < n; i++) {
    for (var j = 0; j < dim; j++) {
      bounds_temp[i][j] = anchors_in[i][j]
    }
  }
  if (bounds_in) {
    for (var i = 0; i < numeric.dim(bounds_in)[0]; i++) {
      for (var j = 0; j < dim; j++) {
        bounds_temp[n + i][j] = bounds_in[i][j]
      }
    }
  }
  var bounds = new_matrix([2, dim])
  for (var j = 0; j < dim;j++) {
    bounds[0][j] = bounds_temp[0][j]
    bounds[1][j] = bounds_temp[0][j]
  }
  for (var i = 0; i < numeric.dim(bounds_temp)[0]; i++) {
    for (var j = 0; j < dim; j++) {
      if (bounds_temp[i][j] < bounds[0][j]) {
        bounds[0][j] = bounds_temp[i][j]
      } else if (bounds_temp[i][j] > bounds[1][j]) {
        bounds[1][j] = bounds_temp[i][j]
      }
    }
  }
  console.log('Bounds')
  console.log(bounds)

  if (time_threshold== 0) {
    var time_threshold = 1 / n_trial
  }

  var ranges = new_vector(n)
  for (var i = 0; i < n_trial; i++) {
    var estimator0 = new_vector(dim)
    for (var j = 0; j < dim; j++) {
      estimator0[j] = Math.random() * (bounds[1][j] - bounds[0][j]) + bounds[0][j]
    }
    var estimator = estimator0.slice()

    t0 = performance.now()
    while (true) {
      for (var j = 0; j < n; j++) {
        ranges[j] = d(anchors_in[j], estimator)
      }
      var error = d(ranges_in, ranges)

      var delta = new_vector(dim, 0)
      for (var j = 0; j < n; j++) {
        factor = (ranges_in[j] - ranges[j]) / ranges[j]
        vector = numeric.sub(estimator, anchors_in[j])
        for (var k = 0; k < dim; k++) {
          delta[k] += factor * vector[k]
        }
      }
      for (var j = 0; j < dim; j++) {
        delta[j] *= 2 * alpha
      }

      var estimator_next = numeric.sub(estimator, delta)
      for (var j = 0; j < n; j++) {
        ranges[j] = d(anchors_in[j], estimator_next)
      }
      var error_next = d(ranges_in, ranges)
      if (error_next < error) {
        estimator = estimator_next
      } else {
        gdescent_result.estimator_candidate[i] = estimator.slice()
        gdescent_result.error[i] = error
        break
      }
      if (performance.now() - t0 > time_threshold * 1000) {
        gdescent_result.error[i] = Number.MAX_VALUE
        break
      }
    }
  }
  return gdescent_result
}

function mlat(anchors_in, ranges_in, bounds_in = null,
         n_trial = 100, alpha = 0.001, time_threshold = 0) {
  var gdescent_result = gdescent(anchors_in, ranges_in, bounds_in,
                             n_trial, alpha, time_threshold)

  var idx = -1
  var error = Number.MAX_VALUE
  for (var i = 0; i < gdescent_result.error.length; i++) {
    if (gdescent_result.error[i] < error) {
      idx = i
      error = gdescent_result.error[i]
    }
  }
  gdescent_result.estimator = gdescent_result.estimator_candidate[idx]
  return gdescent_result
}
