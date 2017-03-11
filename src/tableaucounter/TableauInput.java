package tableaucounter;

import java.io.*;
import java.util.*;

/**
 * Converts a text file into a Young Tableau that can be processed by the
 * computer.
 * @author Benjamin Levandowski
 */
public final class TableauInput {
    
    private TableauInput() {}

    /**
     * Returns a {@code Tableau} object from text input.
     * @return
     * the corresponding tableau
     * @throws FileNotFoundException
     * if the input file is missing or does not have read access
     */
    public static Tableau getTableau() throws FileNotFoundException {
        File tableauIn = new File("Tableau.txt");
        Scanner tableauReader2, lineReader = new Scanner("");
        int[][] weights;
        try (Scanner tableauReader = new Scanner(tableauIn)) {
            int i;
            ArrayList<Integer> lengths = new ArrayList<>();
            while(tableauReader.hasNextLine()) {
                lineReader = new Scanner(tableauReader.nextLine());
                i = 0;
                while(lineReader.hasNextInt()) {
                    lineReader.nextInt();
                    i++;
                }
                lengths.add(i);
            }
            weights = new int[lengths.size()][];
            for(int j = 0; j < weights.length; j++) {
                weights[j] = new int[lengths.get(j)];
            }
            
            tableauReader.close();
            tableauReader2 = new Scanner(tableauIn);
            for(int[] weight : weights) {
                lineReader = new Scanner(tableauReader2.nextLine());
                for(int j = 0; j < weight.length; j++) {
                    weight[j] = lineReader.nextInt();
                }
            }
            lineReader.close();
        }
        return new Tableau(weights);
    }
}
