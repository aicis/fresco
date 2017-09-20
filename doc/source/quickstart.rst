.. _Quickstart:

Quickstart
==========

This section shows you how to quickly run a small application using
FRESCO. If you have not yet installed FRESCO you should first consult
the :ref:`Installation <install>` section.

Your First FRESCO Application
-----------------------------

AES is a commonly used encryption scheme. It transforms a plaintext
:math:`p` and a key :math:`k` into a ciphertext :math:`c`, such that
no-one can learn anything about :math:`p` only by looking at
:math:`c`. Anyone knowing both the ciphertext :math:`c` *and* the key
:math:`k` can, however, easily obtain the original plaintext.

Suppose that we have two parties, *Alice* and *Bob*. Alice holds
:math:`k`, Bob holds :math:`p`, and they are both interested in
computing the encryption :math:`c = \text{AES}_k(p)`. However, Alice
does not want to reveal :math:`k` to Bob and Bob does not want to reveal
:math:`p` to Alice. This is a perfect task for applying secure
computation. We will solve the problem with FRESCO using the following
code:

.. sourcecode:: java
		
  public class AESDemo implements Application<List<Boolean>, ProtocolBuilderBinary> {

    private Boolean[] in;
    private int id;
  
    private final static int BLOCK_SIZE = 128; // 128 bit AES
    private final static int INPUT_LENGTH = 32; // chars for defining 128 bit in hex
  
    public AESDemo(int id, Boolean[] in) {
      this.in = in;
      this.id = id;
    }
  
  
    /**
     * The main method sets up application specific command line parameters, parses command line
     * arguments. Based on the command line arguments it configures the SCE, instantiates the
     * TestAESDemo and runs the TestAESDemo on the SCE.
     */
    public static void main(String[] args) {
      CmdLineUtil util = new CmdLineUtil();
      Boolean[] input = null;
      try {
  
        util.addOption(Option.builder("in")
            .desc("The input to use for encryption. " + "A " + INPUT_LENGTH
                + " char hex string. Required for player 1 and 2. "
                + "For player 1 this is interpreted as the AES key. "
                + "For player 2 this is interpreted as the plaintext block to encrypt.")
            .longOpt("input").hasArg().build());
  
        CommandLine cmd = util.parse(args);
  
        // Get and validate the AES specific input.
        int myId = util.getNetworkConfiguration().getMyId();
        if (myId == 1 || myId == 2) {
          if (!cmd.hasOption("in")) {
            throw new ParseException("Player 1 and 2 must submit input");
          } else {
            if (cmd.getOptionValue("in").length() != INPUT_LENGTH) {
              throw new IllegalArgumentException(
                  "bad input hex string: must be hex string of length " + INPUT_LENGTH);
            }
            input = ByteArithmetic.toBoolean(cmd.getOptionValue("in"));
          }
        } else {
          if (cmd.hasOption("in")) {
            throw new ParseException("Only player 1 and 2 should submit input");
          }
        }
  
      } catch (ParseException | IllegalArgumentException e) {
        System.out.println("Error: " + e);
        System.out.println();
        util.displayHelp();
        System.exit(-1);
      }
  
      // Do the secure computation using config from command line.
      AESDemo aes = new AESDemo(util.getNetworkConfiguration().getMyId(), input);
      ProtocolSuite<ResourcePoolImpl, ProtocolBuilderBinary> ps =
          (ProtocolSuite<ResourcePoolImpl, ProtocolBuilderBinary>) util.getProtocolSuite();
      SecureComputationEngine<ResourcePoolImpl, ProtocolBuilderBinary> sce =
          new SecureComputationEngineImpl<ResourcePoolImpl, ProtocolBuilderBinary>(ps,
              util.getEvaluator());
  
      List<Boolean> aesResult = null;
      try {
        ResourcePoolImpl resourcePool = ResourcePoolHelper.createResourcePool(ps,
            util.getNetworkStrategy(), util.getNetworkConfiguration());
        aesResult = sce.runApplication(aes, resourcePool);
      } catch (Exception e) {
        System.out.println("Error while doing MPC: " + e.getMessage());
        System.exit(-1);
      } finally {
        ResourcePoolHelper.shutdown();
      }
  
      // Print result.
      boolean[] res = new boolean[BLOCK_SIZE];
      for (int i = 0; i < BLOCK_SIZE; i++) {
        res[i] = aesResult.get(i);
      }
      System.out.println("The resulting ciphertext is: " + ByteArithmetic.toHex(res));
    }
  
    //This is the actual application computing the AES function
    @Override
    public DRes<List<Boolean>> buildComputation(ProtocolBuilderBinary producer) {
      return producer.seq(seq -> {
        Binary bin = seq.binary();
        List<DRes<SBool>> keyInputs = new ArrayList<>();
        List<DRes<SBool>> plainInputs = new ArrayList<>();
        if (this.id == 1) {
          for (boolean b : in) {
            keyInputs.add(bin.input(b, 1));
            plainInputs.add(bin.input(false, 2));
          }
        } else {
          // Receive inputs
          for (boolean b : in) {
            keyInputs.add(bin.input(false, 1));
            plainInputs.add(bin.input(b, 2));
          }
        }
        DRes<List<SBool>> res = seq.bristol().AES(plainInputs, keyInputs);
        return res;
      }).seq((seq, aesRes) -> {
        List<DRes<Boolean>> outs = new ArrayList<>();
        for (SBool toOpen : aesRes) {
          outs.add(seq.binary().open(toOpen));
        }
        return () -> outs;
      }).seq((seq, opened) -> {
        return () -> opened.stream().map(DRes::out).collect(Collectors.toList());
      });
    }
  }


