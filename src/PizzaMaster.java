import org.jacop.constraints.*;
import org.jacop.core.IntVar;
import org.jacop.core.Store;
import org.jacop.search.*;
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
        IntVar[] paidPizzas = new IntVar[n];
        IntVar[] freePizzas = new IntVar[n];
        IntVar[][] voucherBought = new IntVar[m][n];
        IntVar[][] voucherFree = new IntVar[m][n];


        //Populera paidPizzaz och freePizzas
        //Samt lägg till constraint för att
        //en pizza kan inte både köpas och fås.
        for(int i = 0; i < n; i++){
            paidPizzas[i] = new IntVar(store, "Paid pizza"+i, 0,1);
            freePizzas[i] = new IntVar(store, "Free pizza"+i, 0,1);
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
                voucherBought[i][j] = new IntVar(store, "Paid pizza" + (i * 10 + j), 0, 1);
                voucherFree[i][j] = new IntVar(store, "Free pizza" + (i*10+j), 0, 1);
                store.impose(new XneqY(voucherBought[i][j], voucherFree[i][j]));
            }
        }



        //En pizza får inte användas för att "aktivera" två olika vouchers.
        int index = 0;
        for(IntVar[] iv : getColumns(voucherBought)){
            store.impose(new SumInt(store, iv, "<=", new IntVar(store, 1, 1)));
            store.impose(new SumInt(store, iv, "==", paidPizzas[index]));
            index++;
        }

        //En pizza ska inte tas ut gratis två gånger.
        for(IntVar[] iv : getColumns(voucherFree)){
            store.impose(new SumInt(store, iv, "<=", new IntVar(store, 1, 1)));
        }

        //Antalet gratizpizzor får inte överstiga antalet som vouchern erbjuder.
        for(int i = 0; i < m; i++){
           store.impose(new SumInt(store, voucherFree[i], "<=", new IntVar(store, free[i], free[i])));
        }

        //Du får inte ta fler gratispizzor än vouchern tillåter samt
        //du får inte ta gratispizzor om du inte betalar för tillräckligt många pizzor.
        for(int i =0; i < m; i ++){
            PrimitiveConstraint nbrPaid = new SumInt(store, voucherBought[i],">=", new IntVar(store, buy[i], buy[i]));
            PrimitiveConstraint nbrFree = new SumInt(store, voucherFree[i],"<=", new IntVar(store, free[i], free[i]));
            PrimitiveConstraint zero = new SumInt(store, voucherFree[i], "==", new IntVar(store, 0, 0));
            store.impose(new IfThenElse(nbrPaid, nbrFree, zero));
        }

        //Populera variabelvektorn med priser.
        IntVar[] priceVar = new IntVar[price.length];
        for(int i = 0; i < price.length; i++){
            priceVar[i] = new IntVar(store, price[i], price[i]);
        }
        //Pizza som tas gratis får inte vara dyrare än den billigaste som köpts.
        for(int i = 0; i < m; i++){
            for(int j = 0; j<n; j++){
                for(int k = 0; k < n; k++){

                }
            }
        }


        Search<IntVar> search = new DepthFirstSearch<IntVar>();
        SelectChoicePoint<IntVar> select = new SimpleSelect<IntVar>(paidPizzas, null, new IndomainMin<IntVar>());

        search.setSolutionListener(new PrintOutListener<IntVar>());
        search.getSolutionListener().searchAll(true);

        boolean result = search.labeling(store, select);

        if (result) {
            System.out.println("Solution : " + java.util.Arrays.asList(paidPizzas));
        } else {
            System.out.println("No solution found.");
        }

    }

    private static ArrayList<IntVar[]> getColumns(IntVar[][] matrix) {
        ArrayList<IntVar[]> cols = new ArrayList<>();
        IntVar[] col = new IntVar[matrix[0].length];

        for (int j = 0; j < matrix.length; j++) {
            for (int i = 0; i < matrix[0].length; i++) {
                col[i] = matrix[j][i];
            }
            cols.add(col);
        }
        return cols;
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

    private static int getCheapest(IntVar[] var, int[] price){
        int min = Integer.MAX_VALUE;
        for(int i = 0; i < price.length; i++){
            System.out.println("Min: " +var[i].value());
            if(var[i].value() != 0 && min > var[i].value()*price[i]){
                min = var[i].value()*price[i];
            }
        }
        return min;
    }

    private static int getMostExpensive(IntVar[] var, int[] price){
        int max = Integer.MIN_VALUE;
        for(int i = 0; i < price.length; i++){
            System.out.println("Max: " +var[i].value());
            if(max < var[i].value()*price[i]){
                max = var[i].value()*price[i];
            }
        }
        return max;
    }
}
