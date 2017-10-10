package dk.alexandra.fresco.network;

import static org.junit.Assert.assertTrue;

import dk.alexandra.fresco.framework.TestThreadRunner;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.TestConfiguration;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.resources.ResourcePoolImpl;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

public class TestScapiNetwork {

  private abstract static class ThreadWithFixture
      extends TestThread<ResourcePoolImpl, ProtocolBuilderNumeric> {

    protected ScapiNetworkImpl network;
    protected int timeoutMillis = 10000;

    @Override
    public void setUp() {
      network = (ScapiNetworkImpl) this.conf.resourcePool.getNetwork();
    }

  }

  private static void runTest(
      TestThreadFactory<ResourcePoolImpl, ProtocolBuilderNumeric> test, int n, int noOfChannels) {
    // Since SCAPI currently does not work with ports > 9999 we use fixed ports
    // here instead of relying on ephemeral ports which are often > 9999.
    List<Integer> ports = new ArrayList<Integer>(n);
    for (int i = 1; i <= n; i++) {
      ports.add(9000 + i);
    }
    Map<Integer, NetworkConfiguration> netConf =
        TestConfiguration.getNetworkConfigurations(n, ports);
    Map<Integer, TestThreadConfiguration<ResourcePoolImpl, ProtocolBuilderNumeric>> conf =
        new HashMap<Integer, TestThreadConfiguration<ResourcePoolImpl, ProtocolBuilderNumeric>>();
    for (int i : netConf.keySet()) {
      Network network = new ScapiNetworkImpl();
      network.init(netConf.get(i), noOfChannels);
      ResourcePoolImpl rp = new ResourcePoolImpl(i, n, network, null, null);
      TestThreadConfiguration<ResourcePoolImpl, ProtocolBuilderNumeric> ttc =
          new TestThreadConfiguration<>(null,
              rp);
      conf.put(i, ttc);
    }
    TestThreadRunner.run(test, conf);

  }


  final TestThreadFactory<ResourcePoolImpl, ProtocolBuilderNumeric> test =
      new TestThreadFactory<ResourcePoolImpl, ProtocolBuilderNumeric>() {
        @Override
        public TestThread<ResourcePoolImpl, ProtocolBuilderNumeric> next() {
          return new ThreadWithFixture() {
            @Override
            public void test() throws Exception {
              network.connect(timeoutMillis);
              network.close();
            }
          };
        }
      };


  @Test
  public void testCanConnect_2() throws Exception {
    runTest(test, 2, 1);
  }

  @Test
  public void testCanConnect_3() throws Exception {
    runTest(test, 3, 1);
  }

  @Test
  public void testCanConnect_7() throws Exception {
    runTest(test, 7, 1);
  }



  @Test
  public void testPlayerTwoCanSendBytesToPlayerOne() throws Exception {
    final byte[] data = new byte[] {0x42, 0xf, 0x00, 0x23, 0x15};
    final TestThreadFactory<ResourcePoolImpl, ProtocolBuilderNumeric> test =
        new TestThreadFactory<ResourcePoolImpl, ProtocolBuilderNumeric>() {
          @Override
          public TestThread<ResourcePoolImpl, ProtocolBuilderNumeric> next() {
            return new ThreadWithFixture() {
              @Override
              public void test() throws Exception {
                network.connect(timeoutMillis);
                if (conf.getMyId() == 1) {
                  byte[] received = (byte[]) network.receive(2);
                  assertTrue(Arrays.equals(data, received));
                } else if (conf.getMyId() == 2) {
                  network.send(1, data);
                }
                network.close();
              }
            };
          }
        };
    runTest(test, 3, 1);
  }



  @Test
  public void testCanUseDifferentChannels() throws Exception {    

    final byte[] data1 = new byte[] {0x42, 0xf, 0x00, 0x23, 0x15};
    final byte[] data2 = new byte[] {0x34, 0x2, 0x00, 0x1, 0x22};
    final TestThreadFactory<ResourcePoolImpl, ProtocolBuilderNumeric> test =
        new TestThreadFactory<ResourcePoolImpl, ProtocolBuilderNumeric>() {
          @Override
          public TestThread<ResourcePoolImpl, ProtocolBuilderNumeric> next() {
            return new ThreadWithFixture() {
              @Override
              public void test() throws Exception {
                network.connect(timeoutMillis);
                if (conf.getMyId() == 1) {
                  network.send(0, 2, data2);
                  byte[] received = (byte[]) network.receive(1, 2);
                  assertTrue(Arrays.equals(data1, received));
                } else if (conf.getMyId() == 2) {
                  network.send(1, 1, data1);
                  byte[] received = (byte[]) network.receive(0, 1);
                  assertTrue(Arrays.equals(data2, received));
                }
                network.close();
              }
            };
          }
        };
    runTest(test, 3, 2);
  }
}
