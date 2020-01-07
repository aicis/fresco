
Distance demonstrator
=====================

This demonstrator will compute the distance between two points in a euclidian
two dimensional space. 

To build the demonstrator, run the command:

* make build

The build target runs the compilation process and generates a runnable jar with
the distance demo as the main target.

To run the demonstrator using the dummy protocol suite, run the command:

* make runDummy

To run the demonstrator using the SPDZ protocol suite, run the command:

* make runSpdz

The run targets runs the actual MPC computation and releases a result which is
the distance between the two inputted points. For this demonstrator, it should
be approximately 11.1803 since the inputted values for party 1 are (x:10, y:10)
and party 2 inputs (x:20, y:15). These input values can be adjusted within the
Makefile (the -x and -y options).