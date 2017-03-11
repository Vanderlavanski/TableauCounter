package tableaucounter;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;

/**
 * Takes in a text file describing the Young Tableau shape and its cell weights.
 * Frome here, it is flattened in to a 1D integer array and a list of rules
 * based on the shape.
 * @author Benjamin Levandowski
 */
public final class Tableau {
    private final int[] SHAPE;
    private final int[] WEIGHT;
    private final int[][][] SORTED_RULES;
    private final boolean RECT;
    
    /**
     * Transforms a matrix of weights into a list of rules.
     * @param weights
     * 2D array of tableau weights
     */
    public Tableau(int[][] weights) {
        this.SHAPE = new int[weights.length];
        int length = 0;
        int rules[][];
        for(int i = 0; i < weights.length; i++) {
            SHAPE[i] = weights[i].length;
            length += weights[i].length;
        }
        boolean rectangular = true;
        for(int i = 1; i < SHAPE.length; i++) {
            rectangular = rectangular && SHAPE[i - 1] == SHAPE[i];
        }
        RECT = rectangular;
        this.WEIGHT = new int[length];
        int iterator = 0;
        for(int[] weight : weights) {
            for(int j = 0; j < weight.length; j++) {
                WEIGHT[iterator++] = weight[j];
            }
        }
        if(WEIGHT[0] != 1)
            throw new RejectedExecutionException("First weight must be 1");
        if(RECT && WEIGHT[WEIGHT.length - 1] != 1)
            throw new RejectedExecutionException("Last weight of a rectangular "
                    + "tableau must be 1");
        ArrayList<Integer[]> rulesList = makeRules();
        int minIndex = getListIndex(1, 1);
        int maxIndex;
        if(RECT)
            maxIndex = IntStream.of(WEIGHT).sum() - WEIGHT[WEIGHT.length - 1];
        else
            maxIndex = 99;
        //Assume top left and bottom right are fixed
        for(int i = 0; i < rulesList.size(); i++) {
            if(rulesList.get(i)[0] <= minIndex ||
               rulesList.get(i)[1] <= minIndex ||
               rulesList.get(i)[0] >= maxIndex ||
               rulesList.get(i)[1] >= maxIndex)
                rulesList.remove(i--);
        }
        rulesList.sort((a, b) -> a[0] > b[0] ? 1 : -1);
        rules = new int[rulesList.size()][2];
        for(int i = 0; i < rules.length; i++) {
            rules[i] = new int[] {rulesList.get(i)[0] - 1,
                                  rulesList.get(i)[1] - 1};
        }
        SORTED_RULES = sortRules(rules, weights);
    }
    
    /**
     * Prints a the tableau with a specific set of entries
     * @param entries
     * order of tableau entries to be printed
     * @return
     * the formatted tableau string
     */
    public String print(int[] entries) {
        int h = 0;
        String s = "";
        for(int i = 0; i < SHAPE.length; i++) {
            for(int j = 0; j < SHAPE[i]; j++) {
                for(int k = 0; k < WEIGHT[getCellIndex(i + 1, j + 1)]; k++) {
                    if(i == 0 && j == 0) {
                        s += (1 + " ");
                        break;
                    }
                    if(h < entries.length || !RECT)
                        s += ((entries[h++]) + " ");
                    else
                        s += (entries.length + 2) + " ";
                }
                s += "\t";
            }
            s += "\n";
        }
        return s;
    }
    
    private int getCellIndex(int row, int col) {
        int g = 0;
        for(int i = 0; i < row; i++) {
            for(int j = 0; j < SHAPE[i]; j++) {
                if(i + 1 == row && j + 1 == col)
                    break;
                g++;
            }
        }
        return g;
    }
    
    //Indices start at 1 because that's fun
    private int getListIndex(int row, int col) {
        int g = 0;
        int h = 0;
        for(int i = 0; i < row; i++) {
            for(int j = 0; j < SHAPE[i]; j++) {
                if(i + 1 == row && j == col)
                    break;
                h += WEIGHT[g++];
            }
        }
        return --h;
    }
    
    //Saves list of:  i must be less than j; {{i, j}, ...}
    private ArrayList<Integer[]> makeRules() {
        ArrayList<Integer[]> rulesList = new ArrayList<>();
        //Column rules
        for(int i = 1; i < SHAPE.length; i++) {
            for(int j = 1; j <= SHAPE[i]; j++) {
                rulesList.add(new Integer[] {getListIndex(i, j),
                    getListIndex(i + 1, j) - WEIGHT[getCellIndex(i + 1, j)] +
                    1});
            }
        }
        
        //Row rules
        for(int i = 1; i <= SHAPE.length; i++) {
            for(int j = 1; j < SHAPE[i - 1]; j++) {
                rulesList.add(new Integer[] {getListIndex(i, j),
                    getListIndex(i, j) + 1});
            }
        }
        
        //Cell rules
        for(int i = 1; i <= SHAPE.length; i++) {
            for(int j = 1; j <= SHAPE[i - 1]; j++) {
                for(int k = 1; k < WEIGHT[getCellIndex(i, j)]; k++) {
                    rulesList.add(new Integer[] {getListIndex(i, j) - k,
                        getListIndex(i, j) - k + 1});
                }
            }
        }
        return rulesList;
    }
    
