package dk.alexandra.fresco.suite.spdz;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.NativeProtocol.EvaluationStatus;
import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.ProtocolEvaluator;
import dk.alexandra.fresco.framework.TestThreadRunner;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.builder.numeric.field.BigIntegerFieldDefinition;
import dk.alexandra.fresco.framework.builder.numeric.field.FieldElement;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.NetworkUtil;
import dk.alexandra.fresco.framework.network.socket.SocketNetwork;
import dk.alexandra.fresco.framework.sce.SecureComputationEngine;
import dk.alexandra.fresco.framework.sce.SecureComputationEngineImpl;
import dk.alexandra.fresco.framework.sce.evaluator.BatchEvaluationStrategy;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedProtocolEvaluator;
import dk.alexandra.fresco.framework.sce.evaluator.NetworkBatchDecorator;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import dk.alexandra.fresco.framework.util.OpenedValueStoreImpl;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.ProtocolSuiteNumeric;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzInputMask;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzTriple;
import dk.alexandra.fresco.suite.spdz.storage.SpdzDummyDataSupplier;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Various tests to verify that the implementation of SPDZ follows the protocol as described in the
 * paper Damgård et.al, "Multiparty Computation from Somewhat Homomorphic Encryption".
 */
public class SpdzProtocolTest {

  private static final int P = 251;
  private static final int DEFAULT_MAX_BIT_LENGTH = 8;

  // Input of X from party 1
  private static final int X = 10;

  // Input of Y from party 2
  private static final int Y = 5;

  private static void runTest(
      TestThreadFactory<SpdzResourcePool, ProtocolBuilderNumeric> f) {

    ProtocolTestContext context = new ProtocolTestContext(P, 123);

    // Preprocessing. Sample masks and multiplication triple
    SecretSharedValue mask1 = context.sample();
    SecretSharedValue mask2 = context.sample();

    SecretSharedValue a = context.sample();
    SecretSharedValue b = context.sample();
    SecretSharedValue c = context.share(context.mod(a.value * b.value));
    MultiplicationTriple triple = new MultiplicationTriple(a, b, c);

    /* ==================================== */
    /* SIMULATE PROTOCOL TO COMPUTE (X+Y)*X */
    /* ==================================== */

    /* Input X from party 1 */
    SecretSharedValue xInput = context.input(X, mask1);

    /* Input Y from party 2 */
    SecretSharedValue yInput = context.input(Y, mask2);

    /* Compute sum = X + Y */
    SecretSharedValue sum = xInput.add(yInput);

    /* Compute X * sum */
    SecretSharedValue product = xInput.multiply(sum, triple);

    // Collect each parties expected intermediate values in lists
    Pair<LinkedList<Pair<Integer, Integer>>, LinkedList<Pair<Integer, Integer>>> intermediateValues =
        expected(xInput, yInput, sum, product);
    LinkedList<Pair<Integer, Integer>> intermediateValues1 = intermediateValues.getFirst();
    LinkedList<Pair<Integer, Integer>> intermediateValues2 = intermediateValues.getSecond();

    /* OPEN PRODUCT */
    intermediateValues1.add(new Pair<>(product.value, 0));
    intermediateValues2.add(new Pair<>(product.value, 0));

    List<Integer> ports = new ArrayList<>(2);
    ports.add(9001);
    ports.add(9002);

    Map<Integer, NetworkConfiguration> netConf =
        NetworkUtil.getNetworkConfigurations(ports);
    Map<Integer, TestThreadRunner.TestThreadConfiguration<SpdzResourcePool, ProtocolBuilderNumeric>> conf =
        new HashMap<>();

    for (int playerId : netConf.keySet()) {
      LinkedList<Pair<Integer, Integer>> inputs =
          playerId == 1 ? intermediateValues1 : intermediateValues2;
      ProtocolSuiteNumeric<SpdzResourcePool> protocolSuite = new SpdzProtocolSuite(
          SpdzProtocolTest.DEFAULT_MAX_BIT_LENGTH);

      BatchEvaluationStrategy<SpdzResourcePool> batchEvalStrat =
          new DebugSequentialStrategy<>(protocol -> {
            synchronized (SpdzProtocolTest.class) {
              Assert.assertFalse(inputs.isEmpty());

              Pair<Integer, Integer> expected = inputs.pollFirst();
              if (protocol.out() instanceof SpdzSInt) {
                SpdzSInt out = (SpdzSInt) protocol.out();
                Assert.assertEquals(expected.getFirst().intValue(),
                    context.fieldDefinition.convertToUnsigned(out.getShare()).intValue());
                Assert.assertEquals(expected.getSecond().intValue(),
                    context.fieldDefinition.convertToUnsigned(out.getMac()).intValue());
              } else if (protocol.out() instanceof BigInteger) {
                BigInteger out = (BigInteger) protocol.out();
                Assert.assertEquals(expected.getFirst().intValue(), out.intValue());
              }
            }
          });

      ProtocolEvaluator<SpdzResourcePool> evaluator =
          new BatchedProtocolEvaluator<>(batchEvalStrat, protocolSuite);

      SecureComputationEngine<SpdzResourcePool, ProtocolBuilderNumeric> sce =
          new SecureComputationEngineImpl<>(protocolSuite, evaluator);

      TestThreadRunner.TestThreadConfiguration<SpdzResourcePool, ProtocolBuilderNumeric> ttc =
          new TestThreadRunner.TestThreadConfiguration<>(sce,
              () -> createResourcePool(playerId, context, mask1, mask2, triple),
              () -> new SocketNetwork(netConf.get(playerId)));
      conf.put(playerId, ttc);
    }
    TestThreadRunner.run(f, conf);

    // Assert that all intermediate values has been checked
    Assert.assertTrue(intermediateValues1.isEmpty());
    Assert.assertTrue(intermediateValues2.isEmpty());
  }

