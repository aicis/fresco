
Distance demonstrator
=====================

This demonstrator will use AES to encrypt a block of plaintext comming from
party 2 using a key provided by party 1.

To build the demonstrator, use the Makefile and run the command:

* make build

The build target runs the compilation process and generates a runnable jar with
the set intersection demo as the main target.

To run the demonstrator using the boolean dummy suite, use the Makefile and write:

* make runDummy

To run the demonstrator using the tinytables protocol suite, use the Makefile and write:

* make runPrePro
* make run

The runPrePro target is used since TinyTables requires us to run a preprocessing
phase before the actual MPC can run. This means that the preprocessing phase
could actually be run without knowing the actual input for the computation.

The run target runs the actual MPC computation and computes the encryption of
whatever party 2 inputs, using the AES key given by party 1. For this
demonstrator, the output should be: 69c4e0d86a7b0430d8cdb78070b4c55a