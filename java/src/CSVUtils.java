import java.io.*;
import java.nio.file.*;

public class CSVUtils {

    public static void writeRow(
            String path,
            String header,
            String row
    ) throws IOException {

        boolean fileExists = Files.exists(Paths.get(path));

        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(path, true))) {

            if (!fileExists) {
                writer.write(header);
                writer.newLine();
            }
            writer.write(row);
            writer.newLine();
        }
    }
}
