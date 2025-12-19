import java.util.Objects;

public class BlockIndex {
    public final int i;
    public final int j;

    public BlockIndex(int i, int j) {
        this.i = i;
        this.j = j;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BlockIndex)) return false;
        BlockIndex other = (BlockIndex) o;
        return i == other.i && j == other.j;
    }

    @Override
    public int hashCode() {
        return Objects.hash(i, j);
    }
}