    private int[][][] sortRules(int[][] rules, int[][] weights) {
        int[][][] sortedRules;
        int[] counts = new int[getN()];
        int c;
        for(int i = 0; i < counts.length; i++) {
            c = 1;
            for(int[] rule : rules) {
                if(rule[0] == i || rule[1] == i)
                    c++;
            }
            counts[i] = c;
        }
        sortedRules = new int[getN()][][];
        int[] maxima = flatten(getCellMaxima(weights));
        for(int i = 0; i < sortedRules.length; i++) {
            c = 1;
            sortedRules[i] = new int[counts[i]][];
            sortedRules[i][0] = new int[] {maxima[i]};  //{99} to check
            for(int[] rule : rules) {
                if(rule[0] == i || rule[1] == i)
                    sortedRules[i][c++] = rule;
            }
        }
        return sortedRules;
    }
    
    private int[][] getCellMaxima(int[][] weights) {
        int[][] maxima = new int[SHAPE.length][];//Each digit has a minimum
        maxima[0] = new int[IntStream.of(weights[0]).sum() - 1];
        for(int i = 1; i < maxima.length - 1; i++) {
            maxima[i] = new int[IntStream.of(weights[i]).sum()];
        }
        //This is what I get for adding a rectangular boolean
        if(RECT)
            maxima[maxima.length - 1] =
                    new int[IntStream.of(weights[maxima.length - 1]).sum() -
                    weights[maxima.length - 1][weights[maxima.length - 1].length
                    - 1]];
        else
            maxima[maxima.length - 1] =
                    new int[IntStream.of(weights[maxima.length - 1]).sum()];
        int h;
        int[][] cumulativeSums = cumulativeSums(weights);
        for(int i = 0; i < maxima.length; i++) {  //Go through each row
            h = 0;
            for(int j = 0; j < weights[i].length; j++) {  //One cell at a time
                for(int k = 1; k <= weights[i][j]; k++) {//One for each weight
                    if((RECT && i == maxima.length - 1 && j ==
                            weights[i].length - 1) ||
                            (i == 0 && j == 0))
                        break;
                    maxima[i][h++] = k + cumulativeSums[i][j];
                }
            }
        }
        return maxima;
    }
    
    private int[][] cumulativeSums(int[][] weights) {
        int[][] ints = new int[weights.length][];
        int sum;
        for(int g = 0; g < ints.length; g++) {
            ints[g] = new int[weights[g].length];
            for(int h = 0; h < ints[g].length; h++) {
                sum = 0;
                for(int i = 0; i < weights.length; i++) {
                    for(int j = 0; j < weights[i].length; j++) {
                        if((i >= g && j > h) || (i > g && j >= h))
                            continue;
                        sum += weights[i][j];
                    }
                }
                sum -= weights[g][h];
                ints[g][h] = sum;
            }
        }
        return ints;
    }
    
    private int[] flatten(int[][] array) {
        int[] f;
        int s = 0;
        for(int[] a : array) {
            s += a.length;
        }
        f = new int[s];
        s = 0;
        for(int[] a : array) {
            for(int j = 0; j < a.length; j++) {
                f[s++] = a[j];
            }
        }
        return f;
    }
    
    /**
     * Returns the tableau rules sorted by entry.
     * @return
     * A 3D array with the entry position indexed first, followed by the rules
     * and then by the relevant entries.
     */
    public int[][][] getSortedRules() {return SORTED_RULES;}
    
    /**
     * Returns true if the tableau is rectangular in shape.  This is unaffected
     * by the weights.
     * @return
     * true if and only if the tableau is rectangular in shape
     */
    public boolean getRect() {return RECT;}
    
    /**
     * Returns the number of potentially varying entries in a given tableau.
     * The top left corner must always be 1, and if the tableau is
     * rectangular, the bottom right corner must also always be 1.
     * @return
     * the number of varying entries
     */
    public int getN() {
        if(RECT)
            return IntStream.of(WEIGHT).sum() - 2;
        else
            return IntStream.of(WEIGHT).sum() - 1;
    }//Take off ends if rectangular, take off first otherwise
    
    @Override
    public String toString() {
        int[] numbers = new int[getN()];
        for(int i = 0; i < numbers.length; i++) {
            numbers[i] = i + 2;
        }
        return print(numbers);
    }
}