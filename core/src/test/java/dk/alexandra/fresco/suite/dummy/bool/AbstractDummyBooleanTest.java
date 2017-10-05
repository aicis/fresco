package dk.alexandra.fresco.suite.dummy.bool;

import dk.alexandra.fresco.framework.ProtocolEvaluator;
import dk.alexandra.fresco.framework.TestThreadRunner;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.TestConfiguration;
import dk.alexandra.fresco.framework.network.NetworkingStrategy;
import dk.alexandra.fresco.framework.sce.configuration.TestSCEConfiguration;
import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.framework.sce.resources.ResourcePoolImpl;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract class which handles a lot of boiler plate testing code. This makes running a single test
 * using different parameters quite easy.
 */
public abstract class AbstractDummyBooleanTest {

  protected void runTest(
      TestThreadRunner.TestThreadFactory<ResourcePoolImpl, ProtocolBuilderBinary> f,
      EvaluationStrategy evalStrategy, NetworkingStrategy network) throws Exception {
    // The dummy protocol suite has the nice property that it can be run by just one player.
    int noOfParties = 1;
    List<Integer> ports = new ArrayList<Integer>(noOfParties);
    for (int i = 1; i <= noOfParties; i++) {
      ports.add(9000 + i * (noOfParties - 1));
    }

    Map<Integer, NetworkConfiguration> netConf =
        TestConfiguration.getNetworkConfigurations(noOfParties, ports);
    Map<Integer, TestThreadRunner.TestThreadConfiguration<ResourcePoolImpl, ProtocolBuilderBinary>> conf =
        new HashMap<Integer, TestThreadRunner.TestThreadConfiguration<ResourcePoolImpl, ProtocolBuilderBinary>>();
    for (int playerId : netConf.keySet()) {
      TestThreadRunner.TestThreadConfiguration<ResourcePoolImpl, ProtocolBuilderBinary> ttc =
          new TestThreadRunner.TestThreadConfiguration<>();
      ttc.netConf = netConf.get(playerId);

      DummyBooleanProtocolSuite ps = new DummyBooleanProtocolSuite();

      boolean useSecureConnection = false; // No tests of secure connection here.

      ProtocolEvaluator<ResourcePoolImpl, ProtocolBuilderBinary> evaluator =
          EvaluationStrategy.fromEnum(evalStrategy);
      ttc.sceConf = new TestSCEConfiguration<ResourcePoolImpl, ProtocolBuilderBinary>(ps, network,
          evaluator, ttc.netConf, useSecureConnection);
      conf.put(playerId, ttc);
    }
    TestThreadRunner.run(f, conf);
  }
}
