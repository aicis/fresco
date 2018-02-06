package dk.alexandra.fresco.network;

import static org.junit.Assert.assertTrue;

import dk.alexandra.fresco.framework.Party;
import dk.alexandra.fresco.framework.TestThreadRunner;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.NetworkConfigurationImpl;
import dk.alexandra.fresco.framework.configuration.TestConfiguration;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.resources.ResourcePoolImpl;
import java.io.Closeable;
import java.io.IOException;
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
      network = (ScapiNetworkImpl) this.conf.getNetwork();
    }

  }

  private static void runTest(TestThreadFactory<ResourcePoolImpl, ProtocolBuilderNumeric> test,
      int n) {
    // Since SCAPI currently does not work with ports > 9999 we use fixed ports
    // here instead of relying on ephemeral ports which are often > 9999.
    List<Integer> ports = new ArrayList<>(n);
    for (int i = 1; i <= n; i++) {
      ports.add(9100 + i);
    }
    Map<Integer, NetworkConfiguration> netConf =
        TestConfiguration.getNetworkConfigurations(n, ports);
    Map<Integer, TestThreadConfiguration<ResourcePoolImpl, ProtocolBuilderNumeric>> conf =
        new HashMap<>();
    for (int i : netConf.keySet()) {
      ScapiNetworkImpl network = new ScapiNetworkImpl();
      network.init(netConf.get(i), 1);
      TestThreadConfiguration<ResourcePoolImpl, ProtocolBuilderNumeric> ttc =
          new TestThreadConfiguration<>(null,
              () -> new ResourcePoolImpl(i, n, null), () -> network);
      conf.put(i, ttc);
    }
    TestThreadRunner.run(test, conf);
  }

  private static void runTestSecureCommunication(
      TestThreadFactory<ResourcePoolImpl, ProtocolBuilderNumeric> test) throws IOException {
    // Since SCAPI currently does not work with ports > 9999 we use fixed ports
    // here instead of relying on ephemeral ports which are often > 9999.
    List<Integer> ports = new ArrayList<>(2);
    for (int i = 1; i <= 2; i++) {
      ports.add(9100 + i);
    }
    Map<Integer, NetworkConfiguration> netConfs = new HashMap<>(2);
    for (int i = 0; i < 2; i++) {
      Map<Integer, Party> partyMap = new HashMap<>();
      int id = 1;
      for (int port : ports) {
        Party party = new Party(id, "localhost", port, "MDEyMzQ1Njc4OWFiY2RlZg==");
        partyMap.put(id, party);
        id++;
      }
      netConfs.put(i + 1, new NetworkConfigurationImpl(i + 1, partyMap));
    }
    Map<Integer, TestThreadConfiguration<ResourcePoolImpl, ProtocolBuilderNumeric>> configurations =
        createConfigurations(2, netConfs);
    TestThreadRunner.run(test, configurations);
    for (TestThreadConfiguration<ResourcePoolImpl, ProtocolBuilderNumeric> networkConfiguration : configurations
        .values()) {
      Network network = networkConfiguration.getNetwork();
      if (network instanceof Closeable) {
        ((Closeable) network).close();
      }
    }
  }

  private static Map<
      Integer,
      TestThreadConfiguration<ResourcePoolImpl, ProtocolBuilderNumeric>> createConfigurations(
      int n, Map<Integer, NetworkConfiguration> netConf) {
    Map<Integer, TestThreadConfiguration<ResourcePoolImpl, ProtocolBuilderNumeric>> conf =
        new HashMap<>();
    for (int i : netConf.keySet()) {
      ScapiNetworkImpl network = new ScapiNetworkImpl();
      network.init(netConf.get(i), 1);
      TestThreadConfiguration<ResourcePoolImpl, ProtocolBuilderNumeric> ttc =
          new TestThreadConfiguration<>(null,
              () -> new ResourcePoolImpl(i, n, null), () -> network);
      conf.put(i, ttc);
    }
    return conf;
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
    runTest(test, 2);
  }

  @Test
  public void testCanConnect_3() throws Exception {
    runTest(test, 3);
  }

  @Test
  public void testCanConnect_7() throws Exception {
    runTest(test, 7);
  }


  @Test
  public void testConnectTwice() throws Exception {
    runTest(test, 2);
  }

  @Test
  public void testConnectSecure() throws Exception {
    runTestSecureCommunication(test);
  }

  @Test
  public void testPlayerTwoCanSendBytesToPlayerOne() throws Exception {
    final byte[] data = new byte[]{0x42, 0xf, 0x00, 0x23, 0x15};
    final TestThreadFactory<ResourcePoolImpl, ProtocolBuilderNumeric> test =
        new TestThreadFactory<ResourcePoolImpl, ProtocolBuilderNumeric>() {
          @Override
          public TestThread<ResourcePoolImpl, ProtocolBuilderNumeric> next() {
            return new ThreadWithFixture() {
              @Override
              public void test() throws Exception {
                network.connect(timeoutMillis);
                if (conf.getMyId() == 1) {
                  byte[] received = conf.getNetwork().receive(2);
                  assertTrue(Arrays.equals(data, received));
                } else if (conf.getMyId() == 2) {
                  conf.getNetwork().send(1, data);
                }
                network.close();
              }
            };
          }
        };
    runTest(test, 3);
  }

  @Test
  public void testSelfSend() throws Exception {
    final byte[] data = new byte[]{0x42, 0xf, 0x00, 0x23, 0x15};
    final TestThreadFactory<ResourcePoolImpl, ProtocolBuilderNumeric> test =
        new TestThreadFactory<ResourcePoolImpl, ProtocolBuilderNumeric>() {
          @Override
          public TestThread<ResourcePoolImpl, ProtocolBuilderNumeric> next() {
            return new ThreadWithFixture() {
              @Override
              public void test() throws Exception {
                network.connect(timeoutMillis);
                if (conf.getMyId() == 1) {
                  network.send(1, data);
                  byte[] received = network.receive(1);
                  assertTrue(Arrays.equals(data, received));
                }
                network.close();
              }
            };
          }
        };
    runTest(test, 3);
  }

  @Test
  public void testSendToNonParty() throws Exception {
    final byte[] data = new byte[]{0x42, 0xf, 0x00, 0x23, 0x15};
    final TestThreadFactory<ResourcePoolImpl, ProtocolBuilderNumeric> test =
        new TestThreadFactory<ResourcePoolImpl, ProtocolBuilderNumeric>() {
          @Override
          public TestThread<ResourcePoolImpl, ProtocolBuilderNumeric> next() {
            return new ThreadWithFixture() {
              @Override
              public void test() throws Exception {
                network.connect(timeoutMillis);
                if (conf.getMyId() == 1) {
                  boolean exception = false;
                  try {
                    network.send(4, data);
                  } catch (IllegalArgumentException e) {
                    exception = true;
                  }
                  assertTrue(exception);
                }
              }
            };
          }
        };
    runTest(test, 3);

  }

  @Test
  public void testCanUseDifferentChannels() throws Exception {

    final byte[] data1 = new byte[]{0x42, 0xf, 0x00, 0x23, 0x15};
    final byte[] data2 = new byte[]{0x34, 0x2, 0x00, 0x1, 0x22};
    final TestThreadFactory<ResourcePoolImpl, ProtocolBuilderNumeric> test =
        new TestThreadFactory<ResourcePoolImpl, ProtocolBuilderNumeric>() {
          @Override
          public TestThread<ResourcePoolImpl, ProtocolBuilderNumeric> next() {
            return new ThreadWithFixture() {
              @Override
              public void test() throws Exception {
                network.connect(timeoutMillis);
                if (conf.getMyId() == 1) {
                  network.send(2, data2);
                  byte[] received = network.receive(2);
                  assertTrue(Arrays.equals(data1, received));
                } else if (conf.getMyId() == 2) {
                  network.send(1, data1);
                  byte[] received = network.receive(1);
                  assertTrue(Arrays.equals(data2, received));
                }
                network.close();
              }
            };
          }
        };
    runTest(test, 3);
  }
}
