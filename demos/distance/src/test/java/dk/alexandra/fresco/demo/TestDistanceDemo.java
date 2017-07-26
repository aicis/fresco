package dk.alexandra.fresco.demo;

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
import dk.alexandra.fresco.suite.spdz.configuration.PreprocessingStrategy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

public class TestDistanceDemo {

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

  @Test
  public void testDistance() throws Exception {
    final TestThreadFactory f = new TestThreadFactory() {
      @Override
      public TestThread next(TestThreadConfiguration conf) {
        return new TestThread() {
          @Override
          public void test() throws Exception {
            int x, y;
            if (conf.getMyId() == 1) {
              x = 10;
              y = 10;
            } else {
              x = 20;
              y = 15;
            }
            System.out.println("Running with x: " + x + ", y: " + y);
            DistanceDemo distDemo = new DistanceDemo(conf.getMyId(), x, y);
            secureComputationEngine.runApplication(distDemo,
                ResourcePoolHelper.createResourcePool(conf.sceConf, conf.sceConf.getSuite()));
            double distance = distDemo.getOutput().out().doubleValue();
            distance = Math.sqrt(distance);
            Assert.assertEquals(11.1803, distance, 0.0001);
          }
        };
      }

      ;
    };
    runTest(f, 2);
    ResourcePoolHelper.shutdown();
  }
}
