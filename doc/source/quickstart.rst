.. _Quickstart:

Quickstart
==========

This section gives a brief introduction on how to start working with FRESCO. If you have further
questions please get in touch using our `issue tracker <https://github.com/aicis/fresco/issues>`_ or
by email at fresco@alexandra.dk.

The best place to start is to browse the demos bundled with the FRESCO repository at
https://github.com/aicis/fresco/tree/master/demos.

The following demos are currently included:

* `Sum <https://github.com/aicis/fresco/tree/master/demos/sum>`_ - computes the sum of a number of
  integers input by a single party.

* `Distance <https://github.com/aicis/fresco/tree/master/demos/distance>`_ - computes the distance
  between two points provided by two different parties (in Euclidean two dimensional space).

* `Aggregation <https://github.com/aicis/fresco/tree/master/demos/aggregation>`_ - computes the aggregation
  of a hard-coded list. The list consists of pairs of (key,value). The demo aggregates all values
  where the keys match.

* `AES <https://github.com/aicis/fresco/tree/master/demos/aes>`_ - computes an AES encryption of a
  block of plain-text provided by one party under a key provided by an other party.

* `Private Set Intersection <https://github.com/aicis/fresco/tree/master/demos/psi>`_ - computes the
  intersection of the private sets of two parties.

Each demo includes instructions on how to build and run them directly on the command line.

The demos should hopefully give you a sense of how secure computation is specified in FRESCO. To get
started on your own applications you should also have a look at the various classes implementing the
``ComputationDirectory`` interface which gathers various generic functionality implemented in FRESCO
which can be combined to realize more complex functionality. Specifically consider ``Numeric``
and ``AdvancedNumeric`` for arithmetic and ``Binary`` and ``AdvancedBinary`` for Boolean based
secure computation.


A Simple Example
----------------

In this example we demonstrate how to use the FRESCO framework in your own application. FRESCO is a
flexible framework intended to be used in your own software stack, so start by adding the dependency
to fresco in your own project.

This example is based on the ``DistanceDemo`` class implementing the **Distance** demo outlined
above. However, essentially any FRESCO application could be substituted for ``DistanceDemo`` in the
following.

.. sourcecode:: java

    DistanceDemo distDemo = new DistanceDemo(1, x, y);
    Party me = new Party(1, "localhost", 8871);
    DummyArithmeticProtocolSuite protocolSuite = new DummyArithmeticProtocolSuite();
    SecureComputationEngine<DummyArithmeticResourcePool, ProtocolBuilderNumeric> sce =
        new SecureComputationEngineImpl<>(
            protocolSuite,
            new BatchedProtocolEvaluator<>(new BatchedStrategy<>(), protocolSuite));
    BigInteger bigInteger = sce.runApplication(
        distDemo,
        new DummyArithmeticResourcePoolImpl(1, 1),
        new KryoNetNetwork(new NetworkConfigurationImpl(1, Collections.singletonMap(1,
            me))));
    double dist = Math.sqrt(bigInteger.doubleValue());


Here we take the existing application, ``DistanceDemo``, and run it with a single party using the
dummy protocol suite. This can run directly in your own tests.

Congratulations on running your first FRESCO application!

If you want to see this run with multiple parties, the above example can be modified to include two
parties running on the same machine.

.. sourcecode:: java

    DistanceDemo distDemo = new DistanceDemo(1, x, y);
    Party partyOne = new Party(1, "localhost", 8871);
    Party partyTwo = new Party(2, "localhost", 8872);
    DummyArithmeticProtocolSuite protocolSuite = new DummyArithmeticProtocolSuite();
    SecureComputationEngine<DummyArithmeticResourcePool, ProtocolBuilderNumeric> sce =
        new SecureComputationEngineImpl<>(
            protocolSuite,
            new BatchedProtocolEvaluator<>(new BatchedStrategy<>(), protocolSuite));
    HashMap<Integer, Party> parties = new HashMap<>();
    parties.put(1, partyOne);
    parties.put(2, partyTwo);
    BigInteger bigInteger = sce.runApplication(
        distDemo,
        new DummyArithmeticResourcePoolImpl(myId, 2),
        new KryoNetNetwork(new NetworkConfigurationImpl(myId, parties)));
    double dist = Math.sqrt(bigInteger.doubleValue());

A Little Explanation
--------------------

Let's have a look at each part of the example above.

A FRESCO application, in this case ``DistanceDemo``, implements the ``Application`` interface. To
run an ``Application`` we must first create a ``SecureComputationEngine``. This is a core component
of FRESCO that is the primary entry point for executing secure computations through the computation
directories and the active protocol suite.

The ``SecureComputationEngine`` is initialized with a ``ProtocolSuite`` and a ``ProtocolEvaluator``
(defining the secure computation technique and strategy for evaluating the application
respectively). In this case we are using the ``DummyArithmeticProtocolSuite`` with the
``BatchedProtocolEvaluator``.
 
To run an ``Application``, we also need a ``ResourcePool`` and a ``Network``. A ``ResourcePool`` is
controlled by you, the application developer and is a central database of resources that the suite
needs. The ``Network`` is the interconnected parties participating in the secure computation. By
default FRESCO uses a ``Network`` implementation based on `KryoNet
<https://github.com/EsotericSoftware/kryonet>`_ as the network supplier, but you can create your own
and use that if this matches your application better.

When we call ``runApplication`` the ``SecureComputationEngine`` executes the application and returns
the evaluated result directly in a ``BigInteger`` - here the distance between the two points.

Notice how our ``Application`` is created. Implementing ``Application`` signals that our
``DistanceDemo`` class is a FRESCO application. An application must also state what it outputs as
well as what type of application this is i.e. are we creating a binary or arithmetic application.
This is seen in the interface

.. sourcecode:: java

    public interface Application<OutputT, Builder extends ProtocolBuilder> extends Computation<OutputT, Builder> 

The output type can be anything you want. In our case it is a ``BigInteger``. The builder type we
use here is a numeric type since the ``DistanceDemo`` computation works with numeric protocol
suites. Since the ``Application`` interface extends the ``Computation`` interface, this requires us
to implement the method

.. sourcecode:: java

   DRes<BigInteger> buildComputation(ProtocolBuilderNumeric producer)

This is the method that defines how our FRESCO application is built. The ``DRes`` return type
represents a deferred result for the output (modeling that everything in FRESCO is evaluated
"later").
