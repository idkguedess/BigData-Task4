public class Utils {

    public static long now() {
        return System.nanoTime();
    }

    public static double elapsedSeconds(long start, long end) {
        return (end - start) / 1_000_000_000.0;
    }
}
