package dk.alexandra.fresco.demo;

import dk.alexandra.fresco.framework.ProtocolEvaluator;
import dk.alexandra.fresco.framework.TestThreadRunner;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.NetworkUtil;
import dk.alexandra.fresco.framework.network.AsyncNetwork;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.SecureComputationEngineImpl;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedProtocolEvaluator;
import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import dk.alexandra.fresco.suite.ProtocolSuite;
import dk.alexandra.fresco.suite.ProtocolSuiteNumeric;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;

public class TestOrderMatchingDemo {
  protected void runTest(
      TestThreadRunner.TestThreadFactory<Spdz2kResourcePool, ProtocolBuilderNumeric> f,
      EvaluationStrategy evalStrategy, int noOfParties) {
    List<Integer> ports = NetworkUtil.getFreePorts(2 * noOfParties);
    Map<Integer, NetworkConfiguration> netConf =
        NetworkUtil.getNetworkConfigurations(ports.subList(0, noOfParties));
    Map<Integer, NetworkConfiguration> coinTossingNetConf = NetworkUtil
        .getNetworkConfigurations(ports.subList(noOfParties, ports.size()));
    Map<Integer, TestThreadRunner.TestThreadConfiguration<Spdz2kResourcePool, ProtocolBuilderNumeric>> conf =
        new HashMap<>();
    for (int playerId : netConf.keySet()) {
      ProtocolSuite protocolSuite = new Spdz2kProtocolSuite64(true);
      NetworkConfiguration coinTossingPartyNetConf = coinTossingNetConf.get(playerId);
      NetworkConfiguration partyNetConf = netConf.get(playerId);
      ProtocolEvaluator<Spdz2kResourcePool> evaluator =
          new BatchedProtocolEvaluator<Spdz2kResourcePool>(evalStrategy.getStrategy(), protocolSuite);

      TestThreadRunner.TestThreadConfiguration<Spdz2kResourcePool, ProtocolBuilderNumeric> ttc =
          new TestThreadRunner.TestThreadConfiguration<Spdz2kResourcePool, ProtocolBuilderNumeric>(
              new SecureComputationEngineImpl<>(protocolSuite, evaluator),
              () -> createResourcePool(playerId, noOfParties, () -> new AsyncNetwork(coinTossingPartyNetConf)),
              () -> new AsyncNetwork(partyNetConf));
      conf.put(playerId, ttc);
    }
    TestThreadRunner.run(f, conf);
  }

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

  protected ProtocolSuiteNumeric<Spdz2kResourcePool<CompUInt64>> createProtocolSuite() {
    return new Spdz2kProtocolSuite64(true);
  }


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
        }
      };
    }
  }

  public static class TestOrderMatchingBig<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {
    static final int AMOUNT = 64;
    static final List<Order> buyOrders = OrderMatchingDemo.generateOrders(AMOUNT, true);
    static final List<Order> sellOrders = OrderMatchingDemo.generateOrders(AMOUNT, false);
    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Test
        public void test() {
          List<Order> inputBuy = new ArrayList<>();
          List<Order> inputSell = new ArrayList<>();
          if (conf.getMyId() == 1) {
            // For the simple case we simply have one party supply all orders in plain
            inputBuy = buyOrders;
            inputSell = sellOrders;
          }
          OrderMatchingDemo distDemo = new OrderMatchingDemo(conf.getMyId(), AMOUNT, inputBuy,
              AMOUNT, inputSell);
          List<OrderMatch> processedOrders = runApplication(distDemo);
          List<Order> totalOrders = new ArrayList<>(buyOrders);
          totalOrders.addAll(sellOrders);
          List<OrderMatch> reference = (new PlainOrderMatching(totalOrders)).compute();

          Assert.assertEquals(reference.size(), processedOrders.size());
          List<OrderMatch> sortedReal = processedOrders.stream().sorted().collect(Collectors.toList());
          List<OrderMatch> sortedReference = reference.stream().sorted().collect(Collectors.toList());
          for (int i = 0; i < sortedReference.size(); i++) {
            // TODO the OddEven mergesort is not stable so equal bits don't get stay in order based on user ID and are instead shuffeled arbitrary
            // this is fine now since we only have one round but needs to be fixed for multiple rounds
//            Assert.assertEquals(sortedReference.get(i).firstId,
//                sortedReal.get(i).firstId);
//            Assert.assertEquals(sortedReference.get(i).secondId,
//                sortedReal.get(i).secondId);
            Assert.assertEquals(sortedReference.get(i).rate,
                sortedReal.get(i).rate);
          }
        }
      };
    }
  }


  @Test
  public void testSimpleMatching() {
    int noParties = 2;
    runTest(new TestOrderMatchingSimple<>(), EvaluationStrategy.SEQUENTIAL_BATCHED, noParties);
  }

  @Test
  public void testBigMatching() {
    int noParties = 2;
    runTest(new TestOrderMatchingBig<>(), EvaluationStrategy.SEQUENTIAL_BATCHED, noParties);
  }

  @Test
  public void testPlainOrderMatching() {
    PlainOrderMatching matching = new PlainOrderMatching(TestOrderMatchingSimple.orders);
    Set<OrderMatch> res = new HashSet<>(matching.compute());
    Assert.assertTrue(TestOrderMatchingSimple.expected.containsAll(res));
    Assert.assertEquals(TestOrderMatchingSimple.expected.size(), res.size());
  }
}
