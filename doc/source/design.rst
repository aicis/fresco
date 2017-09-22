
The Design of FRESCO
====================

The overall goal of FRESCO is to support rapid and easy prototyping of
secure computation protocols. In particular, FRESCO decouples
applications from protocol suites in order to obtain a *plug-and-play*
system. To achieve this we apply a couple of nifty *design patterns*:


The Protocol Producer/Consumer Pattern
--------------------------------------

A central concept in FRESCO is that of a *protocol*. This is something
that can be evaluated by the computation parties in order to obtain
some desirable state. For example, we can have a multiplication
protocol that can securely multiply two secret numbers. But a protocol
can also be more complex, e.g., a protocol that securely computes the
intersection of two sets.

FRESCO also supports a number of *values*. These essentially play the
same role as data types in ordinary programming languages. Common
examples of values are integers and booleans. Values can be either
*open* or *closed*.  Each protocol has some input values and some
output values. A multiplication protocol would have two input values,
i.e., the two numbers to multiply, and one output value for the
result. The output values of one protocol can be the input value of
other protocols. Thus, in general this forms a directed acyclic graph
(DAG) of protocols glued together by values.

In FRESCO we have *protocol producers*. Each producer is responsibile
for building the full DAG of protocols that corresponds to some
task. But in order to avoid having the full DAG in memory, we require
that protocols can be requrested from the producer in small, manageble
*batches*.

A protocol suite acts as a *consumer* of protocols. It knows how to
evaluate a small set of protocols. The :ref:`SPDZ <SPDZ>` protocol suite, for
example, knows how to evaluate gates that performs addition and
multiplication over a finite field: We say that finite field
multiplication and addition are *natively* supported by the SPDZ
suite. Any protocol producer that produces only these two kinds of
gates is said to be *compatible* with the SPDZ suite.

FRESCO offers a :ref:`standard library <STD_LIB>` of tools that can convert a
protocol producer producing some kinds of protocols, e.g., comparison
protocols and sorting protocols, into a producer of more basic
protocols, such as multiplication and addition protocols. This allows
you to easily write high-level producers that produce complex
protocols and then use the standard library to turn this into a
producer of simpler, native gates that can be evaluated by one or more
protocol suites.

The producer/consumer pattern offers great flexibility. It allows us
to use different producer strategies. In FRESCO producers can
currently be written by hand, or they can be instantiated from a fixed
circuit description read from disk. But one could also imagine a
producer that is based on interpreting some kind of secure computing
language.

We can also have different consumer patterns. Essentially, each
protocol suite is a unique producer pattern with its own security and
performance characteristics. In addition FRESCO allows to use
different *evaluator strategies*. For example, you can choose a fully
sequential evaluator strategy. This strategy evaluates one native
protocol at a time. This is slow, but requires only one thread and may
be good for debugging. You can also choose strategies that evaluates
gates in parallel, e.g., in order to utilize multiple cores. [#foo]_



The Abstract Factory Pattern
----------------------------

FRESCO encourages a style where applications are written to run on
many different protocol suites. The producer/consumer pattern
decouples applications from suites, but to also obtain decoupling with
respect to the *types* we use the abstract factory
pattern. Concretely, this means that FRESCO promotes a style where
your application is *based on* some set of abstracts protocol
factories. Instead of writing your application using, e.g., a SPDZ
protocol factory, you should write it such that it relies on an
abstract factory for secure arithmetic protocols. 

Suppose that your application produces set intersection
protocols. When you want to run your application, you can either use a
protocol suite that *natively* supports such a protocol. But the idea
of FRESCO is that you can also run your application using a suite that
supports only multiplication and addition over a finite field, by
applying a generic set intersection protocol from the standard library
that is itself only based on a basic arithmetic factory.

We have in FRESCO taken this concept a bit further. We also introduced the
concept of *builders*. A builder resembles the ``BigInteger`` class in style and
use. Their methods work on either binary or arithmetic types and produces the
result of a given function as the return type. This makes it possible to write
really concise expressions which are easy to read. Examples can be found in any
test within the framework or here in the documentation in the
:ref:`Quickstart<Quickstart>` section. Such a builder is given to the
application developer when implementing the FRESCO ``Application``
interface. The builder currently comes in two variants: binary and
arithmetic. However, you can extend these with your own functionality or even
create your own builder if need be.


Related Open Source Projects
----------------------------

We are currently aware of the following open source frameworks that
are more or less related to FRESCO:

* `VIFF <http://viff.dk>`_ is written in Python and licensed under
  LGPL. It has some of the same ideas as FRESCO, in particular, that
  applications are decoupled from protocol suites (which are called
  *runtimes* in VIFF). VIFF is based on the Twisted framework and
  supports an asynchronous evaluation strategy.

* `SCAPI <http://github.com/cryptobiu/scapi>`_ is written in Java and
  licensed under MIT as FRESCO. It binds to several C/C++ libraries for
  efficient cryptographic and number theoretic operations. SCAPI is
  intended for easy prototyping of many cryptographic protocols,
  including protocols for secure computing. Compared to SCAPI the FRESCO
  has more focus on reusing applications across different protocol
  suites.

If you think something is missing in the above list, send an email to
fresco@alexandra.dk or send us a pull request with an update as
described in :ref:`contributing`.


.. [#foo] A given protocol suite may not support all available
   evaluation strategies. Protocol suites where there is a high degree
   of dependence on shared resources when evaluating native protocols
   require special attention. In the extreme, such a protocol suite
   needs its own specific evaluator strategy. On the other hand,
   suites where native protocols can be evaluated without any
   dependencies can use any available strategy.
