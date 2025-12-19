import java.util.*;
import java.util.concurrent.*;

public class Driver {

    public static void main(String[] args) throws Exception {

        int n = 512;
        int blockSize = 64;
        int workers = Runtime.getRuntime().availableProcessors();

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
        ConcurrentMap<BlockIndex, List<BlockValue>> shuffled =
                new ConcurrentHashMap<>();

        for (var entry : Ablocks.entrySet()) {
            executor.submit(() -> {
                List<MapOutput> outputs = Mapper.map(
                        entry.getValue(),
                        "A",
                        entry.getKey().i,
                        entry.getKey().j,
                        nBlocks,
                        nBlocks
                );
                for (MapOutput o : outputs) {
                    shuffled
                        .computeIfAbsent(o.key, k -> Collections.synchronizedList(new ArrayList<>()))
                        .add(o.value);
                }
            });
        }

        for (var entry : Bblocks.entrySet()) {
            executor.submit(() -> {
                List<MapOutput> outputs = Mapper.map(
                        entry.getValue(),
                        "B",
                        entry.getKey().i,
                        entry.getKey().j,
                        nBlocks,
                        nBlocks
                );
                for (MapOutput o : outputs) {
                    shuffled
                        .computeIfAbsent(o.key, k -> Collections.synchronizedList(new ArrayList<>()))
                        .add(o.value);
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.HOURS);

        long t3 = Utils.now();
        timings.put("map_shuffle", Utils.elapsedSeconds(t2, t3));

        Map<BlockIndex, double[][]> result = new HashMap<>();

        for (var entry : shuffled.entrySet()) {
            result.put(
                entry.getKey(),
                Reducer.reduce(entry.getValue(), blockSize)
            );
        }


        long t4 = Utils.now();
        timings.put("reduce", Utils.elapsedSeconds(t3, t4));

        timings.put("total", Utils.elapsedSeconds(t1, t4));

        System.out.println("=== Java Distributed Matrix Multiplication ===");
        System.out.println("Matrix size: " + n + "x" + n);
        System.out.println("Block size: " + blockSize);
        System.out.println("Workers: " + workers);
        System.out.println("Blocks computed: " + result.size());
        System.out.println();

        timings.forEach((k, v) ->
                System.out.printf("%-15s : %.4f s%n", k, v)
        );
    }
}
