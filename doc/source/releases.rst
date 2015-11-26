
.. _releases:

Releases
========

This page gives an overview of the releases of FRESCO.

The latest release is 1.0:

1.0
---

The initial version, released 2015-11-30, can be downloaded from
`here <http://github.com/aicis/fresco/releases>`_.
 
* The overall FRESCO framework.
* Implementation of the BGW protocol suite.
* Implementation of the SPDZ protocol suite (online phase only).
* A standard library, including
 
  * Protocols from `here
    <https://www.cs.bris.ac.uk/Research/CryptographySecurity/MPC>`_
    for AES, DES, as well as MD5, SHA1, and SHA256.
 
  * Some generic protocols for comparison of integers. 

.. 
   1.0.1
    
   * Bugfix X.
   * Bugfix Y.
    
   .. todo:: Release 1.0.1 is not really there yet! 


1.1
---

This is a planned future release. We plan to include:

* A protocol suite that uses the 2PC protocol in SCAPI.

* A persistent storage alternative to the current MySQL storage, but
  that is instead based on files and that can efficiently handles very
  large amounts of preprocessed data.
