package dk.alexandra.fresco.demo;

import dk.alexandra.fresco.demo.cli.CmdLineUtil;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.sce.SecureComputationEngine;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.apache.commons.cli.CommandLine;

public class BenchMatching {//extends AbstractSpdz2kTest<Spdz2kResourcePool<CompUInt64>> {
//  public static class TestOrderMatchingBig<ResourcePoolT extends ResourcePool>
//      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {
    static final int AMOUNT = 64;
    static final List<Order> buyOrders = generateOrders(AMOUNT, true);
    static final List<Order> sellOrders = generateOrders(AMOUNT, false);

//    @Override
//    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
//
//      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
//        @Test
//        public void test() {
//          List<Order> inputBuy = new ArrayList<>();
//          List<Order> inputSell = new ArrayList<>();
//          if (conf.getMyId() == 1) {
//            // For the simple case we simply have one party supply all orders in plain
//            inputBuy = buyOrders;
//            inputSell = sellOrders;
//          }
//          OrderMatchingDemo distDemo = new OrderMatchingDemo(conf.getMyId(), buyOrders.size(), inputBuy,
//              sellOrders.size(), inputSell);
//          List<OrderMatch> processedOrders = runApplication(distDemo);
//
//          List<Order> totalOrders = new ArrayList<>(buyOrders);
//          totalOrders.addAll(sellOrders);
//          List<OrderMatch> reference = (new PlainOrderMatching(totalOrders)).compute();
//          Assert.assertEquals(reference.size(), processedOrders.size());
//          List<OrderMatch> sortedReal = processedOrders.stream().sorted().collect(Collectors.toList());
//          List<OrderMatch> sortedReference = reference.stream().sorted().collect(Collectors.toList());
//          for (int i = 0; i < sortedReference.size(); i++) {
//            // TODO the OddEven mergesort is not stable so equal bits don't get stay in order based on user ID and are instead shuffeled arbitrary
//            // this is fine now since we only have one round but needs to be fixed for multiple rounds
////            System.out.println(i);
////            Assert.assertEquals(sortedReference.get(i).firstId,
////                sortedReal.get(i).firstId);
////            Assert.assertEquals(sortedReference.get(i).secondId,
////                sortedReal.get(i).secondId);
//            Assert.assertEquals(sortedReference.get(i).rate,
//                sortedReal.get(i).rate);
//          }
//        }
//      };
//    }
//  }
//
//  @Override
//  protected Spdz2kResourcePool<CompUInt64> createResourcePool(int playerId, int noOfParties,
//      Supplier<Network> networkSupplier) {
//    CompUIntFactory<CompUInt64> factory = new CompUInt64Factory();
//    CompUInt64 keyShare = factory.createRandom();
//    Spdz2kResourcePool<CompUInt64> resourcePool =
//        new Spdz2kResourcePoolImpl<>(
//            playerId,
//            noOfParties, new AesCtrDrbg(new byte[32]),
//            new Spdz2kOpenedValueStoreImpl<>(),
//            new Spdz2kDummyDataSupplier<>(playerId, noOfParties, keyShare, factory),
//            factory);
//    resourcePool.initializeJointRandomness(networkSupplier, AesCtrDrbg::new, 32);
//    return resourcePool;
//  }
//
//  @Override
//  protected ProtocolSuiteNumeric<Spdz2kResourcePool<CompUInt64>> createProtocolSuite() {
//    return new Spdz2kProtocolSuite64(true);
//  }


//  @Test
//  public void testBigMatching() {
//    runTest(new BenchMatching.TestOrderMatchingBig<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
//  }

  /**
   * Simulate a list of orders, around 1000000
   * @param amount amount of orders in the list
   * @param buy true if it is a list of buy orders
   * @return list of orders, buy orders have userid [0;amount[ and sell orders [amount:2*amount[
   */
  static List<Order> generateOrders(int amount, boolean buy) {
    Random rand = new Random(buy ? 1 : 0);
    List<Order> orders = new ArrayList<>();
    for (int i = 0; i < amount; i++) {
      int mean = buy ? 1000100 : 999900;
      int rate = gaussianInt(rand, mean, 100+2*i);
      orders.add(new Order(buy ? i : amount + i, rate, buy));
    }
    return orders;
  }

  private static int gaussianInt(Random rand, int mean, int standardDeviation) {
    int gauss = (int) (rand.nextGaussian() * standardDeviation + mean);
    return gauss < 0 ? 0 : gauss;
  }

  public static <ResourcePoolT extends ResourcePool> void main(String[] args) throws IOException {
    CmdLineUtil<ResourcePoolT, ProtocolBuilderNumeric> cmdUtil = new CmdLineUtil<>();
    CommandLine cmd = cmdUtil.parse(args);
    NetworkConfiguration networkConfiguration = cmdUtil.getNetworkConfiguration();
    List<Order> inputBuy = new ArrayList<>();
    List<Order> inputSell = new ArrayList<>();
    if (networkConfiguration.getMyId() == 1) {
      // For the simple case we simply have one party supply all orders in plain
      inputBuy = buyOrders;
      inputSell = sellOrders;
    }
    OrderMatchingDemo orderDemo = new OrderMatchingDemo(networkConfiguration.getMyId(),
        buyOrders.size(), inputBuy, sellOrders.size(), inputSell);
    SecureComputationEngine<ResourcePoolT, ProtocolBuilderNumeric> sce = cmdUtil.getSce();
    cmdUtil.startNetwork();
    ResourcePoolT resourcePool = cmdUtil.getResourcePool();
    List<OrderMatch> orders = sce.runApplication(orderDemo, resourcePool, cmdUtil.getNetwork());
    System.out.println("Orders are:");
    for (OrderMatch currentOrder : orders) {
      System.out.println("Transfer between " + currentOrder.firstId + " and " + currentOrder.secondId +
          ". With rate " + currentOrder.rate + ".");
    }
    cmdUtil.closeNetwork();
    sce.shutdownSCE();
  }
}
