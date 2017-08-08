
PSI - Private Set Intersection
==============================

This demonstrator will compute the set intersection between two parties. The
requirement is that the protocol suite is a boolean version such as Tiny Tables,
and that the two input lists are of equal length.

To build the demonstrator, use the Makefile by running:

* make build

This runs the compilation process and generates a runnable jar with
the set intersection demo as the main target and creates a folder per party.

To run the demonstrator using the dummy protocol suite, run the command:

* make runDummy

To run the demonstator using the tinytables protocol suite, run the command:

* make runPrePro
* Wait approximately 10 seconds until the log files within the two server
  directories outputs a lot of 0's. Ignore this output as it's not the actual one.
* make run

The runPrePro target is there since we use TinyTables which requires that we
first preprocess various values needed for the actual run of the protocol
suite. A file is created which contains the needed preprocessed material. This
file is needed by the actual TinyTables computation.

The last target runs the actual MPC computation and releases a result. The
result should be read as a concatinated list of all the inputs, where each has
been deterministicly encrypted using AES within MPC. This means that if some of
the first half's hex strings are equal to the second half, there is an
intersection for that number. Indices are kept, so for the demo inputs, you
should be able to observe equality for index 1, 2, 3 and 7. This translates into
equality between the ciphertext indices: 0 and 7, 1 and 8, 2 and 9 and finally 6
and 13.