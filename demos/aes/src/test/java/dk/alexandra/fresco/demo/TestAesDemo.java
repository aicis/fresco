package dk.alexandra.fresco.demo;

import static org.junit.Assert.fail;

import dk.alexandra.fresco.framework.ProtocolEvaluator;
import dk.alexandra.fresco.framework.TestThreadRunner;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.TestConfiguration;
import dk.alexandra.fresco.framework.network.KryoNetNetwork;
import dk.alexandra.fresco.framework.sce.SecureComputationEngine;
import dk.alexandra.fresco.framework.sce.SecureComputationEngineImpl;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedProtocolEvaluator;
import dk.alexandra.fresco.framework.sce.evaluator.SequentialStrategy;
import dk.alexandra.fresco.framework.sce.resources.ResourcePoolImpl;
import dk.alexandra.fresco.framework.util.ByteAndBitConverter;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.HmacDrbg;
import dk.alexandra.fresco.suite.ProtocolSuite;
import dk.alexandra.fresco.suite.dummy.bool.DummyBooleanProtocolSuite;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;


public class TestAesDemo {

  @Test
  public void testAesDemo() throws Exception {
    int noPlayers = 2;
    // Since SCAPI currently does not work with ports > 9999 we use fixed ports
    // here instead of relying on ephemeral ports which are often > 9999.
    List<Integer> ports = new ArrayList<>(noPlayers);
    for (int i = 1; i <= noPlayers; i++) {
      ports.add(9000 + i * 10);
    }
    Map<Integer, NetworkConfiguration> netConf =
        TestConfiguration.getNetworkConfigurations(noPlayers, ports);
    Map<Integer, TestThreadConfiguration<ResourcePoolImpl, ProtocolBuilderBinary>> conf =
        new HashMap<>();
    for (int playerId : netConf.keySet()) {
      ProtocolSuite<ResourcePoolImpl, ProtocolBuilderBinary> suite =
          new DummyBooleanProtocolSuite();
      ProtocolEvaluator<ResourcePoolImpl, ProtocolBuilderBinary> evaluator =
          new BatchedProtocolEvaluator<>(new SequentialStrategy<>(), suite);
      SecureComputationEngine<ResourcePoolImpl, ProtocolBuilderBinary> sce =
          new SecureComputationEngineImpl<>(suite, evaluator);
      Drbg drbg = new HmacDrbg();
      TestThreadConfiguration<ResourcePoolImpl, ProtocolBuilderBinary> ttc =
          new TestThreadConfiguration<>(
              sce,
              () -> new ResourcePoolImpl(playerId, noPlayers,
                  drbg),
              () -> new KryoNetNetwork(netConf.get(playerId)));
      conf.put(playerId, ttc);
    }

    TestThreadFactory<ResourcePoolImpl, ProtocolBuilderBinary> f =
        new TestThreadFactory<ResourcePoolImpl, ProtocolBuilderBinary>() {
          @Override
          public TestThread<ResourcePoolImpl, ProtocolBuilderBinary> next() {
            return new TestThread<ResourcePoolImpl, ProtocolBuilderBinary>() {

              @Override
              public void test() throws Exception {

                Boolean[] input = null;
                if (conf.getMyId() == 2) {
                  // 128-bit AES plaintext block
                  input = ByteAndBitConverter.toBoolean("00112233445566778899aabbccddeeff");
                } else if (conf.getMyId() == 1) {
                  // 128-bit key
                  input = ByteAndBitConverter.toBoolean("000102030405060708090a0b0c0d0e0f");
                }

                AesDemo app = new AesDemo(conf.getMyId(), input);

                List<Boolean> aesResult = runApplication(app);

                // Verify output state.
                String expected = "69c4e0d86a7b0430d8cdb78070b4c55a"; // expected cipher
                boolean[] actualBoolean = new boolean[aesResult.size()];
                int i = 0;
                for (Boolean b : aesResult) {
                  actualBoolean[i++] = b;
                }
                String actual = ByteAndBitConverter.toHex(actualBoolean);
                Assert.assertEquals(expected, actual);

              }
            };
          }
        };

    TestThreadRunner.run(f, conf);
  }

  @Test
  public void testAESCmdLine() throws Exception {
    Runnable p1 = () -> {
      try {
        AesDemo.main(
            new String[]{"-i", "1", "-p", "1:localhost:8081", "-p", "2:localhost:8082", "-s",
                "dummyBool", "-in", "000102030405060708090a0b0c0d0e0f"});
      } catch (IOException e) {
        throw new RuntimeException("Error", e);
      }
    };

    Runnable p2 = () -> {
      try {
        AesDemo.main(
            new String[]{"-i", "2", "-p", "1:localhost:8081", "-p", "2:localhost:8082", "-s",
                "dummyBool", "-in", "00112233445566778899aabbccddeeff"});
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

  @Test
  public void testAESCmdLine3Party() throws Exception {
    Runnable p1 = () -> {
      try {
        AesDemo.main(
            new String[]{"-i", "1", "-p", "1:localhost:8081", "-p", "2:localhost:8082",
                "-p", "3:localhost:8083", "-s", "dummyBool", "-in", "000102030405060708090a0b0c0d0e0f"});
      } catch (IOException e) {
        throw new RuntimeException("Error", e);
      }
    };

    Runnable p2 = () -> {
      try {
        AesDemo.main(
            new String[]{"-i", "2", "-p", "1:localhost:8081", "-p", "2:localhost:8082",
                "-p", "3:localhost:8083", "-s", "dummyBool", "-in", "00112233445566778899aabbccddeeff"});
      } catch (IOException e) {
        throw new RuntimeException("Error", e);
      }
    };
    
    Runnable p3 = () -> {
      try {
        AesDemo.main(
            new String[]{"-i", "3", "-p", "1:localhost:8081", "-p", "2:localhost:8082",
                "-p", "3:localhost:8083", "-s", "dummyBool"});
      } catch (IOException e) {
        throw new RuntimeException("Error", e);
      }
    };
    Thread t1 = new Thread(p1);
    Thread t2 = new Thread(p2);
    Thread t3 = new Thread(p3);
    t1.start();
    t2.start();
    t3.start();
    t1.join();
    t2.join();
    t3.join();
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void testAESCmdLineBadLength() throws Exception {
    AesDemo.main(
        new String[]{"-i", "1", "-p", "1:localhost:8081", "-p", "2:localhost:8082", 
            "-s", "dummyBool", "-in", "000102030405060708090a0b0c0d0"});
    fail();
  }

  @Test(expected=IllegalArgumentException.class)
  public void testAESCmdLineNoInput() throws Exception {
    AesDemo.main(
        new String[]{"-i", "1", "-p", "1:localhost:8081", "-p", "2:localhost:8082", 
            "-s", "dummyBool"});
    fail();
  }

  @Test(expected=IllegalArgumentException.class)
  public void testAESCmdLine3PartyInput() throws Exception {
    AesDemo.main(
        new String[]{"-i", "3", "-p", "1:localhost:8081", "-p", "2:localhost:8082", 
            "-p", "3:localhost:8083", "-s", "dummyBool", "-in", "000102030405060708090a0b0c0d0"});
    fail();
  }
  
  
}
