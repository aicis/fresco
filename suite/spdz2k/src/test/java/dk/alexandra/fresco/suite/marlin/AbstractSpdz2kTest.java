package dk.alexandra.fresco.suite.marlin;

import dk.alexandra.fresco.framework.ProtocolEvaluator;
import dk.alexandra.fresco.framework.TestThreadRunner;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.TestConfiguration;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.async.AsyncNetwork;
import dk.alexandra.fresco.framework.sce.SecureComputationEngine;
import dk.alexandra.fresco.framework.sce.SecureComputationEngineImpl;
import dk.alexandra.fresco.framework.sce.evaluator.BatchEvaluationStrategy;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedProtocolEvaluator;
import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.suite.ProtocolSuiteNumeric;
import dk.alexandra.fresco.suite.marlin.resource.Spdz2kResourcePool;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public abstract class AbstractSpdz2kTest<MarlinResourcePoolT extends Spdz2kResourcePool<?>> {

  private final List<Integer> partyNumbers = Arrays.asList(2, 3);

  void runTest(
      TestThreadRunner.TestThreadFactory<MarlinResourcePoolT, ProtocolBuilderNumeric> f,
      EvaluationStrategy evalStrategy) {
    for (Integer numberOfParties : partyNumbers) {
      runTest(f, evalStrategy, numberOfParties);
    }
  }

  protected void runTest(
      TestThreadFactory<MarlinResourcePoolT, ProtocolBuilderNumeric> f,
      EvaluationStrategy evalStrategy, int noOfParties) {

    List<Integer> ports = getFreePorts(2 * noOfParties);
    Map<Integer, NetworkConfiguration> netConf =
        TestConfiguration.getNetworkConfigurations(noOfParties, ports.subList(0, noOfParties));
    Map<Integer, NetworkConfiguration> coinTossingNetConf = TestConfiguration
        .getNetworkConfigurations(noOfParties, ports.subList(noOfParties, ports.size()));

    Map<Integer, TestThreadRunner.TestThreadConfiguration<MarlinResourcePoolT, ProtocolBuilderNumeric>> conf =
        new HashMap<>();
    for (int playerId : netConf.keySet()) {
      NetworkConfiguration partyNetConf = netConf.get(playerId);
      NetworkConfiguration coinTossingPartyNetConf = coinTossingNetConf.get(playerId);
      ProtocolSuiteNumeric<MarlinResourcePoolT> ps = createProtocolSuite();
      BatchEvaluationStrategy<MarlinResourcePoolT> batchEvaluationStrategy =
          evalStrategy.getStrategy();
      ProtocolEvaluator<MarlinResourcePoolT> evaluator =
          new BatchedProtocolEvaluator<>(batchEvaluationStrategy, ps);

      SecureComputationEngine<MarlinResourcePoolT, ProtocolBuilderNumeric> sce =
          new SecureComputationEngineImpl<>(ps, evaluator);

      Supplier<Network> networkSupplier = () -> (Network) new AsyncNetwork(partyNetConf);
      TestThreadRunner.TestThreadConfiguration<MarlinResourcePoolT, ProtocolBuilderNumeric> ttc =
          new TestThreadRunner.TestThreadConfiguration<>(
              sce,
              () -> createResourcePool(playerId, noOfParties,
                  () -> new AsyncNetwork(coinTossingPartyNetConf)),
              () -> new AsyncNetwork(partyNetConf));

      conf.put(playerId, ttc);
    }
    TestThreadRunner.run(f, conf);
  }

  private List<Integer> getFreePorts(int numPorts) {
    List<Integer> ports = new ArrayList<>();
    for (int i = 0; i < numPorts; i++) {
      try (ServerSocket s = new ServerSocket(0)) {
        ports.add(s.getLocalPort());
      } catch (IOException e) {
        throw new RuntimeException("No free ports", e);
      }
    }
    return ports;
  }

  protected abstract MarlinResourcePoolT createResourcePool(int playerId, int noOfParties,
      Supplier<Network> networkSupplier);

  protected abstract ProtocolSuiteNumeric<MarlinResourcePoolT> createProtocolSuite();

}
