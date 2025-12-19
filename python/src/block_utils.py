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

def assemble_matrix(blocks, block_size, n, p):
    C = [[0.0 for _ in range(p)] for _ in range(n)]

    for bi, bj, block in blocks:
        for i in range(len(block)):
            for j in range(len(block[0])):
                C[bi * block_size + i][bj * block_size + j] = block[i][j]

    return C
