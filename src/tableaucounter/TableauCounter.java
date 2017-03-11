package tableaucounter;

import java.io.*;
import java.nio.file.*;

/**
 * Counts the number of set-weighted Young Tableaux of a given shape with the
 * option of storing them in a text file.
 * @author Benjamin Levandowski
 */
public class TableauCounter {

    /**
     * Main class.  Use the "-w" command line argument to save tableaux to a
     * text file.
     * @param args
     * use "-w" to write tableaux to a text file
     * @throws FileNotFoundException
     * if the input tableau file is not present
     * @throws IOException
     * if the output file has write protection
     * @throws InterruptedException
     * if the input tableau doesn't start with a 1 or if it's rectangular, end
     * with a 1
     */
    public static void main(String[] args) throws
            FileNotFoundException, IOException, InterruptedException {
        boolean write = true;
        if(args.length > 0 && "-w".equals(args[0]))
            write = true;
        long total;
        Tableau tableau = TableauInput.getTableau();
        System.out.println(tableau);
        
        if(write)
            total = writeCount(tableau);
        else {
            final int THREAD_COUNT = (tableau.getSortedRules()[0][0][0] - 1) *
            (tableau.getSortedRules()[1][0][0] - 1);
            ParallelCounter[] counters = new ParallelCounter[THREAD_COUNT];
            Thread[] threads = new Thread[THREAD_COUNT];
            System.out.println("There were " + THREAD_COUNT +
                    " threads stared.");
            for(int i = 0; i < THREAD_COUNT; i++) {
                counters[i] = new ParallelCounter(tableau, i, THREAD_COUNT);
                threads[i] = new Thread(counters[i], "#" + i);
                threads[i].start();
            }
            total = 0L;
            for(int i = 0; i < THREAD_COUNT; i++) {
                threads[i].join();
                total += counters[i].getCount();
            }
        }
        
        System.out.println("\nThere are " + total + " tableaux of this shape.");
    }
    
    /**
     * Counts the number of Young Tableaux of a given shape while saving each
     * unique tableau to a text file.
     * @param tableau
     * the Young Tableau shape to be counted
     * @return the number of unique tableaux of that shape
     * @throws IOException
     * if there is an issue with the output file
     */
    public static long writeCount(Tableau tableau) throws IOException {
        String file = "output.txt";
        long total;
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(file))) {
            Counter count = new Counter(tableau);
            total = count.count(writer);
            writer.write("There are " + total +
                    " tableaux of this shape.");
        }
        return total;
    }
}