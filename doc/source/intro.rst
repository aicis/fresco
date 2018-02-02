.. _intro:

Introduction
============

FRESCO is a FRamework for Efficient Secure COmputation. It is designed to make it easy and efficient
to write prototype applications based on secure computation.


What is Secure Computation?
---------------------------

*Secure computation* (also known as Multi-Party Computation (MPC) or Computation on Encrypted Data
(CoED)) is a cryptographic tool that allows a number of parties to jointly compute functions of their
private data, without revealing the underlying private data. As an example consider the classic
*millionaires problem*: Two millionaires meet on the street and want to decide who is the richest of
the two, but none of the two are willing to reveal the size of his own fortune. The millionaires can
solve this problem, by comparing their fortunes using secure computation to ensure that they learn
only who is the richest and no private data is revealed. Furthermore, they can do so directly
between each other, without having to trust any third parties.

In general any computable function can be computed privately using secure computation. To give a few
examples the FRESCO framework has been used in prototypes to 

* Compute statistical data from surveys without revealing the individual survey answers (in the PRACTICE_).

* Benchmark the financial and energy performance of companies while keeping the private performance
  data of the individual company (in the PRACTICE_ and `Big Data by Security`_ projects repectively).

* Let banks credit rate potential customers without revealing the private data of the costumers *or*
  the private credit rating functions of the bank (in the `Big Data by Security`_ project).

.. _PRACTICE : https://practice-project.eu/

.. _`Big Data by Security`: https://bigdatabysecurity.dk/

Main Features of FRESCO
-----------------------

The FRESCO framework aims to support the development of both new applications using secure
computation, and the develpment of new secure computation techniques (refered to as *protocol
suites* in FRESCO) to be used as the backend for those application. In some sense FRESCO can be
thought of as a *hub* that provides the infrastructure to connect applications with protocol suites.
The focus of the framework is the following main features:

* **Rapid and simple application development**. With FRESCO you can write application that uses
  secure computation without being an expert in cryptography. You only need to specify which data to
  "close" and which data to "open". FRESCO provides a *standard library* of many commonly used
  secure functionalites. These can be easily combined in order to quickly achieve new complex
  functionlities for use in applications. Once you have written your application, you can run it
  using different kinds of protocol suites. This is important, since each suite comes with its
  own specific security level and performance, and you may not even know which kind of security is
  required at the time you write your application.

* **Rapid and simple protocol suite development**. FRESCO provides a collection of reusable patterns
  and components that allows protocol suites to be developed with minimal effort. Once you have
  developed your protocol suite, you immediately get the benefit that many existing applications
  (and tests) can run on top of your new suite.

.. Not really sure about the last two points...

* **Open and flexible design**. FRESCO provides great freedom regarding the way you implement your
  applications and protocol suites. Applications can, e.g., be specified in Java, or as a textual
  representation of a circuit. Protocol suites have full freedom and control over things such as
  thread scheduling and networking. It is even possible, using JNI, to write your protocol suite in
  C/C++ and still get the benefit of access to many existing applications written using FRESCO.

* **Support for large and efficient computations**. FRESCO supports techniques such as
  parallelization and pre-processing that allows to cope with large computations.


Contact
-------

If you have any comments, questions or ideas, feel free to contact the
FRESCO development team either by dropping a mail to
fresco@alexandra.dk or by using our `issue tracker
<https://github.com/aicis/fresco/issues>`_ at GitHub.

Related Projects
----------------

For further projects related to secure computations we refer to the Awesome-MPC_ list.

.. _Awesome-MPC: https://github.com/rdragos/awesome-mpc

