import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Reducer {

    public static double[][] reduce(List<BlockValue> values, int blockSize) {
        Map<Integer, double[][]> aBlocks = new HashMap<>();
        Map<Integer, double[][]> bBlocks = new HashMap<>();

        for (BlockValue v : values) {
            if (v.tag.equals("A")) {
                aBlocks.put(v.k, v.block);
            } else {
                bBlocks.put(v.k, v.block);
            }
        }

        double[][] result = BlockUtils.zeroBlock(blockSize);

        for (int k : aBlocks.keySet()) {
            if (bBlocks.containsKey(k)) {
                double[][] partial = BlockUtils.multiply(aBlocks.get(k), bBlocks.get(k));
                BlockUtils.add(result, partial);
            }
        }
        return result;
    }
}
