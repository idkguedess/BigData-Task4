from multiprocessing import Pool, cpu_count
from matrix_generator import generate_matrix
from block_utils import split_into_blocks
from mapper import mapper
from reducer import reducer
from utils import shuffle, write_csv_row
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

def run_experiment(n, block_size, workers):
    results, timings = distributed_block_mm(
        n, n, n, block_size, workers
    )

    csv_header = [
        "language", "matrix_size", "block_size", "workers",
        "generation", "blocking", "map", "map_shuffle", "reduce", "total"
    ]

    csv_row = [
        "python", n, block_size, workers,
        f"{timings['generation']:.6f}",
        f"{timings['blocking']:.6f}",
        f"{timings['map']:.6f}",
        f"{timings['shuffle']:.6f}",
        f"{timings['reduce']:.6f}",
        f"{timings['total']:.6f}"
    ]

    write_csv_row("benchmarks/results.csv", csv_header, csv_row)
    print(f"Experiment completed. Results written to benchmarks/results.csv")



if __name__ == "__main__":
    for n in [256, 512, 1024]:
        for block_size in [16, 32, 64]:
            run_experiment(
                n=n,
                block_size=block_size,
                workers=cpu_count()
            )