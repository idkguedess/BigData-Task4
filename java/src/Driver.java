import java.util.*;
import java.util.concurrent.*;

public class Driver {

    public static void main(String[] args) throws Exception {

        int workers = Runtime.getRuntime().availableProcessors();

        int[] sizes = new int[] {256, 512, 1024};
        int[] blockSizes = new int[] {16, 32, 64};

        String csvPath = "benchmarks/results.csv";
        String header = String.join(",",
                "language", "matrix_size", "block_size", "workers",
        "generation", "blocking", "map", "map_shuffle", "reduce", "total"
        );

        System.out.println("=== Java Distributed Matrix Multiplication Benchmarks ===");

        for (int n : sizes) {
            for (int blockSize : blockSizes) {
                Map<String, Double> timings = new LinkedHashMap<>();

                long t0 = Utils.now();
                double[][] A = MatrixGenerator.generate(n, n);
                double[][] B = MatrixGenerator.generate(n, n);
                long t1 = Utils.now();
                timings.put("generation", Utils.elapsedSeconds(t0, t1));

                Map<BlockIndex, double[][]> Ablocks = BlockUtils.split(A, blockSize);
                Map<BlockIndex, double[][]> Bblocks = BlockUtils.split(B, blockSize);
                long t2 = Utils.now();
                timings.put("blocking", Utils.elapsedSeconds(t1, t2));

                int nBlocks = n / blockSize;

                ExecutorService executor = Executors.newFixedThreadPool(workers);
                List<MapOutput> mappedOutputs = Collections.synchronizedList(new ArrayList<>());

                long tMapStart = Utils.now();

                for (Map.Entry<BlockIndex, double[][]> entry : Ablocks.entrySet()) {
                    final BlockIndex key = entry.getKey();
                    final double[][] value = entry.getValue();
                    executor.submit(() -> {
                        List<MapOutput> outputs = Mapper.map(
                                value,
                                "A",
                                key.i,
                                key.j,
                                nBlocks,
                                nBlocks
                        );
                        mappedOutputs.addAll(outputs);
                    });
                }

                for (Map.Entry<BlockIndex, double[][]> entry : Bblocks.entrySet()) {
                    final BlockIndex key = entry.getKey();
                    final double[][] value = entry.getValue();
                    executor.submit(() -> {
                        List<MapOutput> outputs = Mapper.map(
                                value,
                                "B",
                                key.i,
                                key.j,
                                nBlocks,
                                nBlocks
                        );
                        mappedOutputs.addAll(outputs);
                    });
                }

                executor.shutdown();
                executor.awaitTermination(1, TimeUnit.HOURS);
                long tMapEnd = Utils.now();
                timings.put("map", Utils.elapsedSeconds(tMapStart, tMapEnd));

                long tShuffleStart = Utils.now();
                Map<BlockIndex, List<BlockValue>> shuffled = new HashMap<>();
                for (MapOutput o : mappedOutputs) {
                    shuffled
                        .computeIfAbsent(o.key, k -> new ArrayList<>())
                        .add(o.value);
                }
                long tShuffleEnd = Utils.now();
                timings.put("shuffle", Utils.elapsedSeconds(tShuffleStart, tShuffleEnd));

                Map<BlockIndex, double[][]> result = new HashMap<>();

                for (Map.Entry<BlockIndex, List<BlockValue>> entry : shuffled.entrySet()) {
                    result.put(
                            entry.getKey(),
                            Reducer.reduce(entry.getValue(), blockSize)
                    );
                }

                long t4 = Utils.now();
                timings.put("reduce", Utils.elapsedSeconds(tShuffleEnd, t4));
                timings.put("total", Utils.elapsedSeconds(t1, t4));

                String row = String.format(java.util.Locale.US,
                    "java,%d,%d,%d,%.6f,%.6f,%.6f,%.6f,%.6f,%.6f",
                        n,
                        blockSize,
                        workers,
                        timings.get("generation"),
                        timings.get("blocking"),
                        timings.get("map"),
                        timings.get("shuffle"),
                        timings.get("reduce"),
                        timings.get("total")
                );

                CSVUtils.writeRow(csvPath, header, row);
                System.out.println("Matrix size: " + n + "x" + n + ", block " + blockSize + ", workers " + workers);
                timings.forEach((k, v) -> System.out.printf(java.util.Locale.US, "%-15s : %.4f s%n", k, v));
                System.out.println("Benchmark results written to " + csvPath);
                System.out.println();
            }
        }
        System.out.println("Benchmarking complete.");
    }
}