  private static SpdzResourcePool createResourcePool(int myId, ProtocolTestContext context,
      SecretSharedValue mask1,
      SecretSharedValue mask2, MultiplicationTriple triple) {

    // Mock resource pool to provide the predefined triples and masks

    SpdzDummyDataSupplier supplier = mock(SpdzDummyDataSupplier.class);
    int sharedKey = context.alpha[myId - 1];

    if (myId == 1) {
      when(supplier.getNextInputMask(1)).thenReturn(
          new SpdzInputMask(mask1.asSpdzSInt(myId),
              context.asFieldElement(mask1.value)));
      when(supplier.getNextInputMask(2)).thenReturn(
          new SpdzInputMask(mask2.asSpdzSInt(myId)));
    } else {
      when(supplier.getNextInputMask(1)).thenReturn(
          new SpdzInputMask(mask1.asSpdzSInt(myId)));
      when(supplier.getNextInputMask(2)).thenReturn(
          new SpdzInputMask(mask2.asSpdzSInt(myId),
              context.asFieldElement(mask2.value)));
    }

    when(supplier.getSecretSharedKey()).thenReturn(context.asFieldElement(sharedKey));
    when(supplier.getFieldDefinition()).thenReturn(context.fieldDefinition);
    when(supplier.getNextTriple()).thenReturn(new SpdzTriple(
        triple.a.asSpdzSInt(myId), triple.b.asSpdzSInt(myId),
        triple.c.asSpdzSInt(myId)));

    return new SpdzResourcePoolImpl(myId, 2, new OpenedValueStoreImpl<>(), supplier,
        AesCtrDrbg::new);
  }

  public static Pair<LinkedList<Pair<Integer, Integer>>, LinkedList<Pair<Integer, Integer>>> expected(
      SecretSharedValue... values) {
    LinkedList<Pair<Integer, Integer>> party1 = Arrays.stream(values)
        .map(value -> new Pair<>(value.shares[0], value.macs[0])).collect(
            Collectors.toCollection(LinkedList::new));
    LinkedList<Pair<Integer, Integer>> party2 = Arrays.stream(values)
        .map(value -> new Pair<>(value.shares[1], value.macs[1])).collect(
            Collectors.toCollection(LinkedList::new));
    return new Pair<>(party1, party2);
  }

