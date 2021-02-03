package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.ProtocolEvaluator;
import dk.alexandra.fresco.framework.TestThreadRunner;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.builder.numeric.field.MersennePrimeFieldDefinition;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.NetworkUtil;
import dk.alexandra.fresco.framework.network.CloseableNetwork;
import dk.alexandra.fresco.framework.network.socket.SocketNetwork;
import dk.alexandra.fresco.framework.sce.SecureComputationEngine;
import dk.alexandra.fresco.framework.sce.SecureComputationEngineImpl;
import dk.alexandra.fresco.framework.sce.evaluator.BatchEvaluationStrategy;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedProtocolEvaluator;
import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import dk.alexandra.fresco.framework.util.OpenedValueStoreImpl;
import dk.alexandra.fresco.suite.ProtocolSuiteNumeric;
import dk.alexandra.fresco.suite.dummy.arithmetic.BasicArithmeticTests.TestLotsMult;
import dk.alexandra.fresco.suite.spdz.storage.SpdzDataSupplier;
import dk.alexandra.fresco.suite.spdz.storage.SpdzDummyDataSupplier;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.apache.commons.math3.distribution.PoissonDistribution;
import org.apache.commons.math3.stat.inference.ChiSquareTest;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Abstract class which handles a lot of boiler plate testing code. This makes running a single test
 * using different parameters quite easy.
 */
public class SpdzBirthdayTest {

  private static final int DEFAULT_MOD_BIT_LENGTH = 32;
  private static final int DEFAULT_MOD_BYTE_LENGTH = DEFAULT_MOD_BIT_LENGTH / 8;
  private static final int DEFAULT_MAX_BIT_LENGTH = 20;
  private static final int DEFAULT_NUMBER_OF_PARTIES = 2;
  private static final EvaluationStrategy DEFAULT_EVAL_STRATEGY = EvaluationStrategy.SEQUENTIAL_BATCHED;

  private static void runTest(
      TestThreadRunner.TestThreadFactory<SpdzResourcePool, ProtocolBuilderNumeric> f) {

    List<Integer> ports = new ArrayList<>(DEFAULT_NUMBER_OF_PARTIES);
    for (int i = 1; i <= DEFAULT_NUMBER_OF_PARTIES; i++) {
      ports.add(9000 + i * (DEFAULT_NUMBER_OF_PARTIES - 1));
    }

    Map<Integer, NetworkConfiguration> netConf =
        NetworkUtil.getNetworkConfigurations(ports);
    Map<Integer, TestThreadRunner.TestThreadConfiguration<SpdzResourcePool, ProtocolBuilderNumeric>> conf =
        new HashMap<>();
    List<byte[]> payloads = new LinkedList<>();
    for (int playerId : netConf.keySet()) {

      ProtocolSuiteNumeric<SpdzResourcePool> protocolSuite = createProtocolSuite(
      );
      BatchEvaluationStrategy<SpdzResourcePool> batchEvalStrat = DEFAULT_EVAL_STRATEGY
          .getStrategy();

      ProtocolEvaluator<SpdzResourcePool> evaluator =
          new BatchedProtocolEvaluator<>(batchEvalStrat, protocolSuite);

      SecureComputationEngine<SpdzResourcePool, ProtocolBuilderNumeric> sce =
          new SecureComputationEngineImpl<>(protocolSuite, evaluator);

      TestThreadRunner.TestThreadConfiguration<SpdzResourcePool, ProtocolBuilderNumeric> ttc =
          new TestThreadRunner.TestThreadConfiguration<>(sce, () -> createResourcePool(playerId),
              () -> new DebugSocketNetwork(new SocketNetwork(netConf.get(playerId)), (id, m) -> {
                synchronized (SpdzBirthdayTest.class) {
                  if (m[0] != DEFAULT_MOD_BYTE_LENGTH) {
                    return;
                  }

                  // First 4 bytes from all elements in list
                  for (int i = 0; i < m.length; i += DEFAULT_MOD_BYTE_LENGTH + 1) {
                    payloads.add(Arrays.copyOfRange(m, i + 2, i + 1 + DEFAULT_MOD_BYTE_LENGTH));
                  }
                }
              }, (id, m) -> {

              }));
      conf.put(playerId, ttc);
    }
    TestThreadRunner.run(f, conf);

    // Birthday spacings test
    List<BigInteger> values = new ArrayList<>();
    for (byte[] payload : payloads) {
      values.add(new BigInteger(1, payload));
    }
    values = values.stream().distinct().collect(Collectors.toList());

    double p = new BirthdaySpacings().birthdaySpacingsTest(values, 4.0, 24);
    Assert.assertTrue(p > 0.05);
  }

  protected static SpdzProtocolSuite createProtocolSuite() {
    return new SpdzProtocolSuite(DEFAULT_MAX_BIT_LENGTH);
  }

