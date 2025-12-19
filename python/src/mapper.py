def mapper(block, matrix_id, i, j, n_blocks, m_blocks, p_blocks):
    outputs = []

    if matrix_id == "A":
        for col in range(p_blocks):
            outputs.append(((i, col), ("A", j, block)))
    else:
        for row in range(n_blocks):
            outputs.append(((row, j), ("B", i, block)))

    return outputs
