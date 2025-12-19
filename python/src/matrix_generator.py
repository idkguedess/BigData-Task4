import random

def generate_matrix(n, m):
    return [[random.random() for _ in range(m)] for _ in range(n)]
