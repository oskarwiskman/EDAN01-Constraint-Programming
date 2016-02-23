package Lab2Java;

import org.jacop.constraints.*;
import org.jacop.core.IntVar;
import org.jacop.core.Store;
import org.jacop.search.*;

public class Logistics {
    public static void main(String[] args) {
        long T1, T2, T;
        T1 = System.currentTimeMillis();
        example(3);
        T2 = System.currentTimeMillis();
        T = T2 - T1;
        System.out.println("\n\t*** Execution time = " + T + " ms");
    }

    public static void solve(int graph_size, int start, int n_dests, int[] dest, int n_edges, int[] from, int[] to, int[] cost) {
        Store store = new Store();
        int[][] weights = new int[graph_size][graph_size];
        IntVar[][] paths = new IntVar[graph_size][graph_size];

        for (int i = 0; i < graph_size; i++) {
            for (int j = 0; j < graph_size; j++) {
                paths[i][j] = new IntVar(store, 0, 1);
                weights[i][j] = connected(i, j, from, to, cost);
                if (weights[i][j] == 0) {
                    // Om det inte finns en väg, får den vägen inte väljas.
                    store.impose(new XeqC(paths[i][j], 0));
                }
            }
        }
        // Börja i startnoden.
        store.impose(new SumInt(store, paths[start - 1], ">=", new IntVar(store, 1, 1)));
        for(int i = 0; i < graph_size; i++){
            store.impose(new SumInt(store, getColumn(paths, i), "<=", new IntVar(store, 1, 1)));
        }

        // Du får inte lämna en nod du inte besökt.
        for(int i = 0; i < graph_size; i ++){
            if(i!=start-1) {
                PrimitiveConstraint c1 = new SumInt(store, getColumn(paths, i), ">", new IntVar(store, 0, 0));
                PrimitiveConstraint c2 = new SumInt(store, paths[i], "<=", new IntVar(store, 1, 1));
                PrimitiveConstraint zero = new SumInt(store, paths[i], "==", new IntVar(store, 0, 0));
                store.impose(new IfThenElse(c1, c2, zero));
            }
        }

        // Du måste besöka slutnoderna.
        for (int i = 0; i < n_dests; i++) {
            store.impose(new SumInt(store, getColumn(paths, dest[i]-1), ">=", new IntVar(store, 1, 1)));
        }


        IntVar destCost = new IntVar(store, "Cost", 0, sum(cost));
        store.impose(new SumWeight(vectorizeIntVar(paths), vectorizeInt(weights), destCost));

        Search<IntVar> search = new DepthFirstSearch<IntVar>();
        SelectChoicePoint<IntVar> select = new SimpleMatrixSelect<IntVar>(paths, null, new IndomainMin<IntVar>());

//        search.setSolutionListener(new PrintOutListener<IntVar>());
//        search.getSolutionListener().searchAll(true);

        boolean result = search.labeling(store, select, destCost);

        if (result) {
            System.out.println("Solution : " + java.util.Arrays.asList(destCost));
            printMatrix(paths);
        } else {
            System.out.println("No solution found.");
        }


    }

    public static int connected(int f, int t, int[] from, int[] to, int[] cost) {
        for (int i = 0; i < from.length; i++) {
            if (f + 1 == from[i] && t + 1 == to[i]) {
                return cost[i];
            }
            if (t + 1 == from[i] && f + 1 == to[i]) {
                return cost[i];
            }
        }
        return 0;
    }

    public static int sum(int[] array) {
        int sum = 0;
        for (int i = 0; i < array.length; i++) {
            sum += array[i];
        }
        return sum;
    }

    public static IntVar[] vectorizeIntVar(IntVar[][] matrix) {
        IntVar[] vector = new IntVar[matrix.length * matrix[0].length];
        int index = 0;
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                vector[index] = matrix[i][j];
                index++;
            }
        }
        return vector;
    }

    public static int[] vectorizeInt(int[][] matrix) {
        int[] vector = new int[matrix.length * matrix[0].length];
        int index = 0;
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                vector[index] = matrix[i][j];
                index++;
            }
        }
        return vector;
    }

    public static IntVar[] getColumn(IntVar[][] matrix, int i) {
        IntVar[] col = new IntVar[matrix.length];
        for (int j = 0; j < matrix.length; j++) {
            col[j] = matrix[j][i];
        }
        return col;
    }

    public static void printMatrix(IntVar[][] matrix) {
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                System.out.print(matrix[i][j].value() + " ");
            }
            System.out.print("\n");
        }
    }

    public static void example(int ex) {
        switch (ex) {
            case 1:
                int graph_size1 = 6;
                int start1 = 1;
                int n_dests1 = 1;
                int[] dest1 = {6};
                int n_edges1 = 7;
                int[] from1 = {1, 1, 2, 2, 3, 4, 4};
                int[] to1 = {2, 3, 3, 4, 5, 5, 6};
                int[] cost1 = {4, 2, 5, 10, 3, 4, 11};
                solve(graph_size1, start1, n_dests1, dest1, n_edges1, from1, to1, cost1);
                break;
            case 2:
                int graph_size2 = 6;
                int start2 = 1;
                int n_dests2 = 2;
                int[] dest2 = {5, 6};
                int n_edges2 = 7;
                int[] from2 = {1, 1, 2, 2, 3, 4, 4};
                int[] to2 = {2, 3, 3, 4, 5, 5, 6};
                int[] cost2 = {4, 2, 5, 10, 3, 4, 11};
                solve(graph_size2, start2, n_dests2, dest2, n_edges2, from2, to2, cost2);
                break;
            case 3:
                int graph_size3 = 6;
                int start3 = 1;
                int n_dests3 = 2;
                int[] dest3 = {5, 6};
                int n_edges3 = 9;
                int[] from3 = {1, 1, 1, 2, 2, 3, 3, 3, 4};
                int[] to3 = {2, 3, 4, 3, 5, 4, 5, 6, 6};
                int[] cost3 = {6, 1, 5, 5, 3, 5, 6, 4, 2};
                solve(graph_size3, start3, n_dests3, dest3, n_edges3, from3, to3, cost3);
                break;
        }
    }
}
