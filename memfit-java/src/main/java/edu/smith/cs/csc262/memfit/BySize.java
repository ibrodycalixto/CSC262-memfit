package edu.smith.cs.csc262.memfit;
import java.util.Comparator;

public class BySize implements Comparator<Block> {
    // Sort-by in Java: (needs a class)
        @Override public int compare(Block lhs, Block rhs) {
        return Integer.compare(lhs.size, rhs.size);
    }
}
