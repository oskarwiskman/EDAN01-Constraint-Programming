import org.jacop.core.Store;

/**
 * Created by oskar on 1/26/16.
 */
public class PizzaMaster {
	public static void main(String[] args) {
		long T1, T2, T;
		T1 = System.currentTimeMillis();
		letsDeal();
		T2 = System.currentTimeMillis();
		T = T2 - T1;
		System.out.println("\n\t*** Execution time = " + T + " ms");
	}
private static void letsDeal() {
		Store store = new Store();	
		
	}
}
