
Sum demonstrator
================

This demonstrator will compute the sum of a number of integers input by party
1. The inputs are fixed for this demonstrator. This demonstrator also shows that
switching the underlying protocol suite is very easy.

run the demonstrator, use the Makefile and write

* make build
* make runSPDZ
* make runDummy

The build target runs the compilation process and generates a runnable jar with
the sum demo as the main target.

The run[*] targets runs the actual MPC computation and releases a result which
is the sum of the inputted values. For this demonstrator, it should be 65, since
the input is fixed to the list of [1, 2, 3, 7, 8, 12, 15, 17].

Choosing runSPDZ will cause the SPDZ protocol suite to be used, and using runDummy
will cause the Dummy protocol suite to be used. 