public class BlockValue {
    public final String tag;
    public final int k;
    public final double[][] block;

    public BlockValue(String tag, int k, double[][] block) {
        this.tag = tag;
        this.k = k;
        this.block = block;
    }
}