  @Ignore
  @Test
  public void testProtocolImplementation() {
    runTest(new TestThreadFactory<SpdzResourcePool, ProtocolBuilderNumeric>() {
      @Override
      public TestThread<SpdzResourcePool, ProtocolBuilderNumeric> next() {
        BigInteger x = BigInteger.valueOf(X);
        BigInteger y = BigInteger.valueOf(Y);
        return new TestThread<SpdzResourcePool, ProtocolBuilderNumeric>() {
          @Override
          public void test() {
            Application<BigInteger, ProtocolBuilderNumeric> app = producer -> {
              Numeric numeric = producer.numeric();

              DRes<SInt> xClosed = numeric.input(x, 1);
              DRes<SInt> yClosed = numeric.input(y, 2);

              DRes<SInt> sum = numeric.add(xClosed, yClosed);
              DRes<SInt> product = numeric.mult(xClosed, sum);

              return numeric.open(product);
            };
            BigInteger output = runApplication(app);

            Assert.assertEquals(x.add(y).multiply(x), output);
          }
        };
      }
    });
  }

  /**
   * Run as a sequential strategy, but for each processed protocol, a consumer is called after the
   * protocol has been processed..
   */
  private static class DebugSequentialStrategy<ResourcePoolT extends ResourcePool> implements
      BatchEvaluationStrategy<ResourcePoolT> {

    private final Consumer<NativeProtocol<?, ResourcePoolT>> consumer;

    public DebugSequentialStrategy(Consumer<NativeProtocol<?, ResourcePoolT>> consumer) {
      this.consumer = consumer;
    }

    @Override
    public void processBatch(
        ProtocolCollection<ResourcePoolT> protocols, ResourcePoolT resourcePool,
        NetworkBatchDecorator networkBatchDecorator) {
      for (NativeProtocol<?, ResourcePoolT> protocol : protocols) {
        int round = 0;
        EvaluationStatus status;
        do {
          status = protocol.evaluate(round, resourcePool, networkBatchDecorator);
          // send phase
          networkBatchDecorator.flush();
          round++;
        } while (status.equals(EvaluationStatus.HAS_MORE_ROUNDS));
      }

      protocols.forEach(consumer);
    }

  }

  private static class ProtocolTestContext {

    private final int[] alpha;
    private final BigIntegerFieldDefinition fieldDefinition;
    private final int p;
    private final Random random;

    /** Create a new test context with a modulus p and a prng seed. */
    private ProtocolTestContext(int p, int seed) {
      this.p = p;
      this.random = new Random(seed);
      this.alpha = new int[2];
      alpha[0] = random.nextInt(p);
      alpha[1] = random.nextInt(p);
      this.fieldDefinition = new BigIntegerFieldDefinition(BigInteger.valueOf(p));
    }

    /** Compute x mod p */
    private int mod(int x) {
      return Math.floorMod(x, p);
    }

    /** Sample a new random secret shared value */
    private SecretSharedValue sample() {
      return new SecretSharedValue(this, random.nextInt(p));
    }

    /** Share a given value */
    private SecretSharedValue share(int value) {
      return new SecretSharedValue(this, value);
    }

    /** Input a value using a specified input mask */
    private SecretSharedValue input(int value, SecretSharedValue mask) {
      return new SecretSharedValue(this, value, mask);
    }

    /** Return x as a {@link FieldElement}. */
    private FieldElement asFieldElement(int x) {
      return fieldDefinition.createElement(x);
    }
  }

  /**
   * Instances of this class represents a local representation of a secret shared value in SPDZ
   * shared among two parties, each having a share and a MAC.
   */
  private static class SecretSharedValue {

