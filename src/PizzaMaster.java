import org.jacop.constraints.*;
import org.jacop.core.IntVar;
import org.jacop.core.Store;
import org.jacop.search.*;
import java.util.ArrayList;

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


        //Populera paidPizzaz och freePizzas
        //Samt lägg till constraint för att
        //en pizza kan inte både köpas och fås.
        for(int i = 0; i < n; i++){
            paidPizzas[i] = new IntVar(store, "Paid pizza"+(i+1), 0,1);
            freePizzas[i] = new IntVar(store, "Free pizza"+(i+1), 0,1);
            store.impose(new XneqY(paidPizzas[i], freePizzas[i]));
        }

        //Summan av köpta och gratis pizzor ska vara = n.
        IntVar bought = new IntVar(store, "bought", n, n);
        store.impose(new SumInt(store, mergeVectors(paidPizzas, freePizzas), "==", bought));


        //Populera voucherBought och voucherFree
        //Samt att en pizza kan inte fås av en voucher om den
        //användes för att aktivera vouchern.
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                voucherBought[i][j] = new IntVar(store, "Paid pizza" + ((i+1) * 10 + j), 0, 1);
                voucherFree[i][j] = new IntVar(store, "Free pizza" + ((i+1)*10+j), 0, 1);
                store.impose(new Not(new XplusYeqC(voucherBought[i][j], voucherFree[i][j], 2)));
            }
        }

        //En pizza får inte användas för att "aktivera" två olika vouchers.
        for(int i = 0; i<n; i++){
            store.impose(new SumInt(store, getColumn(voucherBought, i), "<=", new IntVar(store, 1, 1)));
            store.impose(new SumInt(store, getColumn(voucherFree, i), "<=", new IntVar(store, 1, 1)));
            store.impose(new SumInt(store, getColumn(voucherBought, i), "==", paidPizzas[i]));
            store.impose(new SumInt(store, getColumn(voucherFree, i), "==", freePizzas[i]));
        }

        //Antalet gratizpizzor får inte överstiga antalet som vouchern erbjuder.
        for(int i = 0; i < m; i++){
           store.impose(new SumInt(store, voucherFree[i], "<=", new IntVar(store, free[i], free[i])));
        }

        //Totala antalet gratispizzor får inte överstiga summan av de gratispizzor som kan fås av vouchers.
//        store.impose(new SumInt(store, freePizzas, "<=", new IntVar(store, sum(free), sum(free))));

        //Du får inte ta fler gratispizzor än vouchern tillåter samt
        //du får inte ta gratispizzor om du inte betalar för tillräckligt många pizzor.
        for(int i =0; i < m; i ++){
            PrimitiveConstraint nbrPaid = new SumInt(store, voucherBought[i],">=", new IntVar(store, buy[i], buy[i]));
            PrimitiveConstraint nbrFree = new SumInt(store, voucherFree[i],"<=", new IntVar(store, free[i], free[i]));
            PrimitiveConstraint zero = new SumInt(store, voucherFree[i], "==", new IntVar(store, 0, 0));
            store.impose(new IfThenElse(nbrPaid, nbrFree, zero));
        }

        //Pizza som tas gratis får inte vara dyrare än den billigaste som köpts.
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
        bubbleSort(price);
        store.impose(new SumWeight(paidPizzas, price, cost));


        Search<IntVar> search = new DepthFirstSearch<IntVar>();
        SelectChoicePoint<IntVar> select = new SimpleMatrixSelect<IntVar>(voucherBought, null, new IndomainMin<IntVar>());

//        search.setSolutionListener(new PrintOutListener<IntVar>());
//        search.getSolutionListener().searchAll(true);

        boolean result = search.labeling(store, select, cost);

        if (result) {
            System.out.println("Solution : ");
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

    private static IntVar[] getColumn(IntVar[][] matrix, int i) {
        IntVar[] col = new IntVar[matrix.length];
        for (int j = 0; j < matrix.length; j++) {
            col[j] = matrix[j][i];
        }
        return col;
    }

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

    public static int sum(int[] array){
        int sum = 0;
        for(int i = 0; i < array.length; i++){
            sum+= array[i];
        }
        return sum;
    }

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
                int[] buy3 = {1, 2, 1, 1};
                int[] free3 = {1, 1, 1, 0};
                solve(n3, price3, m3, buy3, free3);
                break;
            case 4:
                int n4 = 6;
                int[] price4 = {20,15,10,5,10,10};
                int m4 = 4;
                int[] buy4 = {1, 1, 1, 1};
                int[] free4 = {1, 0, 0, 0};
                solve(n4, price4, m4, buy4, free4);
                break;
        }
    }

    private static void printMatrix(IntVar[][] matrix) {
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                System.out.print(matrix[i][j].value() + " ");
            }
            System.out.print("\n");
        }
    }

    private  static void printVector(IntVar[] vector){
        for(int i = 0; i < vector.length; i ++){
            System.out.print(vector[i].value()+" ");
        }
        System.out.print("\n");
    }

    private  static void printIntVector(int[] vector){
        for(int i = 0; i < vector.length; i ++){
            System.out.print(vector[i]+" ");
        }
        System.out.print("\n");
    }
}
