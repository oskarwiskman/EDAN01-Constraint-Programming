include "globals.mzn";

int: n;
array[1..n] of int: inPrices;
int: m;
array[1..m] of int: inBuy;
array[1..m] of int: inFree;
int: bs = sum (inBuy);
int: fs = sum (inFree);
int: vp = bs+fs;

arg_sort(inPrices)

if n > vp then 




int: sizePaid = 2;
int: sizeFree = 2;
array[1..sizePaid] of var 1..n: paidPizza;
array[1..sizeFree] of var 1..n: freePizza;
array[1..2] of int: indexFree = [1, 2];
array[1..2] of int: indexPaid = [1, 2];

constraint all_different(paidPizza++freePizza)
/\ forall (i in 1..m)
(forall (j in indexPaid[i]..indexPaid[i]+buy[i]-1)
 (forall (k in indexFree[i]..indexFree[i]+free[i]-1)
  (prices[freePizza[k]] <= prices[paidPizza[j]])));

solve maximize (prices[freePizza[1]]+prices[freePizza[2]]);
