from collections import defaultdict

def shuffle(mapped_data):
    grouped = defaultdict(list)
    for key, value in mapped_data:
        grouped[key].append(value)
    return grouped

def naive_mm(A, B):
    n = len(A)
    m = len(B)
    p = len(B[0])

    C = [[0.0 for _ in range(p)] for _ in range(n)]

    for i in range(n):
        for k in range(m):
            for j in range(p):
                C[i][j] += A[i][k] * B[k][j]

    return C

