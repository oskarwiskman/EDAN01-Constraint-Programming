package Lab1Java;

import org.jacop.constraints.*;
import org.jacop.core.IntVar;
import org.jacop.core.Store;
import org.jacop.search.*;

public class PizzaMaster {
    public static void main(String[] args) {
        long T1, T2, T;

        T1 = System.currentTimeMillis();
        example(3);
        T2 = System.currentTimeMillis();
        T = T2 - T1;
        System.out.println("\n\t*** Execution time = " + T + " ms");
    }

    private static void solve(int n, int[] price, int m, int[] buy, int[] free) {
        Store store = new Store();
        IntVar[] paidPizzas = new IntVar[n];
        IntVar[] freePizzas = new IntVar[n];
        IntVar[][] voucherBought = new IntVar[m][n];
        IntVar[][] voucherFree = new IntVar[m][n];


        //Populate pizza vectors and add constraint
        // saying that a pizza cannot be bought AND free
        // at the same time.
        for(int i = 0; i < n; i++){
            paidPizzas[i] = new IntVar(store, "Paid pizza"+(i+1), 0,1);
            freePizzas[i] = new IntVar(store, "Free pizza"+(i+1), 0,1);
            store.impose(new XneqY(paidPizzas[i], freePizzas[i]));
        }

        // Add constraint saying that you HAVE to buy n pizzas.
        IntVar bought = new IntVar(store, "bought", n, n);
        store.impose(new SumInt(store, mergeVectors(paidPizzas, freePizzas), "==", bought));


        //Populate voucher matrices and add constraint saying
        // that you cannot get a pizza for free if it was used
        // to activate that voucher.
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                voucherBought[i][j] = new IntVar(store, "Paid pizza" + ((i+1) * 10 + j), 0, 1);
                voucherFree[i][j] = new IntVar(store, "Free pizza" + ((i+1)*10+j), 0, 1);
                store.impose(new Not(new XplusYeqC(voucherBought[i][j], voucherFree[i][j], 2)));
            }
        }

        for(int i = 0; i<n; i++){
            //A pizza may not be used to activate two separate vouchers.
            store.impose(new SumInt(store, getColumn(voucherBought, i), "<=", new IntVar(store, 1, 1)));
            store.impose(new SumInt(store, getColumn(voucherFree, i), "<=", new IntVar(store, 1, 1)));
            //Project the voucher matrices on the corresponding vector. Makes sure that if a pizza was
            //used to activate a voucher it HAS to be paid for.
            store.impose(new SumInt(store, getColumn(voucherBought, i), "==", paidPizzas[i]));
            store.impose(new SumInt(store, getColumn(voucherFree, i), "==", freePizzas[i]));
        }

        //The total amount of free pizzas may not exceed the total amount of free pizzas available
        //from all vouchers. (Optimization, not necessary in order to get a solution.)
        store.impose(new SumInt(store, freePizzas, "<=", new IntVar(store, sum(free), sum(free))));

        //You may not receive more free pizzas than the voucher allows AND you need to pay for
        //at least the specified amount of pizzas on a voucher in order to get ANY pizzas for free.
        for(int i =0; i < m; i ++){
            PrimitiveConstraint nbrPaid = new SumInt(store, voucherBought[i],">=", new IntVar(store, buy[i], buy[i]));
            PrimitiveConstraint nbrFree = new SumInt(store, voucherFree[i],"<=", new IntVar(store, free[i], free[i]));
            PrimitiveConstraint zero = new SumInt(store, voucherFree[i], "==", new IntVar(store, 0, 0));
            store.impose(new IfThenElse(nbrPaid, nbrFree, zero));
        }

        //On a specific voucher, you may not get any pizza for free that costs more than
        //any of the ones you paid for on that voucher. Assumes that the prices are sorted
        //in descending order starting with the most expensive.
        for(int i = 0; i < m; i++){
            for(int j = 0; j<n; j++){
                for(int k = j-1; k >= 0; k--){
                    PrimitiveConstraint c1 = new XeqC(voucherBought[i][j], 1);
                    PrimitiveConstraint c2 = new XeqC(voucherFree[i][k], 1);
                    store.impose(new Not(new And(c1, c2)));
                }
            }
        }

        

        IntVar cost = new IntVar(store, "Cost ", 0, sum(price));
        //Sort the price vector, most expensive first.
        bubbleSort(price);
        //Weight the paid pizzas with the cost.
        store.impose(new SumWeight(paidPizzas, price, cost));

        System.out.println(store);
        Search<IntVar> search = new DepthFirstSearch<IntVar>();
        //select on voucher matrices.
        SelectChoicePoint<IntVar> select = new SimpleMatrixSelect<IntVar>(mergeMatrices(voucherBought, voucherFree), null, new IndomainMin<IntVar>());

