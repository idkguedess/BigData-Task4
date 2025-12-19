import java.util.Random;

public class MatrixGenerator {

    public static double[][] generate(int n, int m) {
        Random rand = new Random();
        double[][] matrix = new double[n][m];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                matrix[i][j] = rand.nextDouble();
            }
        }
        return matrix;
    }
}