    /** The actual value, eg. the sum of the shares. */
    private final int value;

    /** The shares. Party 1 holds shares[0] and party 2 holds shares[1]. */
    private final int[] shares;

    /** The macs. Party 1 holds macs[0] and party 2 holds macs[1]. */
    private final int[] macs;

    private final ProtocolTestContext context;

    private SecretSharedValue(ProtocolTestContext preprocessed, int[] shares, int[] macs) {
      this.shares = shares;
      this.macs = macs;
      this.value = preprocessed.mod(Arrays.stream(shares).sum());
      this.context = preprocessed;
    }

    /** Create a new random sharing a given value */
    private SecretSharedValue(ProtocolTestContext context, int value) {
      this.value = value;
      this.shares = new int[2];
      shares[0] = context.random.nextInt(context.p);
      shares[1] = context.mod(value - shares[0]);
      this.macs = new int[]{context.mod(value * context.alpha[0]),
          context.mod(value * context.alpha[1])};
      this.context = context;
    }

    private SecretSharedValue(ProtocolTestContext context, int value, SecretSharedValue mask) {
      this.value = value;
      this.context = context;

      int epsilon = context.mod(value - mask.value);
      SecretSharedValue masked = mask.add(epsilon);
      this.shares = masked.shares;
      this.macs = masked.macs;
    }

    /** Return this value as an {@link dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt} for a given party */
    private SpdzSInt asSpdzSInt(int myId) {
      return new SpdzSInt(context.asFieldElement(shares[myId - 1]),
          context.asFieldElement(macs[myId - 1]));
    }

    /** Add two secret shared values */
    private SecretSharedValue add(SecretSharedValue other) {
      int[] newShares = {context.mod(shares[0] + other.shares[0]),
          context.mod(shares[1] + other.shares[1])};
      int[] newMacs = {context.mod(macs[0] + other.macs[0]),
          context.mod(macs[1] + other.macs[1])};
      return new SecretSharedValue(context, newShares, newMacs);
    }

    /** Subtract two secret shared values */
    private SecretSharedValue subtract(SecretSharedValue other) {
      int[] newShares = {context.mod(shares[0] - other.shares[0]),
          context.mod(shares[1] - other.shares[1])};
      int[] newMacs = {context.mod(macs[0] - other.macs[0]),
          context.mod(macs[1] - other.macs[1])};
      return new SecretSharedValue(context, newShares, newMacs);
    }

    /** Add a public value to a secret shared value */
    private SecretSharedValue add(int value) {
      int[] newShares = {context.mod(value + shares[0]), shares[1]};
      int[] newMacs = {context.mod(macs[0] + value * context.alpha[0]),
          context.mod(macs[1] + value * context.alpha[1])};
      return new SecretSharedValue(context, newShares, newMacs);
    }

    /** Mulitply a secret shared value by a public value */
    private SecretSharedValue multiply(int value) {
      int[] newShares = {context.mod(shares[0] * value),
          context.mod(shares[1] * value)};
      int[] newMacs = {context.mod(macs[0] * value),
          context.mod(macs[1] * value)};
      return new SecretSharedValue(context, newShares, newMacs);
    }

    /** Multiply two secret shared values using the given multiplication triple */
    private SecretSharedValue multiply(SecretSharedValue other, MultiplicationTriple triple) {

      SecretSharedValue e = this.subtract(triple.a);
      SecretSharedValue d = other.subtract(triple.b);

      // Open epsilon and delta to all parties
      int epsilon = e.value;
      int delta = d.value;

      return triple.c.add(triple.b.multiply(epsilon))
          .add(triple.a.multiply(delta)).add(epsilon * delta);
    }
  }

  private static class MultiplicationTriple {

    private final SecretSharedValue a, b, c;

    private MultiplicationTriple(SecretSharedValue a, SecretSharedValue b, SecretSharedValue c) {
      this.a = a;
      this.b = b;
      this.c = c;
    }
  }
}