//        search.setSolutionListener(new PrintOutListener<IntVar>());
//        search.getSolutionListener().searchAll(true);

        boolean result = search.labeling(store, select, cost);

        if (result) {
            System.out.print("Solution with ");
            System.out.print(store.numberConstraints() + " constraints.\n");
            System.out.println("Paid pizzas vector:");
            printVector(paidPizzas);
            System.out.println("Prices:");
            printIntVector(price);
            System.out.println("Voucher bought matrix:");
            printMatrix(voucherBought);
            System.out.println("Voucher free matrix:");
            printMatrix(voucherFree);
        } else {
            System.out.println("No solution found.");
        }

    }

    /**
     * Gets the column of index i.
     * @param matrix IntVar[][], matrix
     * @param i Integer, column index.
     * @return IntVar[], vector containing column i of matrix.
     */

    private static IntVar[] getColumn(IntVar[][] matrix, int i) {
        IntVar[] col = new IntVar[matrix.length];
        for (int j = 0; j < matrix.length; j++) {
            col[j] = matrix[j][i];
        }
        return col;
    }

    /**
     * Merges two matrices by adding the second below the first.
     * Both matrices need to have the same dimensions.
     * @param A IntVar[][], first matrix.
     * @param B IntVar[][], second matrix.
     * @return IntVar[][], A over B.
     */

    private static IntVar[][] mergeMatrices(IntVar[][] A, IntVar[][] B){
        if(A.length==B.length && A[0].length==B[0].length) {
            int rows = A.length + B.length;
            int columns = A[0].length;
            int indexRow = 0;
            IntVar[][] matrix = new IntVar[rows][columns];
            for (int i = 0; i < A.length; i++) {
                for (int e = 0; e < A[0].length; e++) {
                    matrix[indexRow][e] = A[i][e];
                }
                indexRow++;
            }
            for (int i = 0; i < B.length; i++) {
                for (int e = 0; e < B[0].length; e++) {
                    matrix[indexRow][e] = B[i][e];
                }
                indexRow++;
            }
            return matrix;
        }
        throw(new IllegalArgumentException("Matrices not compatible for this type of merge."));
    }

    /**
     * Merges to vectors of IntVar to one single vector.
     * @param v1 IntVar[], first vector.
     * @param v2 IntVar[], second vector
     * @return IntVar[], vector containing all elements in v1 and v2 in the same order.
     */

    private static IntVar[] mergeVectors(IntVar[] v1, IntVar[] v2){
        int size = v1.length+v2.length;
        int index = 0;
        IntVar[] merged = new IntVar[size];
            for(int i = 0; i < v1.length; i++){
                merged[index] = v1[i];
                index++;
            }
            for(int i = 0; i < v2.length; i++){
                merged[index] = v2[i];
                index++;
            }
        return merged;
    }

    /**
     * Sorts the array in descending order. Highest to lowest.
     * @param array int[], vector to be sorted.
     */

    public static void bubbleSort(int[] array) {

        int o = array.length;
        int temp = 0;

        for (int i = 0; i < o; i++) {
            for (int j = 1; j < (o - i); j++) {

                if (array[j - 1] < array[j]) {
                    temp = array[j - 1];
                    array[j - 1] = array[j];
                    array[j] = temp;
                }

            }
        }
    }

    /**
     * Returns the sum of an array.
     * @param array int[], array to be summed.
     * @return Integer, sum of array.
     */

    public static int sum(int[] array){
        int sum = 0;
        for(int i = 0; i < array.length; i++){
            sum+= array[i];
        }
        return sum;
    }

    /**
     * Runs the selected example if it exists.
     * @param ex Integer, example to run.
     */

    public static void example(int ex ){
        switch(ex) {
            case 1:
                int n = 4;
                int[] price = {10, 5, 20, 15};
                int m = 2;
                int[] buy = {1, 2};
                int[] free = {1, 1};
                solve(n, price, m, buy, free);
                break;
            case 2:
                int n2 = 4;
                int[] price2 = {10, 15, 20, 15};
                int m2 = 7;
                int[] buy2 = {1, 2, 2, 8, 3, 1, 4};
                int[] free2 = {1, 1, 2 ,9, 1, 0, 1};
                solve(n2, price2, m2, buy2, free2);
                break;
            case 3:
                int n3 = 10;
                int[] price3 = {70, 10, 60, 60, 30, 100, 60, 40, 60, 20};
                int m3 = 4;
                int[] buy3 =    {1, 2, 1, 1};
                int[] free3 =   {1, 1, 1, 0};
                solve(n3, price3, m3, buy3, free3);
                break;
            default: System.err.println("Example " + ex + " is not implemented.");
        }
    }

    /**
     * Print a matrix of IntVar.
     * @param matrix IntVar[][], matrix to print.
     */

    private static void printMatrix(IntVar[][] matrix) {
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                System.out.print(matrix[i][j].value() + " ");
            }
            System.out.print("\n");
        }
    }

    /**
     * Prints a vector of IntVar.
     * @param vector IntVar[], vector to print.
     */
    public static void printVector(IntVar[] vector){
        for(int i = 0; i < vector.length; i ++){
            System.out.print(vector[i].value()+" ");
        }
        System.out.print("\n");
    }

    /**
     * Print a vector of integers.
     * @param vector int[], vector to print.
     */
    private  static void printIntVector(int[] vector){
        for(int i = 0; i < vector.length; i ++){
            System.out.print(vector[i]+" ");
        }
        System.out.print("\n");
    }
}
