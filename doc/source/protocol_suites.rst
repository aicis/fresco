
Protocol Suites
===============

FRESCO comes with a number of ready-cooked protocol suites. So, which
of the these suites should you choose? The answer depends on the
security and performance requirements of your concrete application. In
general, the less security you need, the faster suite you can
choose. Be sure you understand the details below before you use a
given suite in a real-world application.

The following table gives a rough comparison of the protocol suites.

==================  =======  ===========  =====================  ========
Suite               Parties  Security     Model of Computation   Reactive
==================  =======  ===========  =====================  ========
DUMMY_BOOL          1+	     none         Boolean                yes
DUMMY_ARITHMETIC    1+	     none         Arithmetic             yes
TinyTables          2        semi-honest  Boolean                yes
SPDZ                2+       malicious    Arithmetic             yes
==================  =======  ===========  =====================  ========

Whether to choose a suite that supports arithmetic or Boolean circuits
depends on your particular application. As a rule of thumb, if your
application requires a lot of addition and multiplication of integers,
an arithmetic suite performs best, while if it naturally deals a lot
with bits, a Boolean suite may be preferable.

.. The FRESCO standard library contains protocols that allow integer
   comparisons to be computed in arithemtic circuit suites and integer
   additions and multiplications to be done in Boolean circuit suites,
   but this comes at an additional overhead. TODO: Implement this in
   standard library.

A :term:`reactive protocol suite` allows intermediate values to be
opened, and further secure computation may continue on closed values
that depend on the values opened so far. This is in contrast to the
simpler case where the application is instead just a *function* where
the only the final output is opened. If your application is reactive,
choose a suite that supports reactive computation. Otherwise, you must
run a *completely* new instance of FRESCO, i.e., with new keys, each
time your application needs to continue after some values have been
opened.

..
    =====  =====  ======
       Inputs     Output
    ------------  ------
      A      B    A or B
    =====  =====  ======
    False  False  False
    True   False  True
    False  True   True
    True   True   True
    =====  =====  ======

..
    =====  =====
    col 1  col 2
    =====  =====
    1      Second column of row 1.
    2      Second column of row 2.
           Second line of paragraph.
    3      - Second column of row 3.

           - Second item in bullet
             list (row 3, column 2).
    \      Row 4; column 1 will be empty.
    =====  =====


.. important:: There are many more security properties to consider,
   and sometimes these can be subtle. Be sure you have a good
   understanding of the security properties provided by a given
   protocol before using it in a real setting. Consult the research
   papers cited below and text books on secure computing such as
   [Gol04]_, [LH10]_, and [CDN15]_.


.. _DUMMY_BOOL:

The Dummy Boolean Protocol Suite
--------------------------------

The dummy suite provides *no security* at all. It is only intended for
testing and debugging. Contrary to other protocols it has the feature
that it can run with only one party. Furthermore, as no encryption is
used, the computation goes on in the clear, so useful debug
information can be printed out.

In addition, since no cryptography is used, it can be used for
benchmarking: The overhead of the dummy suite can be seen as the
*basis* overhead of FRESCO, if no security is applied. By comparing
the performance of an application running on the dummy suite to the
performance of the same application running on another suite, you get
an idea of how much *additional* overhead you introduce in the other
suite, i.e., how much the extra security costs compared to the basic
overhead.

.. _DUMMY_ARITHMETIC:

The Dummy Arithmetic Protocol Suite
-----------------------------------

Similar to the boolean version, this suite provides no security at all. It
operates within a field of variable size to better simulate how other real
arithmetic protocol suites would function. This catches e.g. overflow issues. As
with the boolean version, this protocol suite is intended for testing and
debugging purposes only. It can also be run by just a single party. The idea of
using this protocol suite as a basis is just as valid as for the boolean
version. 

.. _TinyTables:

The TinyTables protocol suite
------------------------------

.. TODO: fix citation style for tiny tables below

The TinyTables protocol suite is based on [TINY]_. It uses a relatively simple technique for first
preprocessing the circuit before the input is known. This preprocessing involves creating a small
table of values for each AND gate, hence the name TinyTables. Online evaluation is then reduced to a
lookup into such a table for each AND gate with minimal communication overhead. As with other
boolean protocol suites, TinyTables utilizes free XOR meaning just local computation must be done to
evaluate each XOR gate. The cost of this preprocessing model is that the application must be known
in advance, and that the parties must be able to communicate during the preprocessing phase.

The protocol suite supports only the two party paradigm, and is malicously secure in theory, but so
far we implemented only the semi-honest version.

.. _SPDZ:

The SPDZ Protocol Suite
-----------------------

The SPDZ suite is based on [DPSZ12]_. It is based on additive secret
sharing over a finite field. The field must have size at least
:math:`2^s` where s is the :term:`statistical security
parameter`. Hence, SPDZ is not suited for Boolean circuits.

SPDZ provides security against a malicious adversary. This suite is
designed to work in the :term:`preprocessing model`. The offline phase
is independent of the circuit to compute (except that a guess at the
number of gates in the circuit must be made in order to produce enough
offline material).

The SPDZ online phase requires a number of rounds equal to the depth
of the circuit to compute, hence best suited for circuits that are not
too deep and in settings with low network latency such as a LAN. It is
well suited to situations where the computation must be fast once the
inputs are known, but where more extensive computation can be done at
some time before the inputs are known, or where a trusted third party
is available at some point before the online computation.

.. note:: Currently, only the *online* phase of SPDZ is
  implemented. FRESCO does, however, contain a method that lets a
  *trusted* party generate the required offline material which makes
  it usable in some settings. The full SPDZ protocol includes a
  protocol that lets the parties obtain the offline material without
  any trusted party.

