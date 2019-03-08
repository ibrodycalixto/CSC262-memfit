package edu.smith.cs.csc262.memfit;
import java.util.*;

public class Block {
    String name;
    int size;
    int offset;

    public Block(String name, int size, int offset){
        this.name = name;
        this.size = size;
        this.offset = offset;
    }

    public boolean isAdjacent(Block other) {
        if ((this.offset < other.offset) && (this.offset + this.size == other.offset)) {
            return true;
        }
        else if ((other.offset < this.offset) && (other.offset + other.size == this.offset)) {
            return true;
        }
        else {return false;}
    }
}
