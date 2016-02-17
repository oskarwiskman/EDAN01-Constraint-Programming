int : nbrNodes; int : N = nbrNodes - 1;

%Constants
int : nbrAddNodes; int : nbrMulNodes; int : nbrAdds; int : nbrMuls;
int : addCycles; int : mulCycles;

%Graph model
array[0..N] of set of int : connections;
set of int : addNodes;
set of int : mulNodes; 
array[0..N] of var 0..maxTime : cycles;
array[0..N] of var 0..maxTime : start;
var 0..maxTime : end;

array[0..nbrAddNodes] of var 0..maxTime : startAdd;
array[0..nbrMulNodes] of var 0..maxTime : startMul;
array[0..nbrAddNodes] of var 0..maxTime : addCycles = array1d(0..nbrAddNodes,
 [addCycles | i in 0..nbrAddNodes]);
array[0..nbrMulNodes] of var 0..maxTime : mulCycles = array1d(0..nbrMulNodes,
 [ mulCycles | i in 0..nbrMulNodes ]);

%Resource requirements.
array[0..nbrAddNodes] of int : adders = array1d(0..nbrAddNodes,
 [ 1 | i in 0..nbrAddNodes ]);
array[0..nbrMulNodes] of int : mullers = array1d(0..nbrMulNodes,
 [ 1 | i in 0..nbrMulNodes ]);

%Available components at current tier.
array[0..nbrAddNodes] of var 0..nbrAdds : availableAdders;
array[0..nbrMulNodes] of var 0..nbrMuls : availableMullers;

/* ----------------------------- Constraints -----------------------------*/

constraint
	diff2(startAdd, availableAdders, addCycles, [ 1 | i on 0..nbrAddNodes ]);

constraint
	diff2(startMul, availableMullers, mulCycles, [ 1 | i on 0..nbrMulNodes ]);

constraint
  forall (i in index_set(addNodes)) (
    cycles[addNodes[i]] = addCycles[i-1] /\
    start[addNodes[i]] = startAdd[i-1]
    );
constraint
  forall (i in index_set(mulNodes)) (
    cycles[mulNodes[i]] = mulCycles[i-1] /\
    start[mulNodes[i]] = startMul[i-1]
    );

constraint
  forall (i in 0..N, d in connections[i] ) (
    start[d] + cycles[d] <= start[i]
  );

constraint
  cumulative(startAdd, addCycles, adders, nbrAddNodes);

constraint
  cumulative(startMul, mulCycles, mullers, nbrMulNodes);
