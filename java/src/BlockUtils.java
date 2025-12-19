import java.util.HashMap;
import java.util.Map;

public class BlockUtils {

    public static Map<BlockIndex, double[][]> split(double[][] matrix, int blockSize) {
        Map<BlockIndex, double[][]> blocks = new HashMap<>();
        int n = matrix.length;
        int m = matrix[0].length;

        for (int i = 0; i < n; i += blockSize) {
            for (int j = 0; j < m; j += blockSize) {
                double[][] block = new double[blockSize][blockSize];
                for (int bi = 0; bi < blockSize; bi++) {
                    for (int bj = 0; bj < blockSize; bj++) {
                        block[bi][bj] = matrix[i + bi][j + bj];
                    }
                }
                blocks.put(new BlockIndex(i / blockSize, j / blockSize), block);
            }
        }
        return blocks;
    }

    public static double[][] zeroBlock(int size) {
        return new double[size][size];
    }

    public static double[][] multiply(double[][] A, double[][] B) {
        int size = A.length;
        double[][] C = zeroBlock(size);

        for (int i = 0; i < size; i++) {
            for (int k = 0; k < size; k++) {
                for (int j = 0; j < size; j++) {
                    C[i][j] += A[i][k] * B[k][j];
                }
            }
        }
        return C;
    }

    public static void add(double[][] A, double[][] B) {
        int size = A.length;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                A[i][j] += B[i][j];
            }
        }
    }
}
