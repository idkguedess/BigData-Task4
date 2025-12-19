from multiprocessing import Pool, cpu_count
from matrix_generator import generate_matrix
from block_utils import assemble_matrix, split_into_blocks
from mapper import mapper
from reducer import reducer
from utils import naive_mm, shuffle
import time

def distributed_block_mm(n, m, p, block_size, workers=None):
    timings = {}

    start = time.time()
    A = generate_matrix(n, m)
    B = generate_matrix(m, p)
    timings["generation"] = time.time() - start

    start = time.time()
    A_blocks = split_into_blocks(A, block_size)
    B_blocks = split_into_blocks(B, block_size)
    timings["blocking"] = time.time() - start

    map_inputs = []
    n_blocks = n // block_size
    m_blocks = m // block_size
    p_blocks = p // block_size

    for (i, k), block in A_blocks.items():
        map_inputs.append((block, "A", i, k, n_blocks, m_blocks, p_blocks))

    for (k, j), block in B_blocks.items():
        map_inputs.append((block, "B", k, j, n_blocks, m_blocks, p_blocks))

    start = time.time()
    with Pool(workers) as pool:
        mapped = pool.starmap(mapper, map_inputs)
    timings["map"] = time.time() - start

    start = time.time()
    mapped_flat = [item for sublist in mapped for item in sublist]
    grouped = shuffle(mapped_flat)
    timings["shuffle"] = time.time() - start

    start = time.time()
    results = []
    for key, values in grouped.items():
        results.append(reducer(key, values, block_size))
    timings["reduce"] = time.time() - start

    timings["total"] = sum(timings.values())

    return results, timings

def test_correctness(n, block_size):
    A = generate_matrix(n, n)
    B = generate_matrix(n, n)

    distributed_blocks = distributed_block_mm(
        n, n, n, block_size
    )

    C_dist = assemble_matrix(
        distributed_blocks, block_size, n, n
    )
    C_naive = naive_mm(A, B)

    for i in range(n):
        for j in range(n):
            if abs(C_dist[i][j] - C_naive[i][j]) > 1e-6:
                print("Incorrect result")
                return

    print("Correctness verified")

if __name__ == "__main__":
    n = m = p = 512
    block_size = 64

    start = time.time()
    result = distributed_block_mm(n, m, p, block_size)
    print(f"Computed {len(result)} blocks in {time.time() - start:.2f}s")
