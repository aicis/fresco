.. _Quickstart:

Quickstart
==========

This section gives a brief introduction to fresco and the possible
applications of this framework.

Bundled within this repository there are five demos:
* Sum - computes the sum of a number of integers input by party 1.
* Distance - computes the distance between two points (in a euclidian two dimensional space).
* Aggregation - will compute the aggregation of a hardcoded list. The list consists of pairs of (key,value). The demo aggregates all values where the key's match.
* AES - uses AES to encrypt a block of plaintext comming from party 2 using a key provided by party 1.
* PrivateSetIntersection - computes the set intersection between two parties.

The demos shows how to run applications directly from the command line.

If you want to try the frame work, then start by creating an application - it is quite easy to
run an application using the dummy protocol suite. There in distribution already a series of
applications ranging from sum and AES to more complex algorithms expressed by the LP solver
(solving a liniar program).

Every computation with a general purpose is located within a ComputationDirectory, so you can
also build your own application based upon the existing set of computations.

A simple example
----------------
In this example we will explore the effort to use the FRESCO framework in your own application.
FRESCO is a flexible framework intended to be used in your own software stack, so start
by adding the dependency to fresco in your own project.

This example is based on the DistanceDemo - it is very simple by nature. Any application can
be replaced with this.

.. sourcecode:: java

    DistanceDemo distDemo = new DistanceDemo(1, x, y);
    BigInteger modulus = ModulusFinder.findSuitableModulus(64);
    Party me = new Party(1, "localhost", 8871);
    DummyArithmeticProtocolSuite protocolSuite =
        new DummyArithmeticProtocolSuite(modulus, 32);
    SecureComputationEngine<DummyArithmeticResourcePool, ProtocolBuilderNumeric> sce =
        new SecureComputationEngineImpl<>(
            protocolSuite,
            new BatchedProtocolEvaluator<>(new BatchedStrategy<>(), protocolSuite));
    BigInteger bigInteger = sce.runApplication(
        distDemo,
        new DummyArithmeticResourcePoolImpl(1, 1, modulus),
        new KryoNetNetwork(new NetworkConfigurationImpl(1, Collections.singletonMap(1,
            me))));
    double dist = Math.sqrt(bigInteger.doubleValue());


Here we take the existing application, DistanceDemo, and run it with a single party using the
dummy protocol suite. This can run directly in your own tests.

Congratulations on running your first FRESCO application.

If you want to see this run on multiple parties, the above example can be modified to include
two parties running on the same machine.

.. sourcecode:: java

    DistanceDemo distDemo = new DistanceDemo(1, x, y);
    BigInteger modulus = ModulusFinder.findSuitableModulus(64);
    Party partyOne = new Party(1, "localhost", 8871);
    Party partyTwo = new Party(2, "localhost", 8872);
    DummyArithmeticProtocolSuite protocolSuite =
        new DummyArithmeticProtocolSuite(modulus, 32);
    SecureComputationEngine<DummyArithmeticResourcePool, ProtocolBuilderNumeric> sce =
        new SecureComputationEngineImpl<>(
            protocolSuite,
            new BatchedProtocolEvaluator<>(new BatchedStrategy<>(), protocolSuite));
    HashMap<Integer, Party> parties = new HashMap<>();
    parties.put(1, partyOne);
    parties.put(2, partyTwo);
    BigInteger bigInteger = sce.runApplication(
        distDemo,
        new DummyArithmeticResourcePoolImpl(myId, 2, modulus),
        new KryoNetNetwork(new NetworkConfigurationImpl(myId, parties)));
    double dist = Math.sqrt(bigInteger.doubleValue());


From here, the work with MultiPartyComputation becomes somewhat more onvolved - you should
change to an actual protocol suites, here Spdz. This requires a Spdz resource pool which involves
figuring how to handle the offline phase - and hence defining the security model properly.

Use the mailing list for further advise and help.

A Little Explanation
--------------------

Lets have a look at each part of the example.

A FRESCO application implements the ``Application`` interface. To run an
application we must first create a ``SecureComputationEngine``. This is a
core component of FRESCO that is the primary entry point for doing the computations
through the computation directories and the active protocol suite.

The ``SecureComputationEngine`` is initialized with the protocol suite and an aveluator.

To run an application, we also need a ``ResourcePool`` and a ``Network``.
A ResourcePool is controlled by you, the application developer and is a central
database of resources that the suite needs. The network is the interconnected parties, by
default FRESCO uses KryoNet as the network supplier, but you can create your own
and use that if this matches your application better.

When we call ``runApplication`` the ``SecureComputationEngine`` executes the application
and returns the evaluated result directly in a big integer - here the distance between
the two points.

Notice how our ``Application`` is made. Implementing ``Application``
signals that our ``DistanceDemo`` class is a FRESCO application. An application must
also state what it outputs as well as what type of application this is i.e. are
we creating a binary or arithmetic application. This is seen in the interface ::

    public interface Application<OutputT, Builder extends ProtocolBuilder> extends Computation<OutputT, Builder> 

The output type can be anything you want. In our case it is a BigInteger.
The builder type we use is a numeric type since the DistanceDemo computation
works with numeric protocol suites. Since the Application interface extends
the Computation interface, this requires us to implement the method

.. sourcecode:: java

   DRes<BigInteger> buildComputation(ProtocolBuilderNumeric producer)

This is the method that defines how our FRESCO application is built. The DRes
return type is just a delayed result for the output (everything in FRESCO is evaluated "later"
and there can be delayed.
