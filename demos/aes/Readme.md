
Distance demonstrator
=====================

This demonstrator will use AES to encrypt a block of plaintext comming from
party 2 using a key provided by party 1.

To run the demonstrator, use the Makefile and write

* make build
* make runPrePro
* make run

The build target runs the compilation process and generates a runnable jar with
the set intersection demo as the main target.

The runPrePro target is used since TinyTables is the boolean protocol suite we
use, and this requires us to run a preprocessing phase before the actual MPC can
run.

The run target runs the actual MPC computation and computes the encryption of
whatever party 2 input using the AES key given by party 1. For this
demonstrator, the output should be: 69c4e0d86a7b0430d8cdb78070b4c55a