package Lab5Java;

import org.jacop.constraints.*;
import org.jacop.core.IntVar;
import org.jacop.core.Store;
import org.jacop.search.*;

public class UrbanPlanning {
	public static void main(String[] args) {
		long T1, T2, T;

		T1 = System.currentTimeMillis();
		example(1);
		T2 = System.currentTimeMillis();
		T = T2 - T1;
		System.out.println("\n\t*** Execution time = " + T + " ms");
		
	}
		public void solve(int n, int n_commercial, int n_resedential, int[] point_distribution){
			Store store = new Store();

			//konstruerar 2 symetriska matriser [row][column]
			//1 = residense 0= commercial
			IntVar[][] grid = new IntVar[n][n]; //the grid of the area
			IntVar[][] symgrid = new IntVar[n][n]; // does it need to be symmetric? not in the example..
			
			for(int i = 0; i<n; i++){
				for( int j= 0; j<n; j++){
					grid[i][j] = new intvar(store, "type" +i+j ,0, 1);
					symgrid[j][i] = new intvar(store, "type" +i+j ,0, 1);
					store.impose(new XeqY(grid[i][j]), symgrid[j][i]);
				}
			}
			//contraint that nbr res in grid = n_resedential and nr commercial = n_commercial
			Intvar resInGrid = new Intvar(store, "nrResInGrid", n_resedential, n_resedential);
//			Intvar comInGrid = new Intvar(store, "nrComInGrid", n_commercial, n_commercial); //överflödig?
			Intvar[] gridsum = new Intvar[n*2];
			int index=0;
			for(int i=0; i<gridSum.length; i++){
				for(int j=0; j<gridSum.length; j++){
					gridSum[index] = grid[i][j];
					index++;
				}
			}
			store.impose(new sum(gridSum, resInGrid));
			
			//Ansätt poäng beroende på type new intvar(-n,n)
			
			//poäng i rader i area = kolumn i symarea??  
			
		}
		public static void example(int ex ){
	        switch(ex) {
	            case 1:
	                int n = 5;
	                int n_ commercial = 13;
	                int n_residential = 12;
	                int[] point_distribution = {-5,-4,-3,3,4,5};
	                solve(n, n_commercial, n_resedential, point_distribution);
	                break;
	            case 2:
	            	 int n = 5;
		                int n_ commercial = 7;
		                int n_residential = 18;
		                int[] point_distribution = {-5,-4,-3,3,4,5};
		                solve(n, n_commercial, n_resedential, point_distribution);
	                break;
	            case 3:
	            	 int n = 7;
		                int n_ commercial = 20;
		                int n_residential = 29;
		                int[] point_distribution = {-7,-6,-5,-4,-3,3,4,5,6,7};
		                solve(n, n_commercial, n_resedential, point_distribution);
	                break;
	            default: System.err.println("Example " + ex + " is not implemented.");
	        }
	    }

}
