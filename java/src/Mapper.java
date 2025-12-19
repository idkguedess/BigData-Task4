import java.util.ArrayList;
import java.util.List;

public class Mapper {

    public static List<MapOutput> map(
            double[][] block,
            String matrixId,
            int i,
            int j,
            int nBlocks,
            int pBlocks
    ) {
        List<MapOutput> outputs = new ArrayList<>();

        if (matrixId.equals("A")) {
            for (int col = 0; col < pBlocks; col++) {
                outputs.add(new MapOutput(
                        new BlockIndex(i, col),
                        new BlockValue("A", j, block)
                ));
            }
        } else {
            for (int row = 0; row < nBlocks; row++) {
                outputs.add(new MapOutput(
                        new BlockIndex(row, j),
                        new BlockValue("B", i, block)
                ));
            }
        }
        return outputs;
    }
}
