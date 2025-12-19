def split_into_blocks(matrix, block_size):
    blocks = {}
    n = len(matrix)
    m = len(matrix[0])

    for i in range(0, n, block_size):
        for j in range(0, m, block_size):
            block = [
                row[j:j + block_size]
                for row in matrix[i:i + block_size]
            ]
            blocks[(i // block_size, j // block_size)] = block
    return blocks

def zero_block(size):
    return [[0.0 for _ in range(size)] for _ in range(size)]

def multiply_blocks(A, B):
    size = len(A)
    C = zero_block(size)
    for i in range(size):
        for k in range(size):
            for j in range(size):
                C[i][j] += A[i][k] * B[k][j]
    return C

def add_blocks(A, B):
    size = len(A)
    for i in range(size):
        for j in range(size):
            A[i][j] += B[i][j]
