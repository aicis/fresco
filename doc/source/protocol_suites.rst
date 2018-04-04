.. _protocol_suites:

Protocol Suites
===============

Various techniques for secure computation are currently known. In the literature these are referred
to as secure computation *protocols*. However, as these usually consist of a number of sub-protocols
in FRESCO we use the term *protocol suites* to avoid ambiguity. I.e., in FRESCO a protocol suite is
taken to be a set of sub-protocols that as a collection implements general secure computation.

FRESCO is designed to work with multiple interchangeable protocol suites and aims to support the
development of new protocol suites. FRESCO also comes with a few protocol suites already
implemented. The following table gives a rough comparison of the currently included protocol suites.

====================  =======  ===========  ====================  ========
Suite                 Parties  Adversary    Model of Computation  Reactive
====================  =======  ===========  ====================  ========
`Dummy Boolean`_      1+       none         Boolean               yes
`Dummy Arithmetic`_   1+       none         Arithmetic            yes
`TinyTables`_         2        semi-honest  Boolean               yes
`SPDZ`_               2+       malicious    Arithmetic            yes
====================  =======  ===========  ====================  ========

Here the *Parties* column describes the number of parties that can be involved in secure computation
using the given protocol suite. *Adversary* describes the type of adversary the protocol suite
tolerates. The *Model of Computation* describes how the protocol represent the computation to be
securely computed. Currently, protocol suites in FRESCO are tied to a single model of computation, i.e., 
the SPDZ suite only supports Arithmetic computations and does not support Boolean computations. Finally, 
a protocol suite being *reactive* means that it allows intermediate values to be opened, and further 
secure computation may continue on closed values that depend on the values opened so far.

Below we will describe the protocol suites in a little more detail.

.. _`Dummy Boolean`:
.. _`Dummy Arithmetic`:

The Dummy Boolean and Arithmetic  Protocol Suites
-------------------------------------------------

The dummy suites do all computations in the clear and thus provide *no security at all*. These
suites are intended for testing and debugging. Contrary to other protocol suites they can run with 
only one party.

The dummy suites are also useful for benchmarking: The overhead of the dummy suite can be seen as
the *baseline* overhead of FRESCO when no security is applied. 

.. _TinyTables:

The TinyTables protocol suite
------------------------------

The *TinyTables* protocol suite is based on work by Damgård *et al.* `[DNNR17]`_. This protocol suite
works in the Boolean setting, with exactly two parties and the original protocol comes in versions
that provide security against both a semi-honest and malicious adversary. The version currently
implemented in FRESCO, however, only implements security against semi-honest adversaries.

TinyTables uses a simple technique to preprocessing the function to be evaluated before the input is
known. This preprocessing involves creating a small table of values for each *AND* gate involved,
hence the name TinyTables. Online evaluation is reduced to a lookup into such a table for each
*AND* gate with minimal communication overhead. As with other Boolean protocol suites, TinyTables
evaluates XOR's locally without communication.

.. _SPDZ:

The SPDZ Protocol Suite
-----------------------

The SPDZ suite is based on another work of Damgård *et al.* `[DPSZ12]`_. This protocol suite works
over a finite field of size at least :math:`2^s` where s is a `statistical security parameter`.
I.e., it works in the arithmetic setting. SPDZ allows for two or more parties to participate in the
secure computation and is secure against malicious adversaries.

SPDZ is based on additive secret sharing over the given finite field. It requires a preprocessing
step to produce so called *Beaver Triples* which will be used online to evaluate multiplications.
Contrary to TinyTables, SPDZ preprocessing is not directly dependent on the function to be evaluated
online beyond the number of multiplications to be performed. In the online evaluation, SPDZ uses the
preprocessed data to evaluate each multiplication with a small amount of communication, whereas
addition can be done locally.


References
----------

.. _`[DNNR17]`:

| [DNNR17]:
| *Ivan Damgård, Jesper Buus Nielsen, Michael Nielsen and Samuel Ranellucci*
| **The TinyTable Protocol for 2-Party Secure Computation, or: Gate-Scrambling Revisited**
| CRYPTO 2017
|
|
.. _`[DPSZ12]`:

| [DPSZ12]:
| *Ivan Damgård, Marcel Keller, Enrique Larraia, Valerio Pastro, Peter Scholl and Nigel P. Smart*
| **Practical Covertly Secure MPC for Dishonest Majority – Or: Breaking the SPDZ Limits**
| ESORICS 2013
|
|

