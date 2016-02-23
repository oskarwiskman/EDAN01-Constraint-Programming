include "globals.mzn";
include "jacop.mzn";

int : nbrNodes; int : N;

%Constants
int : nbrAddNodes; int : nbrMulNodes; int : nbrAdds; int : nbrMuls;
int : addCycles; int : mulCycles;

%Graph model
array[0..N] of set of int : connections;
set of int : addNodes;
set of int : mulNodes;
int : maxTime; 
array[0..N] of var 0..maxTime : cycles;
array[0..N] of var 0..maxTime : start;
var 0..maxTime : end;

array[0..nbrAddNodes] of var 0..maxTime : startAdd;
array[0..nbrMulNodes] of var 0..maxTime : startMul;

array[0..nbrAddNodes] of var 0..maxTime : addDur = array1d(0..nbrAddNodes,
 [addCycles | i in 0..nbrAddNodes]);
array[0..nbrMulNodes] of var 0..maxTime : mulDur = array1d(0..nbrMulNodes,
 [mulCycles | i in 0..nbrMulNodes ]);

%Resource requirements.
array[0..nbrAddNodes] of int : adders = array1d(0..nbrAddNodes,
 [1 | i in 0..nbrAddNodes ]);
array[0..nbrMulNodes] of int : mullers = array1d(0..nbrMulNodes,
 [1 | i in 0..nbrMulNodes ]);

%Available components at current tier.
array[0..nbrAddNodes] of var 1..nbrAdds : availableAdders;
array[0..nbrMulNodes] of var 1..nbrMuls : availableMullers;

/* ----------------------------- Constraints -----------------------------*/

constraint
	diff2(startAdd, availableAdders, addDur, [ 1 | i in 0..nbrAddNodes ]);

constraint
	diff2(startMul, availableMullers, mulDur, [ 1 | i in 0..nbrMulNodes ]);

constraint
  forall (i in index_set(addNodes)) (
    cycles[addNodes[i]] = addDur[i-1] /\
    start[addNodes[i]] = startAdd[i-1]
    );
constraint
  forall (i in index_set(mulNodes)) (
    cycles[mulNodes[i]] = mulDur[i-1] /\
    start[mulNodes[i]] = startMul[i-1]
    );

constraint
  forall (i in 0..N, d in connections[i] ) (
    start[d] + cycles[d] <= start[i]
  );

constraint
  cumulative(startAdd, addDur, adders, nbrAddNodes);

constraint
  cumulative(startMul, mulDur, mullers, nbrMulNodes);

constraint
  forall (i in index_set(cycles)) (
    start[i] + cycles[i] <= end
  );

solve :: seq_search([
    int_search(startAdd++startMul, first_fail, indomain_min, complete),
    int_search(availableAdders++availableMullers, first_fail, indomain_min, complete)
  ])
minimize end;

output[ show(start), show(cycles), show(end), show(availableAdders), show(availableMullers) ];