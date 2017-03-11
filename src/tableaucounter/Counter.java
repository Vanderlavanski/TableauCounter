package tableaucounter;

import java.io.*;
import java.util.*;

/**
 * Counts and writes tableaux of a given shape.
 * @author Benjamin Levandowski
 */
public class Counter {

    private final Tableau TABLEAU;
    private final int N;

    /**
     * Creates a counter specific to a given tableau shape.
     * @param tableau
     * the tableau shape to be counted
     */
    public Counter(Tableau tableau) {
        this.TABLEAU = tableau;
        N = TABLEAU.getN();
    }

    /**
     * Tableaux are counted systematically using a depth first search tree
     * that cuts off branches of invalid tableaux.
     * @param writer
     * address of the output file writer
     * @return the number of tableaux of this particular shape
     * @throws IOException
     * if the output file is write protected or there are other issues
     */
    public int count(BufferedWriter writer) throws IOException {
        int w = N - 2;  //w, for working index, ranges from 0 to N - 1
        int x = 0;
        int[] entries = new int[N];
        ArrayList<Integer> options = new ArrayList<>(N);
        int count = 1;
        for(int i = 0; i < N; i++) {
            entries[i] = i + 2;
        }
        writer.write(TABLEAU.print(entries) + "\n");

        while(w >= 0 && entries[0] < N + 2) {
            //1. Clear everything below w
            entries = clear(w + 1, entries, options);
            //2. Sort options
            options.sort(null);
            //3. If we cannot increases e[w], decrease w
            if(options.isEmpty() || options.get(options.size() - 1) <
                    entries[w]) {
                w--;
                continue;
            }
            //4. increase e[w]
            for(int i = 0; i < options.size(); i++) {
                x = i;
                if(entries[w] < options.get(i)) {
                    break;
                }
            }
            if(entries[w] > 0)
                options.add(entries[w]);
            entries[w] = options.remove(x);
            //5. Validate.  Increment entries[w] if possible
            if(!valid(entries, w)) {
                continue;
            }
            //6. Fill the table.  Happens in step 3
            if(w < N - 1) w++;
            //7. Check for valid Tableau
            if(entries[N - 1] > 0 && valid(entries, w)) {
                count++;
                writer.write(TABLEAU.print(entries) + "\n");
            }
        }
        return count;
    }

    private boolean valid(int[] entries, int w) {
        final int[][][] RULES = TABLEAU.getSortedRules();
        if(entries[w] > RULES[w][0][0])
            return false;
        for(int i = 1; i < RULES[w].length; i++) {
            if(entries[RULES[w][i][0]] < 0 || entries[RULES[w][i][1]] < 0)
                continue;
            if(entries[RULES[w][i][0]] > entries[RULES[w][i][1]])
                return false;
        }
        return true;
    }

    private int[] clear(int s, int[] entries, ArrayList<Integer> options) {
        for(int i = s; i < N; i++) {
            if(entries[i] > 0)
                options.add(entries[i]);
            entries[i] = -1;
        }
        return entries;
    }
}