import org.jacop.constraints.*;
import org.jacop.core.IntVar;
import org.jacop.core.Store;
import org.jacop.search.*;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class PizzaMaster {
    public static void main(String[] args) {
        long T1, T2, T;
        int n = 4;
        int[] price = {10, 5, 20, 15};
        int m = 2;
        int[] buy = {1, 2};
        int[] free = {1, 1};

        T1 = System.currentTimeMillis();
        letsDeal(n, price, m, buy, free);
        T2 = System.currentTimeMillis();
        T = T2 - T1;
        System.out.println("\n\t*** Execution time = " + T + " ms");
    }

    private static void letsDeal(int n, int[] price, int m, int[] buy, int[] free) {
        Store store = new Store();
        IntVar[][] pPaid = new IntVar[n][m];
        IntVar[][] pFree = new IntVar[n][m];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                pPaid[i][j] = new IntVar(store, "p" + (i * 10 + j), 0, 1);
                pFree[i][j] = new IntVar(store, "f" + (i * 10 + j), 0, 1);
            }
        }

        ArrayList<IntVar[]> pPaidRows = getRows(pPaid);
        ArrayList<IntVar[]> pFreeRows = getRows(pFree);


        //Man kan inte köpa (eller få gratis) samma pizza två gånger.
        //Matematiskt, summan av varje rad i matriserna <= 1;
        for (int i = 0; i < n; i++) {
            store.impose(new SumInt(store,pPaidRows.get(i),"<=", new IntVar(store,1,1)));
            store.impose(new SumInt(store,pFreeRows.get(i),"<=", new IntVar(store,1,1)));
        }

        //Summan av matriserna = n
        IntVar bought = new IntVar(store, "bought", n, n);
        store.impose(new SumInt(store, listyfy(pPaid, pFree), "==", bought));


        //Summan av column pPaid[i] = buy i


        //Summan av column pFree[i] <= free i
        //Dvs. du får inte ta fler gratispizzor än kupongen tillåter.
        ArrayList<IntVar[]> pPaidCols = getColumns(pPaid);
        ArrayList<IntVar[]> pFreeCols = getColumns(pFree);
        for(int i = 0; i < m; i++){
            store.impose(new SumInt(store, pFreeCols.get(i), "<=", new IntVar(store, free[i], free[i])));
            PrimitiveConstraint vouchPaid = new SumInt(store, pPaidCols.get(i), "==", new IntVar(store, buy[i], buy[i]));
            PrimitiveConstraint vouchFree = new SumInt(store, pFreeCols.get(i), "==", new IntVar(store, free[i], free[i]));
            store.impose(new IfThen(vouchFree, vouchPaid));
        }

        //Pizza som tas gratis får inte vara dyrare än den billigaste som köpts


        Search<IntVar> search = new DepthFirstSearch<IntVar>();
        SelectChoicePoint<IntVar> select = new SimpleSelect<IntVar>(pPaid, null, new IndomainMin<IntVar>());

        search.setSolutionListener(new PrintOutListener<IntVar>());
        search.getSolutionListener().searchAll(true);

        boolean result = search.labeling(store, select);

        if (result) {
            System.out.println("Solution : "+ java.util.Arrays.asList(pPaid));
        } else {
            System.out.println("No solution found.");
        }

    }

    private static ArrayList<IntVar[]> getRows(IntVar[][] matrix) {
        ArrayList<IntVar[]> rows = new ArrayList<>();
        IntVar[] row = new IntVar[matrix[0].length];

        for (int j = 0; j < matrix.length; j++) {
            for (int i = 0; i < matrix[0].length; i++) {
                row[i] = matrix[i][j];
            }
            rows.add(row);
        }
        return rows;
    }

    private static ArrayList<IntVar[]> getColumns(IntVar[][] matrix){
        ArrayList<IntVar[]> columns = new ArrayList<>();

        for(int i = 0; i < matrix.length; i++){
            columns.add(matrix[i]);
        }

        return columns;
    }

    private static IntVar[] listyfy(IntVar[][] A, IntVar[][] B){
        int size = 2*A.length*2*B.length;
        int index = 0;
        IntVar[] list = new IntVar[size];
        ArrayList<IntVar[]> Arows = getRows(A);
        ArrayList<IntVar[]> Brows = getRows(B);
        for(IntVar[] iv : Arows){
            for(int i = 0; i < iv.length; i ++){
                list[index] = iv[i];
                index++;
            }
        }for(IntVar[] iv : Brows){
            for(int i = 0; i < iv.length; i ++){
                list[index] = iv[i];
                index++;
            }
        }
        return list;
    }
}