We are going to assume that you installed FRESCO from source using Maven. Go to
the root directory of the FRESCO project that you checked out using git. For
this quickstart we will use the AES demonstrator located within 'demos/aes', so
go there. The commands which follows are unix system specific, but Windows users
should be able to easily replicate the steps taken by looking within the
Makefile. Now run ::

    make build

This will create two directories, one for both Alice and Bob. It also creates a
jar a la

.. parsed-literal::

  fresco-demo-aes.jar.

This jar contains the above source file as the main target.

Now we want to execute the secure computation. Open two terminals and go to the
FRESCO AES demo project directory in each terminal. Suppose Alice's 128-bit key
:math:`k` is 00112233445566778899aabbccddeeff (in hexadecimal
representation). In the first terminal you launch a computation party for Alice
by typing:

.. parsed-literal::

    $ java -jar server1/fresco-demo-aes.jar -i1 -sdummyBool -p1:localhost:9001 -p2:localhost:9002 -in 000102030405060708090a0b0c0d0e0f

This starts up the first party (Alice) at port 9001 on localhost. It will listen
for the second party at port 9002 on localhost. Suppose Bob's 128-bit plaintext
:math:`p` is 000102030405060708090a0b0c0d0e0f. In the second terminal you type:

.. parsed-literal::

    $ java -jar server2/fresco-demo-aes.jar -i2 -sdummyBool -p1:localhost:9001 -p2:localhost:9002 -in 00112233445566778899aabbccddeeff

This will start Bob at port 9002 and cause the secure computation to
execute, resulting in the following output in both terminals: ::

    The resulting ciphertext is: 69c4e0d86a7b0430d8cdb78070b4c55a

A quicker way to launch the servers locally is using the Makefile again, by
writing either

.. parsed-literal::

   $ make runDummy

Which will do exactly as the manual method stated above, or:

.. parsed-literal::

   $ make runPrePro
   $ make run

Which runs the exact same code, but tells the command line tool to create an SCE
with the TinyTables protocol suite instead. TinyTables needs to preprocess the
application before being able to run with actual input though, 
    

A Little Explanation
--------------------

Lets have a look at each part of the example.

A FRESCO application implements the ``Application`` interface. To run an
application we must first create a *secure computation engine* (SCE). This is a
core component of FRESCO that coordinates the communication between applications
and protocol suites.

To create a ``SCE`` we need to choose a protocol suite and an evaluator. In our
case we use ``CmdLineUtil`` to create these from command line arguments. To run
an application, we also need a ``ResourcePool``. A ResourcePool is controlled by
you, the application developer, and contains most importantly, the network. By
default FRESCO uses KryoNet as the network supplier, but you can create your own
and use that if this matches your application better. In the demonstrator, the
ResourcePool is created for you based on the command line inputs.

Once we have our application ``aes``, our ``secureComputationEngine`` and the
``ResourcePool``, we simply write:

.. sourcecode:: java

    List<Boolean> aesResult = sce.runApplication(aes, resourcePool);

to launch the secure computation and obtain the output of the application (which
in this case, is the revealed output of the AES computation on the given input
key and plaintext.

Notice how our ``Application`` is made. Implementing ``Application``
signals that our ``AESDemo`` class is a FRESCO application. An application must
also state what it outputs as well as what type of application this is i.e. are
we creating a binary or arithmetic application. This is seen in the interface ::

    public interface Application<OutputT, Builder extends ProtocolBuilder> extends Computation<OutputT, Builder> 

The output type can be anything you want. In our case it is a list of
booleans. The builder type we use is a binary type since the AES computation
works best with binary protocol suites. Since the Application interface extends
the Computation interface, this requires us to implement the method

.. sourcecode:: java

   public DRes<List<Boolean>> buildComputation(ProtocolBuilderBinary producer)

This is the method that defines how our FRESCO application is built. The DRes
return type is just a container for the output type. In our example we start
with basic protocols for closing the input values. Using the closed values, we
then use the bristol description of the AES circuit to compute the AES function
which is then finally output. [#async]_

The preferred method for creating applications is using the lambda expressions
shown in the demonstrator. If you have not used this Java 8 construction before,
it might seem strange, but once you get the hang of it, you might even like
it. Functional language users will hopefully feel right at home. The main idea
is to use the given builder to glue the application together. The builder
contains all of the various functions that FRESCO offers, so look around within
this to discover what options there is. Note that the builder is protocol suite
agnostic and only cares about if the application is binary or arithmetic. This
means that the same application code can be reused for all protocol suites of
the same type.

Changing the Configuration
--------------------------

Recall that we used the ``CmdLineUtil`` to configure our ``SCE``. The
command line arguments have the following meaning: ::

    -i  The id of this player.
    -s  The name of the protocol suite to use.
    -p  Specifies the host and port of each player.

In our example above we used the :ref:`DUMMY_BOOL <DUMMY_BOOL>` suite which
gives no security at all. If you instead want to run using another
suite, simply use the ``-s`` option to change the name.

There are other options as well. You can for example force FRESCO to
evaluate each native protocol in a sequential fashion by using ::

    -e SEQUENTIAL

or you can control the memory footprint of FRESCO by explicitly
setting a limit to the number of native protocols to evaluate in
parallel by using, e.g.,::

    -b 2048

Use ``--help`` to get a list of all possible configurations, including
configurations that are specific to each supported protocol suite.

The AES given here, and other demos can be found in the
demos project folder in the FRESCO source code.


.. [#async] Note that we *explicitly* state which parts of the
  computation are done in sequence and which are done in parallel. For
  example, we state that evaluation of the AES circuit should be
  done before opening the result. This is the current way FRESCO
  works. The FRESCO design do allow asynchronous evaluation, but this is
  not currently implemented.
