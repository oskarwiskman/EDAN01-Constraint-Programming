package Lab5Java;

import org.jacop.constraints.*;
import org.jacop.core.IntVar;
import org.jacop.core.Store;
import org.jacop.search.*;

import static Lab1Java.PizzaMaster.printVector;
import static Lab2Java.Logistics.*;

public class UrbanPlanning {

    public static int EXAMPLE_NBR = 3;

    public static void main(String[] args) {
        long T1, T2, T;

        T1 = System.currentTimeMillis();
        example(EXAMPLE_NBR);
        T2 = System.currentTimeMillis();
        T = T2 - T1;
        System.out.println("\n\t*** Execution time = " + T + " ms");

    }

    public static void solve(int n, int n_commercial, int n_residential, int[] point_distribution) {
        Store store = new Store();

        //Model the area with a grid. 1 represents a residential building and 0 represents a commercial building.
        IntVar[][] grid = new IntVar[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                grid[i][j] = new IntVar(store, "Type" + i * 10 + j, 0, 1);
            }
        }

        //Number of residential buildings needs to be correct. Since value is binary, the
        //number of commercial buildings will be implicitly correct.
        IntVar resInGrid = new IntVar(store, "nrResInGrid", n_residential, n_residential);
        store.impose(new SumInt(store, vectorizeIntVar(grid), "==", resInGrid));

        //Create vector to hold the indices in 'point_distribution' corresponding
        //to the number of residential buildings (1's) in each row/column.
        IntVar[] resiSum = new IntVar[n * 2];
        for (int i = 0; i < n; i++) {
            IntVar sumRow = new IntVar(store, 0, n);
            store.impose(new SumInt(store, grid[i], "==", sumRow));
            resiSum[i] = sumRow;
            IntVar sumCol = new IntVar(store, 0, n);
            store.impose(new SumInt(store, getColumn(grid, i), "==", sumCol));
            resiSum[i + n] = sumCol;
        }

        //Set the scores for each row and column.
        //(scores[0] represents score for row 0, scores[n] represents score for column 0 etc.)
        //Shift by -1 to accommodate vectors starting at 0 in java.
        IntVar[] scores = new IntVar[n * 2];
        for (int i = 0; i < n * 2; i++) {
            IntVar score = new IntVar(store, -(n * n * 2), n * n * 2);
            store.impose(new Element(resiSum[i], point_distribution, score, -1));
            scores[i] = score;
        }

        //Add constraints for lexicographical ordering to prevent permutations of the same solution.
        for (int i = 1; i < n - 1; i++) {
            store.impose(new LexOrder(grid[i], grid[i + 1]));
            store.impose(new LexOrder(getColumn(grid, i), getColumn(grid, i + 1)));
        }

        //Optimization by grouping solutions to the upper left corner of the grid.
        //Not usable when grid is too small since not enough solutions exist to provide
        //an optimal one when stacking this optimization with LexOrder.
        if (EXAMPLE_NBR == 3) {
            for (int i = 0; i < n - 1; i++) {
                IntVar sumRow1 = new IntVar(store, 0, n);
                IntVar sumRow2 = new IntVar(store, 0, n);

                store.impose(new SumInt(store, grid[i], "==", sumRow1));
                store.impose(new SumInt(store, grid[i + 1], "==", sumRow2));
                store.impose(new XgteqY(sumRow1, sumRow2));

                IntVar sumCol1 = new IntVar(store, 0, n);
                IntVar sumCol2 = new IntVar(store, 0, n);
                store.impose(new SumInt(store, getColumn(grid, i), "==", sumCol1));
                store.impose(new SumInt(store, getColumn(grid, i + 1), "==", sumCol2));
                store.impose(new XgteqY(sumCol1, sumCol2));
            }
        }
        
        //Max is what we want to find.
        IntVar maxScore = new IntVar(store, -(n * n * 2), n * n * 2);
        store.impose(new SumInt(store, scores, "==", maxScore));

        //Search is more efficient if we minimize, thus we create an opposite of maxScore (minScore)
        //and try to minimize that. This will by implication maximize maxScore.
        IntVar minScore = new IntVar(store, -(n * n * 2), n * n * 2);
        store.impose(new XplusYeqC(maxScore, minScore, 0));

        //Perform the search.
        Search<IntVar> search = new DepthFirstSearch<IntVar>();
        SelectChoicePoint<IntVar> select = new SimpleMatrixSelect<IntVar>(grid, null, new IndomainMin<IntVar>());

//        search.setSolutionListener(new PrintOutListener<IntVar>());
//        search.getSolutionListener().searchAll(true);

        boolean result = search.labeling(store, select, minScore);

        if (result) {
            System.out.println("Solution : ");
            System.out.println("Maximum grid score is: " + maxScore.value());
            printMatrix(grid);
            System.out.println("Scores:");
            printVector(scores);
            System.out.println("ResiSum:");
            printVector(resiSum);
        } else {
            System.out.println("No solution found.");
        }


    }

    public static void example(int ex) {
        switch (ex) {
            case 1:
                int n1 = 5;
                int n1_commercial = 13;
                int n1_residential = 12;
                int[] point_distribution1 = {-5, -4, -3, 3, 4, 5};
                solve(n1, n1_commercial, n1_residential, point_distribution1);
                break;
            case 2:
                int n2 = 5;
                int n_commercial2 = 7;
                int n_residential2 = 18;
                int[] point_distribution2 = {-5, -4, -3, 3, 4, 5};
                solve(n2, n_commercial2, n_residential2, point_distribution2);
                break;
            case 3:
                int n3 = 7;
                int n_commercial3 = 20;
                int n_residential3 = 29;
                int[] point_distribution3 = {-7, -6, -5, -4, 4, 5, 6, 7};
                solve(n3, n_commercial3, n_residential3, point_distribution3);
                break;
            default:
                System.err.println("Example " + ex + " is not implemented.");
        }
    }

}
