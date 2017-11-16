package aggregation;

import dk.alexandra.fresco.demo.AggregationDemo;
import dk.alexandra.fresco.framework.ProtocolEvaluator;
import dk.alexandra.fresco.framework.TestThreadRunner;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.TestConfiguration;
import dk.alexandra.fresco.framework.network.KryoNetNetwork;
import dk.alexandra.fresco.framework.sce.SecureComputationEngine;
import dk.alexandra.fresco.framework.sce.SecureComputationEngineImpl;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedProtocolEvaluator;
import dk.alexandra.fresco.framework.sce.evaluator.SequentialStrategy;
import dk.alexandra.fresco.framework.util.DetermSecureRandom;
import dk.alexandra.fresco.suite.ProtocolSuite;
import dk.alexandra.fresco.suite.spdz.SpdzProtocolSuite;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePool;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePoolImpl;
import dk.alexandra.fresco.suite.spdz.storage.SpdzStorage;
import dk.alexandra.fresco.suite.spdz.storage.SpdzStorageDummyImpl;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.junit.Test;

public class TestAggregation {

  private static void runTest(TestThreadFactory<SpdzResourcePool, ProtocolBuilderNumeric> test,
      int n) throws Exception {
    // Since SCAPI currently does not work with ports > 9999 we use fixed ports
    // here instead of relying on ephemeral ports which are often > 9999.
    List<Integer> ports = new ArrayList<>(n);
    for (int i = 1; i <= n; i++) {
      ports.add(9000 + i * 10);
    }
    Map<Integer, NetworkConfiguration> netConf =
        TestConfiguration.getNetworkConfigurations(n, ports);
    Map<Integer, TestThreadConfiguration<SpdzResourcePool, ProtocolBuilderNumeric>> conf =
        new HashMap<>();
    for (int i : netConf.keySet()) {
      ProtocolSuite<SpdzResourcePool, ProtocolBuilderNumeric> suite = new SpdzProtocolSuite(150);
      SpdzStorage store = new SpdzStorageDummyImpl(i, n);
      ProtocolEvaluator<SpdzResourcePool, ProtocolBuilderNumeric> evaluator =
          new BatchedProtocolEvaluator<>(new SequentialStrategy<>());
      SecureComputationEngine<SpdzResourcePool, ProtocolBuilderNumeric> sce =
          new SecureComputationEngineImpl<>(suite, evaluator);
      TestThreadConfiguration<SpdzResourcePool, ProtocolBuilderNumeric> ttc =
          new TestThreadConfiguration<>(
              sce,
              () -> {
                KryoNetNetwork network = new KryoNetNetwork();
                network.init(netConf.get(i));
                return new SpdzResourcePoolImpl(i, n, network, new Random(),
                    new DetermSecureRandom(), store);
              });
      conf.put(i, ttc);
    }
    TestThreadRunner.run(test, conf);


  }

  @Test
  public void testAggregation() throws Exception {
    final TestThreadFactory<SpdzResourcePool, ProtocolBuilderNumeric> f =
        new TestThreadFactory<SpdzResourcePool, ProtocolBuilderNumeric>() {
          @Override
          public TestThread<SpdzResourcePool, ProtocolBuilderNumeric> next() {
            return new TestThread<SpdzResourcePool, ProtocolBuilderNumeric>() {
              @Override
              public void test() throws Exception {
                // Create application we are going run
                AggregationDemo<SpdzResourcePool> app = new AggregationDemo<>();
                app.runApplication(conf.sce, conf.getResourcePool());
              }
            };
          }

          ;
        };
    runTest(f, 2);
  }

  @Test
  public void testAggregationCmdLine() throws Exception {
    Runnable p1 = () -> {
      try {
        AggregationDemo.main(
            new String[]{"1", "-i", "1", "-p", "1:localhost:8081", "-p", "2:localhost:8082", "-s",
                "dummyArithmetic"});
      } catch (IOException e) {
        throw new RuntimeException("Error", e);
      }
    };

    Runnable p2 = () -> {
      try {
        AggregationDemo.main(
            new String[]{"2", "-i", "2", "-p", "1:localhost:8081", "-p", "2:localhost:8082", "-s",
                "dummyArithmetic"});
      } catch (IOException e) {
        throw new RuntimeException("Error", e);
      }
    };
    Thread t1 = new Thread(p1);
    Thread t2 = new Thread(p2);
    t1.start();
    t2.start();
    t1.join();
    t2.join();
  }
}
