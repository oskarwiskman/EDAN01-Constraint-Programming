import org.jacop.constraints.SumWeight;
import org.jacop.constraints.XneqY;
import org.jacop.core.IntVar;
import org.jacop.core.Store;
import org.jacop.search.*;

public class PizzaMaster {
    public static void main(String[] args) {
        long T1, T2, T;
        int n = 4;
        int[] price = {10, 5, 20, 15};
        int m = 2;
        int[] buy = {1,2};
        int[] free = {1,1};

        T1 = System.currentTimeMillis();
        letsDeal(n, price, m, buy, free);
        T2 = System.currentTimeMillis();
        T = T2 - T1;
        System.out.println("\n\t*** Execution time = " + T + " ms");
    }

    private static void letsDeal(int n, int[] price, int m, int[] buy, int[] free) {
        Store store = new Store();
        IntVar[] pPaid = new IntVar[n];
        IntVar[] pFree = new IntVar[n];
        IntVar[] pVouch = new IntVar[n];

        for(int i = 0; i < n; i++){
            pPaid[i] = new IntVar(store, 0, 1);
            pFree[i] = new IntVar(store, 0, 1);
            pVouch[i] = new IntVar(store, 0, 1);

            store.impose(new XneqY(pPaid[i], pFree[i]));
            store.impose(new XneqY(pVouch[i], pFree[i]));
        }

        IntVar[] vFree = new IntVar[m];
        IntVar[] vUsed = new IntVar[m];

        for(int i = 0; i < m; i++){
            vFree[i] = new IntVar(store, 0, free[i]);
            vUsed[i] = new IntVar(store, 0, 1);


        }
        IntVar cost = new IntVar(store, 1, 10000);
        store.impose(new SumWeight(pPaid, price, cost));

        Search<IntVar> search = new DepthFirstSearch<IntVar>();
        SelectChoicePoint<IntVar> select = new SimpleSelect<IntVar>(pPaid, null, new IndomainMin<>());

        search.setSolutionListener(new PrintOutListener<IntVar>());
        search.getSolutionListener().searchAll(true);

        boolean result = search.labeling(store, select);

        if(result){
            System.out.println("Cost: " + cost.value());
        }else{
            System.out.println("No solution found.");
        }

    }
}
