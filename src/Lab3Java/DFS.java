package Lab3Java; /**
 * Lab3Java.DFS.java
 * This file is part of JaCoP.
 * <p>
 * JaCoP is a Java Constraint Programming solver.
 * <p>
 * Copyright (C) 2000-2008 Krzysztof Kuchcinski and Radoslaw Szymanek
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * Notwithstanding any other provision of this License, the copyright
 * owners of this work supplement the terms of this License with terms
 * prohibiting misrepresentation of the origin of this work and requiring
 * that modified versions of this work be marked in reasonable ways as
 * different from the original version. This supplement of the license
 * terms is in accordance with Section 7 of GNU Affero General Public
 * License version 3.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import org.jacop.constraints.*;
import org.jacop.core.FailException;
import org.jacop.core.IntDomain;
import org.jacop.core.IntVar;
import org.jacop.core.Store;

/**
 * Implements Simple Depth First Search .
 *
 * @author Krzysztof Kuchcinski
 * @version 4.1
 */

public class DFS {

    boolean trace = false;

    /**
     * Store used in search
     */
    Store store;

    /**
     * Defines varibales to be printed when solution is found
     */
    IntVar[] variablesToReport;

    /**
     * It represents current depth of store used in search.
     */
    int depth = 0;

    /**
     * It represents the cost value of currently best solution for FloatVar cost.
     */
    public int costValue = IntDomain.MaxInt;

    /**
     * It represents the cost variable.
     */
    public IntVar costVariable = null;

    public DFS(Store s) {
        store = s;
    }

    public int nbrNodes = 0;

    public int wrongDec = 0;

    public static int SIMPLE = 0, SPLIT1 = 1, SPLIT2 = 2;


    /**
     * This function is called recursively to assign variables one by one.
     */
    public boolean label(IntVar[] vars) {
        nbrNodes++;
        if (trace) {
            for (int i = 0; i < vars.length; i++)
                System.out.print(vars[i] + " ");
            System.out.println();
        }

        ChoicePoint choice = null;
        boolean consistent;

        // Instead of imposing constraint just restrict bounds
        // -1 since costValue is the cost of last solution
        if (costVariable != null) {
            try {
                if (costVariable.min() <= costValue - 1)
                    costVariable.domain.in(store.level, costVariable, costVariable.min(), costValue - 1);
                else
                    return false;
            } catch (FailException f) {
                return false;
            }
        }

        consistent = store.consistency();

        if (!consistent) {
            // Failed leaf of the search tree
            wrongDec++;
            return false;
        } else { // consistent

            if (vars.length == 0) {
                // solution found; no more variables to label

                // update cost if minimization
                if (costVariable != null)
                    costValue = costVariable.min();

                reportSolution();

                return costVariable == null; // true is satisfiability search and false if minimization
            }

            choice = new ChoicePoint(vars);

            levelUp();

            store.impose(choice.getConstraint());

            // choice point imposed.

            consistent = label(choice.getSearchVariables());

            if (consistent) {
                levelDown();
                return true;
            } else {

                restoreLevel();

                store.impose(new Not(choice.getConstraint()));
                // negated choice point imposed.

                consistent = label(vars);

                levelDown();

                if (consistent) {
                    return true;
                } else {
                    return false;
                }
            }
        }
    }

    void levelDown() {
        store.removeLevel(depth);
        store.setLevel(--depth);
    }

    void levelUp() {
        store.setLevel(++depth);
    }

    void restoreLevel() {
        store.removeLevel(depth);
        store.setLevel(store.level);
    }

    public void reportSolution() {
        if (costVariable != null)
            System.out.println("Cost is " + costVariable);

        for (int i = 0; i < variablesToReport.length; i++)
            System.out.print(variablesToReport[i] + " ");

        System.out.println("\nNumber of nodes: " + nbrNodes);
        System.out.println("Number of wrong decisions: " + wrongDec);
        System.out.println("\n---------------");
    }

    public void setVariablesToReport(IntVar[] v) {
        variablesToReport = v;
    }

    public void setCostVariable(IntVar v) {
        costVariable = v;
    }

    public class ChoicePoint {

        IntVar var;
        IntVar[] searchVariables;
        int value;
        /**
         * @param example ,
         * Choose which example to do.
         */
        int example = SPLIT1;
        /**
         * @param select,
         * Choose whether to select on smallest domain or not.
         * (default is to select on input order).
         */
        boolean select = false;

        public ChoicePoint(IntVar[] v) {
            var = selectVariable(v);
            value = selectValue(var);
        }

        public IntVar[] getSearchVariables() {
            return searchVariables;
        }

        /**
         * example variable selection; input order
         */

        public IntVar selectVariable(IntVar[] v) {
            if (v.length != 0) {
                if(example==SIMPLE){
                    searchVariables = new IntVar[v.length-1];
                    for (int i = 0; i < v.length-1; i++) {
                        searchVariables[i] = v[i+1];
                    }

                    return v[0];
                }
                if(select){
                    //Selects minimum domain first.
                    int smallest = Integer.MAX_VALUE;
                    int index = 0;
                    //Find the smallest domain.
                    for(int i = 0; i < v.length; i++){
                        if(v[i].domain.getSize() < smallest){
                            smallest = v[i].domain.getSize();
                            index = i;
                        }
                    }
                    //If the element with smallest domain is found
                    //remove it and shift the remaining values accordingly.
                    if(v[index].min()==v[index].max()){
                        searchVariables = new IntVar[v.length-1];
                        for(int i = 0; i < v.length-1; i++){
                            if(i<index){
                                searchVariables[i] = v[i];
                            }
                            else{
                                searchVariables[i] = v[i+1];
                            }
                        }
                    }
                    //Else just put them all back.
                    else{
                        searchVariables = new IntVar[v.length];
                        for (int i = 0; i < v.length; i++) {
                            searchVariables[i] = v[i];
                        }
                    }
                    //Return the element with smallest domain.
                    return v[index];
                }
                //Selects on input order.
                if (v[0].min() == v[0].max()) {
                    //First element is determined, remove it.
                    searchVariables = new IntVar[v.length - 1];
                    for (int i = 0; i < v.length - 1; i++) {
                        searchVariables[i] = v[i + 1];
                    }

                }
                else {
                    //Put them back.
                    searchVariables = new IntVar[v.length];
                    for (int i = 0; i < v.length; i++) {
                        searchVariables[i] = v[i];
                    }
                }
                //Return first element.
                return v[0];
            } else {
                System.err.println("Zero length list of variables for labeling");
                return new IntVar(store);
            }
        }

        /**
         * Value selection based on example.
         */
        public int selectValue(IntVar v) {
            switch(example){
                case 0: return v.min();
                case 1: return (v.max()+v.min())/2;
                case 2: return (v.max()+v.min())%2==0 ? (v.max()+v.min())/2 : (v.max()+v.min()+1)/2; //If odd, add one.
                default: return v.min();
            }
        }

        /**
         * Assigning constraint dependant on example.
         */
        public PrimitiveConstraint getConstraint() {
            switch (example) {
                case 0: return new XeqC(var, value);
                case 1: return new XlteqC(var, value);
                case 2: return new XgteqC(var, value);
                default:  return new XeqC(var, value);
            }
        }
    }
}
