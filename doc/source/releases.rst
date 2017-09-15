
.. _releases:

Releases
========

FRESCO uses `semantic <http://semver.org/>`_ versioning. FRESCO has
still not arrived at a stable API and the latest release is 0.1.0. You
can download this and older releases from `GitHub
<http://github.com/aicis/fresco/releases>`_ Here is a description of
the FRESCO releases:


0.1
---

The initial version, released 2015-11-30.
 
* The overall FRESCO framework.

* Implementation of the BGW and SPDZ protocol suites (only the SPDZ online phase). 

* A standard library, including
 
  * Generic protocols for comparison of integers.

  * Protocols for AES, DES, MD5, SHA1, and SHA256 provided by the
    `Bristol Cryptography Group
    <https://www.cs.bris.ac.uk/Research/CryptographySecurity/MPC>`_ .


0.2
---

This is the next release in the pipeline. Most of it is already present within the
development branch. We plan to include:

* The :ref:`TinyTables<tinytables>` protocol suite as well as a :ref:`Dummy
  arithmetic<DUMMY_ARITHMETIC>` protocol suite.

* The preprocessing phase for the SPDZ protocol suite. 

* Many improvements to the standard library. See the :ref:`full list<STD_LIB>`.

* Rework of the entire framework to ease the writing and understanding of
  applications.

* Great improvements to the test coverage.

Future work/releases
--------------------

We will work towards a stable release 1.0. The framework will continuously be
improved on, but no major changes are present in the current pipeline.
