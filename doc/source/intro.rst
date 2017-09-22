
Introduction
============

FRESCO is a FRamework for Efficient Secure COmputation. It is designed
to make it easy and efficient to write prototypes based on secure
computation. We hope that it will grow mature enough to be used for
production systems, too.


What is Secure Computation?
---------------------------

Secure computation allows a number of parties to *compute on private
data*. The classical example is the *millionaires problem*: Two
millionaires meet on the street and want to decide who is the richest
of the two, but none of the two is willing to reveal his own
fortune. If there are no third party, that both millionaires trust,
this seems like an unsolvable problem.

But modern cryptographic techniques make such computations possible,
without a trusted third party. Many such techniques have been
developed since the 1980s and new improvements are continuously being
invented. In FRESCO we call each of these techniques a :term:`protocol
suite`.

The millionaires problem is just a toy application, but secure
computation has many important real-world uses. It can, e.g., be used
to benchmark companies or organizations based on their private
data. That is, a graph can be computed and revealed to the companies,
showing how the companies perform relative to each other with respect
to some parameters, *without* the need for the companies to reveal any
of their private data.


Main Features of FRESCO
-----------------------


* **Rapid and simple application development**. With FRESCO you can
  write application that uses secure computation without being an
  expert in cryptography. You only need to specify which data to
  "close" and which data to "open". FRESCO provides a :ref:`standard
  library<STD_LIB>` of many commonly used secure functionalites. These can be
  easily combined in order to quickly achieve new applications. Once
  you have written your application, you can run it using many
  different kinds of protocol suites. This is important, since each
  suite comes with its own specific security level and performance,
  and you may not even know which kind of security is required at the
  time you write your application.

* **Rapid and simple protocol suite development**. FRESCO provides a
  collection of reusable patterns that allows protocol suites to be
  developed with minimal effort. Once you have developed your protocol
  suite, you immediately get the benefit that many existing
  applications (and tests) can run on top of your new suite.

* **Open and flexible design**. FRESCO provides great freedom
  regarding the way you implement your applications and protocol
  suites. Applications can, e.g., be specified in Java, or as a
  textual representation of a circuit. Protocol suites have full
  freedom and control over things such as thread scheduling and
  networking. It is even possible, using JNI, to write your protocol
  suite in C/C++ and still get the benefit of access to many existing
  applications written using FRESCO.

* **Support for large and efficient computations**. FRESCO supports
  techniques such as streaming, parallelization and pre-processing
  that allows to cope with large computations.

In some sense FRESCO can be thought of as a *hub* that provides the
infrastructure to connect applications with protocol suites.


Contact
-------

If you have any comments, questions or ideas, feel free to contact the
FRESCO development team either by dropping a mail to
fresco@alexandra.dk or by using our `issue tracker
<https://github.com/aicis/fresco/issues>`_ at GitHub.
