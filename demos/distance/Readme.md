
Distance demonstrator
=====================

This demonstrator will compute the distance between two points in a euclidian
two dimensional space. 

To run the demonstrator, use the Makefile and write

* make build
* make run

The build target runs the compilation process and generates a runnable jar with
the set intersection demo as the main target.

The run target runs the actual MPC computation and releases a result which is
the distance between the two inputted points. For this demonstrator, it should
be approximately 11.1803 since the inputted values for party 1 are (x:10, y:10)
and party 2 inputs (x:20, y:15).