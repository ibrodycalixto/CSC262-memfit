package edu.smith.cs.csc262.memfit;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;


public class Simulation {
  //"pool" size
  int size;
  //algorithm for choosing blocks for alloc
  private String alg;
  //list of all blocks unused by memory
  private List<Block> free_list;
  //list of all blocks used by memory
  private List<Block> used_list;
  //index for next algorithm
  private int last = 0;

  public Simulation() {
    this.free_list = new ArrayList<>();
    this.used_list = new ArrayList<>();
  }

  /**
   * Initialize the free list with block at offset 0 of total pool size and update alg and size variables
   * @param size        total pool size
   * @param algorithm   the fit algorithm used to find blocks for allocation
   */
  public void start(int size, String algorithm) {
    this.size = size;
    this.alg = algorithm;
    free_list.add(new Block("free", size, 0));
  }

  /**
   * Determine allocation algorithm, call findBlock (except in Next Fit case)
   * @param size               size of the block we are trying to allocate
   * @param name               name the allocated block will be given
   */
  private void alloc(String name, int size) {
    switch (this.alg) {
      //allocate the first block big enough to accommodate request
      case "first":
        Collections.sort(this.free_list, new ByOffset());
        break;
      //allocate with size closest to the request size
      case "best":
        Collections.sort(this.free_list, new BySize());
        break;
      //allocate with size furthest from the request size
      case "worst":
        Collections.sort(this.free_list, new BySize());
        Collections.reverse(this.free_list);
        break;
      //allocate a random block of large enough size
      case "rand":
        Collections.shuffle(this.free_list);
        //allocate the next block after last one allocated that is big enough
      case "next":
        for (int i = 0; i < this.free_list.size(); i++) {
          int index = (this.last + i) % this.free_list.size();
          blockSplit(this.free_list.get(index), size, name);
          this.last = (index + 1) % this.free_list.size();
          return;
        }
        break;
    }
    findBlock(size, name);
  }

  /**
   * Find the first block big enough to accommodate request size, call blockSplit on it
   * @param requestSize        size of the block we are trying to allocate
   * @param name               name the block will be given
   */
  private void findBlock(int requestSize, String name) {
    for(Block b : this.free_list) {
      if (b.size >= requestSize){
        blockSplit(b, requestSize, name);
        return;
      }
    }
    System.out.println("Error: Allocation failed for " + name);
  }

  /**
   * If free block given is larger than requested size, segment it and add to used list
   * If not, remove from free and add to used
   * @param b             the block that was  given by search algorithm
   * @param requestSize   the desired size of allocated block
   * @param name          name of the allocated block
   */
  private void blockSplit(Block b, int requestSize, String name) {
    Block used = new Block(name, requestSize, b.offset);
    if (b.size == requestSize) {
      this.free_list.remove(b);
      this.used_list.add(used);
    }
    else {
      b.size = b.size - requestSize;
      b.offset = b.offset + requestSize;
      this.used_list.add(used);
    }
  }

  /**
   * Determines if desired block is on used_list, removes it from used and adds it to free
   * If not found, prints out error message
   * Calls compact to merge free blocks
   * @param name  name of the block we want to free
   */
    private void free (String name){
      boolean found = false;
      for (Block b : this.used_list) {
        if (b.name.equals(name)) {
          found = true;
          this.free_list.add(b);
          this.used_list.remove(b);
        }
      }
      if (!found) {
        System.out.println("Oops - the name " + name + " doesn't exist, cannot be freed!");
      }
      compact();
    }

  /**
   * Determines if two blocks in free_list are adjacent, if so, adds to an accumulative block
   * List of accumulative blocks is assigned to free_list if list is not empty
   */
  private void compact() {
    Collections.sort(this.free_list, new ByOffset());
    List<Block> newList = new ArrayList<>();
    Block accum = new Block ("free", 0, 0);
    for (Block b : this.free_list) {
      if (accum.isAdjacent(b)){
        accum.size += b.size;
      } else {
        if (accum.size > 0) {
          newList.add(accum);
          accum = b;
        }
      }
    }
    if (accum.size > 0) {
      newList.add(accum);
    }
    if (!newList.isEmpty()){
      this.free_list = newList;
    }
  }


  /**
   * Prints the Output of the allocation
   * Free list
   * Used List
   * % Used
   * Failure to allocate prints when allocating
   */
  private void printOutput(){
      Collections.sort(this.free_list, new ByOffset());
      Collections.sort(this.used_list, new ByOffset());
      double freeTotal = 0;
      double usedTotal = 0;
      System.out.println("Free List");
      for (Block b : this.free_list) {
        System.out.println("Name: " + b.name + " Offset: " + b.offset + " Size: " + b.size);
        freeTotal += b.size;
      }
      System.out.println("Used List");
      for (Block b : this.used_list) {
        System.out.println("Name: " + b.name + " Offset: " + b.offset + " Size: " + b.size);
        usedTotal += b.size;
      }
      System.out.println("Percentage of memory free: " + (1/(this.size/freeTotal) * 100));
    System.out.println("Percentage of memory used: " + (1/(this.size/usedTotal) * 100));
    }

  /**
   * Parses command line arguments, creates Simulation argument, calls start, free, alloc, and PrintOutout
   */
  public static void main(String[] args) throws IOException {

    if (args.length < 1) {
      System.out.println("Error: need input file on command line");
      System.exit(1);
    }

    Simulation sim = new Simulation ();
    for (String line : Files.readAllLines(new File(args[0]).toPath())) {
        String[] cols = line.split(" ");
        if(line.trim().isEmpty()) {
          break;
        }
        else if (cols[0].equals("pool")) {
          String algorithm = cols[1];
          int size = Integer.parseInt(cols[2]);
          sim.start(size, algorithm);
        } else if (cols[0].equals("alloc")) {
          String name = cols[1];
          int blockSize = Integer.parseInt(cols[2]);
          sim.alloc(name, blockSize);
          sim.printOutput();
        } else if (cols[0].equals("free")) {
          String name = cols[1];
          sim.free(name);
        } else {
          System.out.println("Invalid file line:" + line);
          System.exit(1);
        }
    }
    sim.printOutput();
  }
}


