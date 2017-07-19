
Aggregation demonstrator
========================

NB: THIS IS CURRENTLY UNDER DEVELOPMENT AND MAY NOT FUNCTION

This demonstrator will compute the aggregation of a hardcoded list.  The list
consists of pairs of (key,value). The demonstrator aggregates all values where
the key's match.  This is done using a deterministic encryption scheme (within
MPC) to reveal the encrypted keys.

To build the demonstrator, run the command:

* make build

The build target runs the compilation process and generates a runnable jar with
the aggregation demo as the main target. It also creates directories for each
MPC party.

To run the demonstrator using the dummy protocol suite, run the command:

* make runDummy

To run the demonstrator using the SPDZ protocol suite, run the command:

* make runSpdz

The run targets runs the actual MPC computation and prints the result in the
sys.out stream (which can be found within both server1/log.txt and
server2/log.txt).