  private static SpdzResourcePool createResourcePool(int myId) {
    MersennePrimeFieldDefinition definition = MersennePrimeFieldDefinition
        .find(DEFAULT_MOD_BIT_LENGTH);
    SpdzDataSupplier supplier = new SpdzDummyDataSupplier(myId, 2, definition,
        new BigInteger(DEFAULT_MOD_BIT_LENGTH, new Random(myId)).mod(definition.getModulus()));
    return new SpdzResourcePoolImpl(myId, SpdzBirthdayTest.DEFAULT_NUMBER_OF_PARTIES,
        new OpenedValueStoreImpl<>(), supplier,
        AesCtrDrbg::new);
  }

  @Ignore
  @Test
  public void runRandomnessTest() {
    runTest(new TestLotsMult<>());
  }

  /**
   * Debug wrapper class for SocketNetworks. The send- and receive consumers are notified when a
   * message has been sent or received over the underlying SocketNetwork.
   */
  private static class DebugSocketNetwork implements CloseableNetwork {

    private final CloseableNetwork network;
    private final BiConsumer<Integer, byte[]> sendConsumer;
    private final BiConsumer<Integer, byte[]> receiveConsumer;

    public DebugSocketNetwork(CloseableNetwork network, BiConsumer<Integer, byte[]> sendConsumer,
        BiConsumer<Integer, byte[]> receiveConsumer) {
      this.network = network;
      this.sendConsumer = sendConsumer;
      this.receiveConsumer = receiveConsumer;
    }

    @Override
    public void send(int partyId, byte[] data) {
      sendConsumer.accept(partyId, data);
      network.send(partyId, data);
    }

    @Override
    public byte[] receive(int partyId) {
      byte[] payload = network.receive(partyId);
      receiveConsumer.accept(partyId, payload);
      return payload;
    }

    @Override
    public int getNoOfParties() {
      return network.getNoOfParties();
    }

    @Override
    public void close() throws IOException {
      network.close();
    }
  }

  /**
   * Randomness test from "Some difficult-to-pass tests of randomness" by George Marsaglia and Wai
   * Wan Tsang
   */
  private static class BirthdaySpacings {

    /**
     * Test the p-value for the Birthday spacings test applied on the given numbers in the interval
     * [0, 2^nBits).
     */
    private double birthdaySpacingsTest(List<BigInteger> rng, double lambda, int nBits) {
      return birthdaySpacingsTest(rng, lambda, BigInteger.ONE.shiftLeft(nBits));
    }

    private double birthdaySpacingsTest(List<BigInteger> rng, double lambda, BigInteger n) {
      int m = (int) Math
          .round(Math.pow(n.multiply(BigInteger.valueOf(4)).doubleValue() * lambda, 1.0 / 3.0));
      int observations = rng.size() / m;
      return birthdaySpacingsTest(rng.iterator()::next, lambda, n, observations);
    }

    private double birthdaySpacingsTest(Supplier<BigInteger> rng, double lambda, BigInteger n,
        int observations) {
      int m = (int) Math
          .round(Math.pow(n.multiply(BigInteger.valueOf(4)).doubleValue() * lambda, 1.0 / 3.0));
      return birthdaySpacingsTest(rng, m, n, observations);
    }

    private double birthdaySpacingsTest(Supplier<BigInteger> rng, int m, BigInteger n,
        int observations) {
      BigInteger[] birthdays = new BigInteger[m];

      int[] observed = new int[observations];
      double lambda = BigDecimal
          .valueOf(m).pow(3)
          .divide(new BigDecimal(n.multiply(BigInteger.valueOf(4))), RoundingMode.HALF_UP)
          .doubleValue();

      // For each k, we compute the number of duplicates among the spacings of the
      // random values. These are supposed to be Poisson distributed with mean lambda, and we get the p-value
      // as the Chi squared test of the hypothesis that this is the case.
      for (int k = 0; k < observations; k++) {
        for (int i = 0; i < m; i++) {
          birthdays[i] = rng.get();
        }

        Arrays.sort(birthdays);

        BigInteger[] d = new BigInteger[m - 1];
        for (int i = 1; i < m; i++) {
          d[i - 1] = birthdays[i].subtract(birthdays[i - 1]);
        }

        int duplicates = 0;
        Arrays.sort(d);
        for (int i = 1; i < d.length; i++) {
          if (d[i - 1].equals(d[i])) {
            duplicates++;
            while (i < d.length - 1 && d[i + 1].equals(d[i])) {
              i++;
            }
          }
        }
        observed[k] = duplicates;
      }

      PoissonDistribution expectedDistribution = new PoissonDistribution(lambda);
      ChiSquareTest test = new ChiSquareTest();

      int max = Arrays.stream(observed).max().getAsInt();
      long[] data = new long[max];
      double[] expected = new double[max];
      for (int i = 0; i < max; i++) {
        int finalI = i;
        data[i] = Arrays.stream(observed).filter(j -> j == finalI).count();
        expected[i] = expectedDistribution.probability(i) * observed.length;
      }

      return test.chiSquareTest(expected, data);
    }

  }

}
