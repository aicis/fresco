package aggregation;

import dk.alexandra.fresco.demo.AggregationDemo;
import dk.alexandra.fresco.demo.helpers.ResourcePoolHelper;
import dk.alexandra.fresco.framework.TestThreadRunner;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.TestConfiguration;
import dk.alexandra.fresco.framework.network.NetworkingStrategy;
import dk.alexandra.fresco.framework.sce.configuration.TestSCEConfiguration;
import dk.alexandra.fresco.framework.sce.evaluator.SequentialEvaluator;
import dk.alexandra.fresco.suite.ProtocolSuite;
import dk.alexandra.fresco.suite.spdz.SpdzProtocolSuite;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePool;
import dk.alexandra.fresco.suite.spdz.configuration.PreprocessingStrategy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Ignore;
import org.junit.Test;

public class TestAggregation {

  private static void runTest(TestThreadFactory test, int n) {
    // Since SCAPI currently does not work with ports > 9999 we use fixed ports
    // here instead of relying on ephemeral ports which are often > 9999.
    List<Integer> ports = new ArrayList<Integer>(n);
    for (int i = 1; i <= n; i++) {
      ports.add(9000 + i * 10);
    }
    Map<Integer, NetworkConfiguration> netConf =
        TestConfiguration.getNetworkConfigurations(n, ports);
    Map<Integer, TestThreadConfiguration> conf = new HashMap<Integer, TestThreadConfiguration>();
    for (int i : netConf.keySet()) {
      TestThreadConfiguration ttc = new TestThreadConfiguration();
      ttc.netConf = netConf.get(i);
      ProtocolSuite suite = new SpdzProtocolSuite(150, PreprocessingStrategy.DUMMY, null);
      ttc.sceConf = new TestSCEConfiguration(suite, NetworkingStrategy.KRYONET,
          new SequentialEvaluator(), netConf.get(i), false);
      conf.put(i, ttc);
    }
    TestThreadRunner.run(test, conf);

  }

  // FIXME: Both unit test and compiled version throws the same exception: BufferUnderflowException
  @Ignore
  @Test
  public void testAggregation() throws Exception {
    final TestThreadFactory f = new TestThreadFactory() {
      @Override
      public TestThread next(TestThreadConfiguration conf) {
        return new TestThread() {
          @Override
          public void test() throws Exception {
            // Create application we are going run
            AggregationDemo<SpdzResourcePool> app = new AggregationDemo<>();

            app.runApplication(conf.sceConf, secureComputationEngine,
                (SpdzResourcePool) ResourcePoolHelper.createResourcePool(conf.sceConf,
                    conf.sceConf.getSuite(), conf.sceConf.getNetworkStrategy()));
          }
        };
      }

      ;
    };
    runTest(f, 2);
  }
}
