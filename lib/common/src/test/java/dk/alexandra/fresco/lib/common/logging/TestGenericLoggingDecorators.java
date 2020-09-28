package dk.alexandra.fresco.lib.common.logging;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import dk.alexandra.fresco.framework.ProtocolEvaluator;
import dk.alexandra.fresco.framework.TestThreadRunner;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.builder.numeric.field.BigIntegerFieldDefinition;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.NetworkUtil;
import dk.alexandra.fresco.framework.network.socket.SocketNetwork;
import dk.alexandra.fresco.framework.sce.SecureComputationEngine;
import dk.alexandra.fresco.framework.sce.SecureComputationEngineImpl;
import dk.alexandra.fresco.framework.sce.evaluator.BatchEvaluationStrategy;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedProtocolEvaluator;
import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.HmacDrbg;
import dk.alexandra.fresco.lib.common.compare.CompareTests;
import dk.alexandra.fresco.logging.BatchEvaluationLoggingDecorator;
import dk.alexandra.fresco.logging.EvaluatorLoggingDecorator;
import dk.alexandra.fresco.logging.NetworkLoggingDecorator;
import dk.alexandra.fresco.logging.PerformanceLogger;
import dk.alexandra.fresco.suite.dummy.arithmetic.BasicArithmeticTests;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticProtocolSuite;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticResourcePool;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticResourcePoolImpl;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

public class TestGenericLoggingDecorators {

  private final BigIntegerFieldDefinition fieldDefinition = new BigIntegerFieldDefinition(
      "6703903964971298549787012499123814115273848577471136527425966013026501536706464354255445443244279389455058889493431223951165286470575994074291745908195329");

  @Test
  public void testEvaluatorLoggingDecorator() {
    TestThreadRunner.TestThreadFactory<DummyArithmeticResourcePool, ProtocolBuilderNumeric> f
        = new CompareTests.TestCompareEQ<>();

    EvaluationStrategy evalStrategy = EvaluationStrategy.SEQUENTIAL;
    Map<Integer, NetworkConfiguration> netConf = getNetConf();
    Map<Integer,
        TestThreadRunner.TestThreadConfiguration<
            DummyArithmeticResourcePool,
            ProtocolBuilderNumeric>> conf = new HashMap<>();

    List<PerformanceLogger> decoratedLoggers = new ArrayList<>();
    for (int playerId : netConf.keySet()) {
      NetworkConfiguration partyNetConf = netConf.get(playerId);

      DummyArithmeticProtocolSuite ps = new DummyArithmeticProtocolSuite(fieldDefinition, 200, 16);
      BatchEvaluationStrategy<DummyArithmeticResourcePool> strat = evalStrategy.getStrategy();
      ProtocolEvaluator<DummyArithmeticResourcePool> evaluator
          = new BatchedProtocolEvaluator<>(strat, ps);
      EvaluatorLoggingDecorator<DummyArithmeticResourcePool, ProtocolBuilderNumeric> decoratedEvaluator
          = new EvaluatorLoggingDecorator<>(evaluator);
      SecureComputationEngine<DummyArithmeticResourcePool, ProtocolBuilderNumeric> sce
          = new SecureComputationEngineImpl<>(ps, decoratedEvaluator);
      decoratedLoggers.add(decoratedEvaluator);

      Drbg drbg = new HmacDrbg();
      TestThreadRunner.TestThreadConfiguration<DummyArithmeticResourcePool, ProtocolBuilderNumeric> ttc = new TestThreadRunner.TestThreadConfiguration<>(
          sce, () -> new DummyArithmeticResourcePoolImpl(playerId, netConf.keySet().size(),
          fieldDefinition), () -> new SocketNetwork(partyNetConf));
      conf.put(playerId, ttc);
    }
    TestThreadRunner.run(f, conf);

    PerformanceLogger performanceLogger = decoratedLoggers.get(0);

    Map<String, Long> loggedValues = performanceLogger.getLoggedValues();
    long runningTime = loggedValues.get(EvaluatorLoggingDecorator.SCE_RUNNINGTIMES + 0);
    assertTrue(runningTime > 0);
    performanceLogger.reset();
    loggedValues = performanceLogger.getLoggedValues();
    assertTrue(loggedValues.size() == 0);
  }

