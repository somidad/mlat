import numpy as np
from mlat import MLAT

if __name__ == "__main__":
    W = 9
    L = 9
    H = 3
    anchors = np.array([[0, 0, H],
                        [W, 0, H],
                        [W, L, H],
                        [0, L, H]])
    node = np.array([W * np.random.rand(),
                     L * np.random.rand(),
                     H * np.random.rand()])
    ranges = np.empty(anchors.shape[0])
    error = 0.5
    ranges_with_error = np.empty(anchors.shape[0])
    for i in range(anchors.shape[0]):
        ranges[i] = np.linalg.norm(anchors[i, :] - node)
        ranges_with_error[i] = ranges[i] + np.random.uniform(-error, error)

    estimator, result = MLAT.mlat(anchors, ranges_with_error)

    print('Anchors')
    print(anchors)
    print('Node:', node)
    print('Ranges:')
    print('   ', ranges)
    print('Ranges with error:')
    print('   ', ranges_with_error)
    print('Estimator')
    print(estimator)
    print('Full result')
    print(result)
