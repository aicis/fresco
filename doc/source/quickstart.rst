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

    import dk.alexandra.fresco.framework.Application;
    import dk.alexandra.fresco.framework.Protocol;
    import dk.alexandra.fresco.framework.ProtocolProducer;
    import dk.alexandra.fresco.framework.ProtocolFactory;
    import dk.alexandra.fresco.framework.sce.SCE;
    import dk.alexandra.fresco.framework.sce.SCEFactory;
    import dk.alexandra.fresco.framework.sce.configuration.SCEConfiguration;
    import dk.alexandra.fresco.framework.sce.configuration.ProtocolSuiteConfiguration;
    import dk.alexandra.fresco.framework.value.OBool;
    import dk.alexandra.fresco.framework.value.SBool;
    import dk.alexandra.fresco.framework.configuration.CmdLineUtil;
    import dk.alexandra.fresco.framework.util.ByteArithmetic;

    import dk.alexandra.fresco.lib.field.bool.BasicLogicFactory;
    import dk.alexandra.fresco.lib.helper.ParallelProtocolProducer;
    import dk.alexandra.fresco.lib.helper.sequential.SequentialProtocolProducer;
    import dk.alexandra.fresco.lib.crypto.BristolCryptoFactory;


    public class AESDemo implements Application {

        private int myId;
        private boolean[] in;

        private OBool[] out;

        private final static int BLOCK_SIZE = 128; // We do 128 bit AES.

        public AESDemo(int myId, boolean[] input) {
            this.myId = myId;
            this.in = input;
        }

        public static void main(String[] args) throws Exception {

            // Read FRESCO configuration from command line args.
            CmdLineUtil util = new CmdLineUtil();
            util.parse(args);
            SCEConfiguration sceConf = util.getSCEConfiguration();
            ProtocolSuiteConfiguration psConf = util.getProtocolSuiteConfiguration();

	    // Read and parse key or plaintext.
            String in = util.getRemainingArgs()[0];
            boolean[] input = ByteArithmetic.toBoolean(in);

            // Run secure computation.
            AESDemo aes = new AESDemo(sceConf.getMyId(), input);
            SCE sce = SCEFactory.getSCEFromConfiguration(sceConf, psConf);
            sce.runApplication(aes);

            // Print result.
            boolean[] res = new boolean[BLOCK_SIZE];
            for (int i=0; i<BLOCK_SIZE; i++) {
                res[i] = aes.out[i].getValue();
            }
            System.out.println("The resulting ciphertext is: " + ByteArithmetic.toHex(res));

        }

        @Override
        public ProtocolProducer prepareApplication(ProtocolFactory factory) {

            BasicLogicFactory blf = (BasicLogicFactory) factory;

	    // Convert input to open FRESCO values.
            OBool[] plainOpen = new OBool[BLOCK_SIZE];
            OBool[] keyOpen = new OBool[BLOCK_SIZE];
            for (int i=0; i<BLOCK_SIZE; i++) {
                keyOpen[i] = blf.getOBool();
                plainOpen[i] = blf.getOBool();
                if (this.myId == 1) {
                    keyOpen[i].setValue(this.in[i]);
                } else if (this.myId == 2) {
                    plainOpen[i].setValue(this.in[i]);
                }
            }

            // Establish some secure values.
            SBool[] keyClosed = blf.getSBools(BLOCK_SIZE);
            SBool[] plainClosed = blf.getSBools(BLOCK_SIZE);
            SBool[] outClosed = blf.getSBools(BLOCK_SIZE);

            // Build protocol where Alice (id=1) closes his key.
            ProtocolProducer[] closeKeyBits = new ProtocolProducer[BLOCK_SIZE];
            for (int i=0; i<BLOCK_SIZE; i++) {
                closeKeyBits[i] = blf.getCloseProtocol(1, keyOpen[i], keyClosed[i]);
            }
            ProtocolProducer closeKey = new ParallelProtocolProducer(closeKeyBits);

            // Build protocol where Bob (id=2) closes his plaintext.
            ProtocolProducer[] closePlainBits= new ProtocolProducer[BLOCK_SIZE];
            for (int i=0; i<BLOCK_SIZE; i++) {
                closePlainBits[i] = blf.getCloseProtocol(2, plainOpen[i], plainClosed[i]);
            }
            ProtocolProducer closePlain = new ParallelProtocolProducer(closePlainBits);

            // We can close key and plaintext in parallel.
            ProtocolProducer closeKeyAndPlain = new ParallelProtocolProducer(closeKey, closePlain);

            // Build an AES protocol.
            Protocol doAES = new BristolCryptoFactory(blf).getAesCircuit(plainClosed, keyClosed, outClosed);

            // Create wires that glue together the AES to the following open of the result.
            this.out = blf.getOBools(BLOCK_SIZE);

            // Construct protocol for opening up the result.
            Protocol[] opens = new Protocol[BLOCK_SIZE];
            for (int i=0; i<BLOCK_SIZE; i++) {
                opens[i] = blf.getOpenProtocol(outClosed[i], out[i]);
            }
            ProtocolProducer openCipher = new ParallelProtocolProducer(opens);

            // First we close key and plaintext, then we do the AES, then we open the resulting ciphertext.
            ProtocolProducer finalProtocol = new SequentialProtocolProducer(closeKeyAndPlain, doAES, openCipher);

            return finalProtocol;

        }

    }


