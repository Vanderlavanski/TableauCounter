package tableaucounter;

import java.util.*;

/**
 * Counts tableaux of a given shape
 * @author Benjamin Levandowski
 */
public class ParallelCounter implements Runnable {

    private final Tableau TABLEAU;
    private final int[][][] RULES;
    private final int N, R, T;  //Number, range, total thread count
    @SuppressWarnings("FieldMayBeFinal")
    private long count, startTime, stopTime;

    /**
     * Counts tableaux of a given shape in parallel by starting a new thread
     * for each unique pair of numbers for the first two entries.
     * @param tableau
     * the shape of the tableau to count
     * @param range
     * which range of the tree of all possible tableau of this shape to search
     * @param totalThreads
     * the total number of threads started to search for tableaux
     */
    public ParallelCounter(Tableau tableau, int range, int totalThreads) {
        this.TABLEAU = tableau;
        RULES = TABLEAU.getSortedRules();
        N = TABLEAU.getN();
        R = range;
        T = totalThreads;
        count = 0L;
        startTime = System.nanoTime();
    }

    @Override
    public void run() {
        int w = N - 1;  //w, for working index, ranges from 0 to N - 1
        int x = 0, r = 2, c = 2;  //x is ?? r is row c is col
        for(int i = 0; i < R; i++) {
            c++;
            if(c > T / (RULES[0][0][0] - 1) + 1) {
                r++;
                c = 2;
            }
        }
        if(r == c) return;
        int[] entries = new int[N];
        ArrayList<Integer> options = new ArrayList<>(N);
        entries[0] = r;
        entries[1] = c;
        for(int i = 2; i < N; i++) {
            entries[i] = -1;
        }
        if(!valid(entries, 1)) return;
        for(int i = 2; i < N; i++) {
            entries[i] = i + (i >= r ? 1 : 0);
            if(entries[i] >= c)
                entries[i]++;
            if(entries[i] == r)
                entries[i]++;
        }
        if(absValid(entries)) count++;
        else {
            w = 2;
            while(w < N && valid(entries, w)) {
                w++;
            }
        }

        while(entries[0] == r && entries[1] == c && w >= 0) {
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
            if(entries[N - 1] > 0 && valid(entries, w)) count++;
        }
        stopTime = System.nanoTime();
        String output = "After " + ((stopTime - startTime) / 1000000)
                    + "ms, thread " + R + "/" + T + " completed and ";
        if(count != 1)  //Because plural
            System.out.println(output + "found " + count + " tableaux.");
        else
            System.out.println(output + "only found " + count + " tableau.");
    }

    private boolean valid(int[] entries, int w) {
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
    
    private boolean absValid(int[] entries) {
        for(int i = 0; i < RULES.length; i++) {
            if(!valid(entries, i))
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
    
    /**
     * Returns the tableau branch count.
     * @return the number of tableaux this branch counted
     */
    public long getCount() {
        return count;
    }
}