  @Test
  public void testNetworkLoggingDecorator() {

    TestThreadRunner.TestThreadFactory<DummyArithmeticResourcePool, ProtocolBuilderNumeric> f
        = new CompareTests.TestCompareLT<>();

    EvaluationStrategy evalStrategy = EvaluationStrategy.SEQUENTIAL;
    Map<Integer, NetworkConfiguration> netConf = getNetConf();
    Map<Integer, TestThreadRunner.TestThreadConfiguration<DummyArithmeticResourcePool, ProtocolBuilderNumeric>> conf =
        new HashMap<>();

    List<PerformanceLogger> decoratedLoggers = Collections.synchronizedList(new ArrayList<>());
    for (int playerId : netConf.keySet()) {
      NetworkConfiguration partyNetConf = netConf.get(playerId);

      DummyArithmeticProtocolSuite ps = new DummyArithmeticProtocolSuite(fieldDefinition, 200, 16);
      BatchEvaluationStrategy<DummyArithmeticResourcePool> strat = evalStrategy.getStrategy();
      ProtocolEvaluator<DummyArithmeticResourcePool> evaluator
          = new BatchedProtocolEvaluator<>(strat, ps);
      SecureComputationEngine<DummyArithmeticResourcePool, ProtocolBuilderNumeric> sce
          = new SecureComputationEngineImpl<>(ps, evaluator);

      TestThreadRunner
          .TestThreadConfiguration<DummyArithmeticResourcePool, ProtocolBuilderNumeric> ttc =
          new TestThreadRunner.TestThreadConfiguration<>(sce,
              () -> new DummyArithmeticResourcePoolImpl(playerId,
                  netConf.keySet().size(), fieldDefinition),
              () -> {
                NetworkLoggingDecorator network = new NetworkLoggingDecorator(
                    new SocketNetwork(partyNetConf));
                decoratedLoggers.add(network);
                return network;
              });
      conf.put(playerId, ttc);
    }
    TestThreadRunner.run(f, conf);

    PerformanceLogger performanceLogger = decoratedLoggers.get(0);

    Map<String, Long> loggedValues = performanceLogger.getLoggedValues();
    assertThat(loggedValues.get(NetworkLoggingDecorator.NETWORK_TOTAL_BYTES), is(130L));
    assertThat(loggedValues.get(NetworkLoggingDecorator.NETWORK_TOTAL_BATCHES), is(2L));
    assertThat(loggedValues.get(NetworkLoggingDecorator.NETWORK_PARTY_BYTES + "_1"), is(130L));
    performanceLogger.reset();

    loggedValues = performanceLogger.getLoggedValues();
    assertThat(loggedValues.get(NetworkLoggingDecorator.NETWORK_TOTAL_BYTES), is(0L));
    assertThat(loggedValues.get(NetworkLoggingDecorator.NETWORK_TOTAL_BATCHES), is(0L));
    assertThat(loggedValues.size(), is(2));
  }

  @Test
  public void testBatchEvaluationLoggingDecorator() {

    TestThreadRunner.TestThreadFactory<DummyArithmeticResourcePool, ProtocolBuilderNumeric> f
        = new BasicArithmeticTests.TestSumAndMult<>();

    EvaluationStrategy evalStrategy = EvaluationStrategy.SEQUENTIAL;

    Map<Integer, NetworkConfiguration> netConf = getNetConf();

    Map<Integer,
        TestThreadRunner
            .TestThreadConfiguration<DummyArithmeticResourcePool, ProtocolBuilderNumeric>> conf =
        new HashMap<>();

    List<PerformanceLogger> decoratedLoggers = new ArrayList<>();
    for (int playerId : netConf.keySet()) {
      NetworkConfiguration partyNetConf = netConf.get(playerId);

      DummyArithmeticProtocolSuite ps = new DummyArithmeticProtocolSuite(fieldDefinition, 200, 16);
      BatchEvaluationStrategy<DummyArithmeticResourcePool> strat = evalStrategy.getStrategy();
      BatchEvaluationLoggingDecorator<DummyArithmeticResourcePool> decoratedStrat =
          new BatchEvaluationLoggingDecorator<>(strat);
      decoratedLoggers.add(decoratedStrat);

      ProtocolEvaluator<DummyArithmeticResourcePool> evaluator
          = new BatchedProtocolEvaluator<>(decoratedStrat, ps);
      SecureComputationEngine<DummyArithmeticResourcePool, ProtocolBuilderNumeric> sce
          = new SecureComputationEngineImpl<>(ps, evaluator);

      TestThreadRunner.TestThreadConfiguration<DummyArithmeticResourcePool, ProtocolBuilderNumeric> ttc =
          new TestThreadRunner.TestThreadConfiguration<>(sce,
              () -> new DummyArithmeticResourcePoolImpl(playerId,
                  netConf.keySet().size(), fieldDefinition),
              () -> new SocketNetwork(partyNetConf));
      conf.put(playerId, ttc);
    }
    TestThreadRunner.run(f, conf);

    PerformanceLogger performanceLogger = decoratedLoggers.get(0);
    Map<String, Long> loggedValues = performanceLogger.getLoggedValues();
    assertThat(loggedValues.get(BatchEvaluationLoggingDecorator.BATCH_COUNTER), is((long) 25));
    assertThat(loggedValues.get(BatchEvaluationLoggingDecorator.BATCH_NATIVE_PROTOCOLS),
        is((long) 45));
    assertThat(loggedValues.get(BatchEvaluationLoggingDecorator.BATCH_MIN_PROTOCOLS), is((long) 1));
    assertThat(loggedValues.get(BatchEvaluationLoggingDecorator.BATCH_MAX_PROTOCOLS),
        is((long) 21));

    performanceLogger.reset();
    loggedValues = performanceLogger.getLoggedValues();
    assertThat(loggedValues.get(BatchEvaluationLoggingDecorator.BATCH_COUNTER), is((long) 0));
    assertThat(loggedValues.get(BatchEvaluationLoggingDecorator.BATCH_NATIVE_PROTOCOLS),
        is((long) 0));
    assertThat(loggedValues.get(BatchEvaluationLoggingDecorator.BATCH_MIN_PROTOCOLS),
        is((long) Integer.MAX_VALUE));
    assertThat(loggedValues.get(BatchEvaluationLoggingDecorator.BATCH_MAX_PROTOCOLS), is((long) 0));
  }

  private Map<Integer, NetworkConfiguration> getNetConf() {
    int noOfParties = 2;
    List<Integer> ports = new ArrayList<>(noOfParties);
    for (int i = 1; i <= noOfParties; i++) {
      ports.add(9000 + i * (noOfParties - 1));
    }
    return NetworkUtil.getNetworkConfigurations(ports);
  }
}
