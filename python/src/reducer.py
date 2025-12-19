from block_utils import multiply_blocks, add_blocks, zero_block

def reducer(key, values, block_size):
    a_blocks = {}
    b_blocks = {}

    for tag, k, block in values:
        if tag == "A":
            a_blocks[k] = block
        else:
            b_blocks[k] = block

    result = zero_block(block_size)

    for k in a_blocks:
        if k in b_blocks:
            partial = multiply_blocks(a_blocks[k], b_blocks[k])
            add_blocks(result, partial)

    return key[0], key[1], result