We are going to assume that you installed FRESCO from source using
Maven. Go to the root directory of the FRESCO project that you checked
out using git, i.e., where you invoked the ``mvn install``. For this
quickstart we will instead build a FRESCO jar that include all FRESCOs
dependencies. So run ::

    mvn clean compile assembly:single

This will create a jar a la

.. parsed-literal::

  target/fresco-|release|-SNAPSHOT-jar-with-dependencies.jar.

Then create a subfolder containing a file called ``AESDemo.java``: ::

    $ mkdir tmp
    $ touch tmp/AESDemo.java

Paste the above Java code into the ``AESDemo.java`` file. Then compile
the file:

.. parsed-literal::

    $ javac -cp target/fresco-|release|-SNAPSHOT-jar-with-dependencies.jar tmp/AESDemo.java

Now we want to execute the secure computation. Open two terminals and
go to the FRESCO project directory in each terminal. Suppose Alice's
128-bit key :math:`k` is 00112233445566778899aabbccddeeff (in
hexadecimal representation). In the first terminal you launch a
computation party for Alice by typing:

.. parsed-literal::

    $ java -cp tmp:target/fresco-|release|-SNAPSHOT-jar-with-dependencies.jar AESDemo -i1 -sdummy -p1:localhost:9001 -p2:localhost:9002 00112233445566778899aabbccddeeff

This starts up the first party (Alice) at port 9001 on localhost. It
will listen for the second party at port 9002 on localhost. Suppose
Bob's 128-bit plaintext :math:`p` is
000102030405060708090a0b0c0d0e0f. In the second terminal you type:

.. parsed-literal::

    $ java -cp tmp:target/fresco-|release|-SNAPSHOT-jar-with-dependencies.jar AESDemo -i2 -sdummy -p1:localhost:9001 -p2:localhost:9002 000102030405060708090a0b0c0d0e0f

This will start Bob at port 9002 and cause the secure computation to
execute, resulting in the following output in both terminals: ::

    The resulting ciphertext is: 69c4e0d86a7b0430d8cdb78070b4c55a



A Little Explanation
--------------------

Lets have a look at each part of the example.

A FRESCO application implements the ``Application`` interface. To run
an application we must first create a *secure computation engine*
(SCE). This is a component of FRESCO that coordinates the
communication between applications and protocol suites.

To create a ``SCE`` we need a ``SCEConfiguration`` and a
``ProtocolSuiteConfiguration``. These are objects that define various
parameters for the computation and the protocol suite. In our case we
use ``CmdLineUtil`` to create these from command line arguments. Once
we have our application ``aes`` and our ``sce``, we simply write:

.. sourcecode:: java

    sce.runApplication(aes);

to launch the secure computation.

Notice how our ``Application`` is made. Implementing ``Application``
signals that our ``AESDemo`` class is a FRESCO application. It
requires us to implement the method

.. sourcecode:: java

   public ProtocolProducer prepareApplication(ProtocolFactory factory)

This is the method that defines how our FRESCO application is
built. In our example we start with simple protocols for closing the
input values. Using the ``SequentialProtocolProducer`` and
``ParallelProtocolProduer`` we then glue together the protocols into
more complex protocols until we arrive at the final
application. [#async]_



In the first line we cast the given ``ProtocolFactory`` to a
``BasicLogicFactory``:

.. sourcecode:: java

    BasicLogicFactory blf = (BasicLogicFactory) factory;

This is a way of stating that we build our application in a generic
way that only requires the protocols provided by a basic logic
factory, namely AND, XOR and NOT protocols. As a consequence, our
application can run *natively* on any protocol suite that supports the
basic logic factory.


Changing the Configuration
--------------------------

Recall that we used the ``CmdLineUtil`` to configure our ``SCE``. The
command line arguments have the following meaning: ::

    -i  The id of this player.
    -s  The name of the protocol suite to use.
    -p  Specifies the host and port of each player.

In our example above we used the :ref:`DUMMY <DUMMY>` suite which
gives no security at all. If you instead want to run using another
suite, simply use the ``-s`` option to change the name.

There are other options as well. You can for example force FRESCO to
evaluate each native protocol in a sequential fashion by using ::

    -e SEQUENTIAL

or you can control the memory footprint of FRESCO by explicitly
setting a limit to the number of native protocols to evaluate in
parallel by using, e.g.,::

    --max-batch-size=2048

Use ``--help`` to get a list of all possible configurations, including
configurations that are specific to each supported protocol suite.

The AES given here, with more error handling, etc., and other demos
can be found in the ``dk.alexandra.fresco.demo`` package in the FRESCO
source code.


.. [#async] Note that we *explicitly* state which parts of the
  computation are done in sequence and which are done in parallel. For
  example, we state that evaluation of the AES circuit should not be
  done until all input values are closed. This is the current way FRESCO
  works. The FRESCO design do allow asynchronous evaluation, but this is
  not currently implemented.
