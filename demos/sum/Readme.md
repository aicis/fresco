
Sum demonstrator
================

This demonstrator will compute the sum of a number of integers input one of two parties. For this
demonstrator to the input is fixed to the list of numbers 1, 2, 3, 7, 8, 12, 15 and 17. I.e., the
result should be 65.

This demonstrator also shows that we can switch the underlying protocol suite for the computation.

Running the Demo with Docker
----------------------------

If you are using docker the easiest way to run the demo is to run

```
docker-compose up
```

This will install and run two docker containers (called `alice` and `bob`) each running one of the
parties in the computation. Note, that the first time you run this command the the docker containers
will be build, which may take a few minutes.

The two parties will do the MPC computation to compute the sum and output the result using the dummy
protocol suite (i.e., a mock MPC protocol that does the computation in the clear).

To run the demo using the SPDZ protocol suite you open the file `docker-compose.yml` and substitute each of the lines saying 

```
- dummyarithmetic
```
with 
```
- spdz 
- -Dspdz.preprocessingStrategy=DUMMY
```
and run the demo as described above.


Running the Demo with Make 
-------------------------

You can also run the demo using `make`. To do this first build the demo using

```
make build
```

to run the compilation process and generates a runnable jar with the sum demo as the main target. 

You can then run the demo using by running
```
make runDummy
```

which runs the MPC computation using the dummy protocol suite with three parties.

Alternatively, you can run the demo with 
```
make runSPDZ
```
which will run the MPC computation with the SPDZ protocol suite (using mock preprocessing). 

