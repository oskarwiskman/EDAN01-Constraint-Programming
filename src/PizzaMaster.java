import org.jacop.core.IntVar;
import org.jacop.core.Store;

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
        IntVar[] p = new IntVar[n];


    }
}
