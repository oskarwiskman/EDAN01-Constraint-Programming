/**
 * SimpleDFS.java
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

public class SimpleDFS {

    boolean trace = true;

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

    public SimpleDFS(Store s) {
        store = s;
    }

    public int nbrNodes = 0;

    public int wrongDec = 0;


    /**
     * This function is called recursively to assign variables one by one.
     */
    public boolean label(IntVar[] vars) {

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
            nbrNodes++;

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
                wrongDec++;
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
        int example = 2;

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
                if(example==0){
                    searchVariables = new IntVar[v.length-1];
                    for (int i = 0; i < v.length-1; i++) {
                        searchVariables[i] = v[i+1];
                    }

                    return v[0];
                }
                if (v[0].min() == v[0].max()) {
                    searchVariables = new IntVar[v.length - 1];
                    for (int i = 0; i < v.length - 1; i++) {
                        searchVariables[i] = v[i + 1];
                    }

                } else {
                    searchVariables = new IntVar[v.length];
                    for (int i = 0; i < v.length; i++) {
                        searchVariables[i] = v[i];
                    }
                }
                return v[0];
            } else {
                System.err.println("Zero length list of variables for labeling");
                return new IntVar(store);
            }
        }

        /**
         * example value selection; indomain_min
         */
        int selectValue(IntVar v) {
            return example == 0 ? v.min() : (v.max() + v.min()) / 2;
        }

        /**
         * example constraint assigning a selected value
         */
        public PrimitiveConstraint getConstraint() {
            PrimitiveConstraint pc = new XeqC(var, value);
            switch (example) {
                case 0:
                    pc = new XeqC(var, value);
                break;
                case 1:
                    pc = new XlteqC(var, value);
                break;
                case 2:
                    pc = new XgteqC(var, value);
                break;
            }
            return pc;
        }
    }
}
