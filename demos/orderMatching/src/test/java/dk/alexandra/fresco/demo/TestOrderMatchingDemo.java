package dk.alexandra.fresco.demo;

import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import dk.alexandra.fresco.suite.ProtocolSuiteNumeric;
import dk.alexandra.fresco.suite.spdz2k.AbstractSpdz2kTest;
import dk.alexandra.fresco.suite.spdz2k.Spdz2kProtocolSuite64;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUInt64;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUInt64Factory;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUIntFactory;
import dk.alexandra.fresco.suite.spdz2k.resource.Spdz2kResourcePool;
import dk.alexandra.fresco.suite.spdz2k.resource.Spdz2kResourcePoolImpl;
import dk.alexandra.fresco.suite.spdz2k.resource.storage.Spdz2kDummyDataSupplier;
import dk.alexandra.fresco.suite.spdz2k.resource.storage.Spdz2kOpenedValueStoreImpl;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;

public class TestOrderMatchingDemo extends AbstractSpdz2kTest<Spdz2kResourcePool<CompUInt64>> {
  public static class TestOrderMatchingSimple<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    private static final List<Order> orders = Arrays.asList(
        new Order(0, 1000, false),
        new Order(1, 995, true),
        new Order(2, 998, false),
        new Order(3, 1005, true),
        new Order(4, 992, false),
        new Order(5, 1002, true),
        new Order(6, 1001, true),
        new Order(7, 1008, false));

    // Matching the largest max value buy order with the smallest sell order s.t. buy > sell
    private static final List<OrderMatch> expected = Arrays.asList(
        new OrderMatch(3, 4, 998),
        new OrderMatch(5, 2, 1000),
        new OrderMatch(6, 0, 1000));
    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Test
        public void test() {
          List<Order> buyOrders = new ArrayList<>();
          List<Order> sellOrders = new ArrayList<>();
          if (conf.getMyId() == 1) {
            // For the simple case we simply have one party supply all orders in plain
            buyOrders = orders.stream().filter(c -> c.buy == true).collect(Collectors.toList());
            sellOrders = orders.stream().filter(c -> c.buy == false).collect(Collectors.toList());
          }
          OrderMatchingDemo distDemo = new OrderMatchingDemo(conf.getMyId(), (int) orders.stream().filter(f -> f.buy == true).count(), buyOrders,
              (int) orders.stream().filter(f -> f.buy == false).count(), sellOrders);
          List<OrderMatch> res = runApplication(distDemo);
          Assert.assertTrue(TestOrderMatchingSimple.expected.containsAll(res));
          Assert.assertEquals(TestOrderMatchingSimple.expected.size(), res.size());
//          for (int i = 0; i < expected.size(); i++) {
//            Assert.assertEquals(processedOrders.get(i).firstId,
//                expected.get(i).firstId);
//            Assert.assertEquals(processedOrders.get(i).secondId,
//                expected.get(i).secondId);
//            Assert.assertEquals(processedOrders.get(i).rate,
//                expected.get(i).rate);
//          }
        }
      };
    }
  }

  @Override
  protected Spdz2kResourcePool<CompUInt64> createResourcePool(int playerId, int noOfParties,
      Supplier<Network> networkSupplier) {
    CompUIntFactory<CompUInt64> factory = new CompUInt64Factory();
    CompUInt64 keyShare = factory.createRandom();
    Spdz2kResourcePool<CompUInt64> resourcePool =
        new Spdz2kResourcePoolImpl<>(
            playerId,
            noOfParties, new AesCtrDrbg(new byte[32]),
            new Spdz2kOpenedValueStoreImpl<>(),
            new Spdz2kDummyDataSupplier<>(playerId, noOfParties, keyShare, factory),
            factory);
    resourcePool.initializeJointRandomness(networkSupplier, AesCtrDrbg::new, 32);
    return resourcePool;
  }

  @Override
  protected ProtocolSuiteNumeric<Spdz2kResourcePool<CompUInt64>> createProtocolSuite() {
    return new Spdz2kProtocolSuite64(true);
  }


  @Test
  public void testSimpleMatching() {
    runTest(new TestOrderMatchingSimple<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void testPlainOrderMatching() {
    PlainOrderMatching matching = new PlainOrderMatching(TestOrderMatchingSimple.orders);
    Set<OrderMatch> res = new HashSet<>(matching.compute());
    Assert.assertTrue(TestOrderMatchingSimple.expected.containsAll(res));
    Assert.assertEquals(TestOrderMatchingSimple.expected.size(), res.size());
//    for (int i = 0; i < res.size(); i++) {
//      Assert.assertEquals(TestOrderMatchingSimple.expected.get(i).firstId, res.get(i).firstId);
//      Assert.assertEquals(TestOrderMatchingSimple.expected.get(i).secondId, res.get(i).secondId);
//      Assert.assertEquals(TestOrderMatchingSimple.expected.get(i).rate, res.get(i).rate);
//    }
  }
}
