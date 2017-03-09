
Demos
=====

This folder contains various demonstrators which can be used to build and run
FRESCO applications. We do not provide actual jar files, but rather the scripts
and tools needed to create these yourself. Standard parameters are included in
the scripts.

Set Intersection Demo
---------------------

This demonstrator will compute the set intersection between two parties. The
requirement is that the protocol suite is a boolean version such as Tiny Tables.

To run the demonstrator, use the Makefile and write

* make build
* make runPrePro
* Wait approximately 20 seconds until the log files within the two server
* directories outputs a lot of 0's. Ignore this output as it's not the actual one.
* make run

The first target runs the compilation process and generates a runnable jar with
the set intersection demo as the main target.

The second target is there since we use TinyTables for this demonstrator. This
protocol suite requires that we first preprocess various values needed for the
actual run of the protocol suite. A file is created which contains the needed
preprocessed material. This file is needed by the actual TinyTables computation.

The last target runs the actual MPC computation and releases a result.