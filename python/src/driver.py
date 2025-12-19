from multiprocessing import Pool, cpu_count
from matrix_generator import generate_matrix
from block_utils import split_into_blocks
from mapper import mapper
from reducer import reducer
from utils import shuffle
import time

def distributed_block_mm(n, m, p, block_size, workers=None):
    A = generate_matrix(n, m)
    B = generate_matrix(m, p)

    A_blocks = split_into_blocks(A, block_size)
    B_blocks = split_into_blocks(B, block_size)

    n_blocks = n // block_size
    m_blocks = m // block_size
    p_blocks = p // block_size

    map_inputs = []

    for (i, k), block in A_blocks.items():
        map_inputs.append((block, "A", i, k, n_blocks, m_blocks, p_blocks))

    for (k, j), block in B_blocks.items():
        map_inputs.append((block, "B", k, j, n_blocks, m_blocks, p_blocks))

    if workers is None:
        workers = cpu_count()

    with Pool(workers) as pool:
        mapped = pool.starmap(mapper, map_inputs)

    mapped_flat = [item for sublist in mapped for item in sublist]

    grouped = shuffle(mapped_flat)

    results = []
    for key, values in grouped.items():
        results.append(reducer(key, values, block_size))

    return results

if __name__ == "__main__":
    n = m = p = 512
    block_size = 64

    start = time.time()
    result = distributed_block_mm(n, m, p, block_size)
    print(f"Computed {len(result)} blocks in {time.time() - start:.2f}s")
