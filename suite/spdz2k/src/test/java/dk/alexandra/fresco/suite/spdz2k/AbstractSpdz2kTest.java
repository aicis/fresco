package dk.alexandra.fresco.suite.spdz2k;

import dk.alexandra.fresco.framework.ProtocolEvaluator;
import dk.alexandra.fresco.framework.TestThreadRunner;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.NetworkTestUtils;
import dk.alexandra.fresco.framework.network.AsyncNetwork;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.SecureComputationEngine;
import dk.alexandra.fresco.framework.sce.SecureComputationEngineImpl;
import dk.alexandra.fresco.framework.sce.evaluator.BatchEvaluationStrategy;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedProtocolEvaluator;
import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.suite.ProtocolSuiteNumeric;
import dk.alexandra.fresco.suite.spdz2k.resource.Spdz2kResourcePool;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public abstract class AbstractSpdz2kTest<Spdz2kResourcePoolT extends Spdz2kResourcePool<?>> {

  private final List<Integer> partyNumbers = Arrays.asList(2, 3);

  void runTest(
      TestThreadRunner.TestThreadFactory<Spdz2kResourcePoolT, ProtocolBuilderNumeric> f,
      EvaluationStrategy evalStrategy) {
    for (Integer numberOfParties : partyNumbers) {
      runTest(f, evalStrategy, numberOfParties);
    }
  }

  protected void runTest(
      TestThreadFactory<Spdz2kResourcePoolT, ProtocolBuilderNumeric> f,
      EvaluationStrategy evalStrategy, int noOfParties) {
    List<Integer> ports = NetworkTestUtils.getFreePorts(2 * noOfParties);
    Map<Integer, NetworkConfiguration> netConf =
        NetworkTestUtils.getNetworkConfigurations(noOfParties, ports.subList(0, noOfParties));
    Map<Integer, NetworkConfiguration> coinTossingNetConf = NetworkTestUtils
        .getNetworkConfigurations(noOfParties, ports.subList(noOfParties, ports.size()));

    Map<Integer, TestThreadRunner.TestThreadConfiguration<Spdz2kResourcePoolT, ProtocolBuilderNumeric>> conf =
        new HashMap<>();
    for (int playerId : netConf.keySet()) {
      NetworkConfiguration partyNetConf = netConf.get(playerId);
      NetworkConfiguration coinTossingPartyNetConf = coinTossingNetConf.get(playerId);
      ProtocolSuiteNumeric<Spdz2kResourcePoolT> ps = createProtocolSuite();
      BatchEvaluationStrategy<Spdz2kResourcePoolT> batchEvaluationStrategy =
          evalStrategy.getStrategy();
      ProtocolEvaluator<Spdz2kResourcePoolT> evaluator =
          new BatchedProtocolEvaluator<>(batchEvaluationStrategy, ps);

      SecureComputationEngine<Spdz2kResourcePoolT, ProtocolBuilderNumeric> sce =
          new SecureComputationEngineImpl<>(ps, evaluator);

      TestThreadRunner.TestThreadConfiguration<Spdz2kResourcePoolT, ProtocolBuilderNumeric> ttc =
          new TestThreadRunner.TestThreadConfiguration<>(
              sce,
              () -> createResourcePool(playerId, noOfParties,
                  () -> new AsyncNetwork(coinTossingPartyNetConf)),
              () -> new AsyncNetwork(partyNetConf));

      conf.put(playerId, ttc);
    }
    TestThreadRunner.run(f, conf);
  }

  protected abstract Spdz2kResourcePoolT createResourcePool(int playerId, int noOfParties,
      Supplier<Network> networkSupplier);

  protected abstract ProtocolSuiteNumeric<Spdz2kResourcePoolT> createProtocolSuite();

}
