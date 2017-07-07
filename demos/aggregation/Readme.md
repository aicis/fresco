
Aggregation demonstrator
========================

This demonstrator will compute the aggregation of a hardcoded list.
The list consists of pairs of (key,value). The demonstrator aggregates all values where the key's match.
This is done using a deterministic encryption scheme (within MPC) to reveal the encrypted keys. 

To run the demonstrator, use the Makefile and write

* make build
* make run

The build target runs the compilation process and generates a runnable jar with
the aggregation demo as the main target.

The run target runs the actual MPC computation and prints the result in the sys.out stream (which can be found within both server1/log.txt and server2/log.txt. 