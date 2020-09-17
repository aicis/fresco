package dk.alexandra.fresco.demo;

import dk.alexandra.fresco.demo.cli.CmdLineUtil;
import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.sce.SecureComputationEngine;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import org.apache.commons.cli.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//enum Currency {
//  ETH,
//  BTC
//}

/**
 * Compute the order matching demo for exchange.
 * Currently every party inputs a list of order parameters that then get matched up in MPC.
 * The result is a list of transaction pairs which must be carried out.
 * We currently assume that there are only two currencies and all orders are of a static amount in the pivot currency.
 * The matching is simply the closets buy/sell order which are within each other's buy/sell limits and
 * an valuation of the exchange which is the average of the min/max price of the order.
 * Unmatched orders will not be carried out.
 * Eventually we want order to expand with quantity, validity time and choice of currency.
 * In which situation the system will keep unmatched orders and continuously add new orders, and remove
 * orders that have timed out.
 *
 * The approach leverages the fact that Odd Even merge is stable.
 */
public class OrderMatchingDemo implements
    Application<List<OrderMatch>, ProtocolBuilderNumeric> {

  private static Logger log = LoggerFactory.getLogger(OrderMatchingDemo.class);

  private final int serverId;
  private final int buySize;
  private final List<Order> buyOrders;
  private final int sellSize;
  private final List<Order> sellOrders;

  // TODO should add quantity, deadline and enum for currency to match against
  // Right now we assume that all transactions must be of a static amount in a a base currency
  // and there is only one other currency to exchange against, and then the quantity and rate will
  // be directly depended.

  /**
   * NEED: the orders to be sorted according to user ID. That is in chronological order.
   * This is not secret since in the outsourced setting all servers know when they receive an
   * outsourced order by a given user.
   * @param serverId
   * @param buySize
   * @param buyOrders
   * @param sellSize
   * @param sellOrders
   */
  public OrderMatchingDemo(int serverId, int buySize, List<Order> buyOrders, int sellSize, List<Order> sellOrders) {
    if (serverId == 1 && (buySize != buyOrders.size() || sellSize != sellOrders.size())) {
      throw new IllegalArgumentException("Size must match list of orders from player 1");
    }
    this.serverId = serverId;
    this.buySize = buySize;
    this.buyOrders = new ArrayList<>(buyOrders);
    this.sellSize = sellSize;
    this.sellOrders = new ArrayList<>(sellOrders);
    // This is needed since the internal MPC sorting will leave the largest first
//    Collections.reverse(this.buyOrders);
  }

  @Override
  public DRes<List<OrderMatch>> buildComputation(ProtocolBuilderNumeric builder) {
    final BigInteger max = BigInteger.ONE.shiftLeft(builder.getBasicNumericContext().getMaxBitLength()).subtract(BigInteger.ONE);
    // We use the maximal integer to be used as indicator for a non-match
    final DRes<SInt> blankVal = builder.numeric().known(max);
    return builder.par(par -> {
      // Input values
      List<Pair<DRes<SInt>, List<DRes<SInt>>>> buys = new ArrayList<>();
      List<Pair<DRes<SInt>, List<DRes<SInt>>>> sells = new ArrayList<>();
      for (int i = 0; i < buySize; i++) {
        DRes<SInt> currentUserId = serverId == 1 ? par.numeric().input(BigInteger.valueOf(buyOrders.get(i).userId), 1) :
            par.numeric().input(null, 1);
        DRes<SInt> currentBuyRate = serverId == 1 ? par.numeric().input(BigInteger.valueOf(buyOrders.get(i).limitRate), 1) :
            par.numeric().input(null, 1);
        Pair<DRes<SInt>, List<DRes<SInt>>> currentBuy = new Pair<>(currentBuyRate, Arrays.asList(currentUserId));
        buys.add(currentBuy);
      }
      for (int i = 0; i < sellSize; i++) {
        DRes<SInt> currentUserId = serverId == 1 ? par.numeric().input(BigInteger.valueOf(sellOrders.get(i).userId), 1) :
            par.numeric().input(null, 1);
        DRes<SInt> currentSellRate = serverId == 1 ? par.numeric().input(BigInteger.valueOf(sellOrders.get(i).limitRate), 1) :
            par.numeric().input(null, 1);
        Pair<DRes<SInt>, List<DRes<SInt>>> currentSell = new Pair<>(currentSellRate, Arrays.asList(currentUserId));
        sells.add(currentSell);
      }
      return () -> new Pair<List<Pair<DRes<SInt>, List<DRes<SInt>>>>, List<Pair<DRes<SInt>, List<DRes<SInt>>>>>(buys, sells);
    }).par((par, input) -> {
      DRes<List<Pair<DRes<SInt>, List<DRes<SInt>>>>> sortedBuys = par.collections().sort(input.getFirst());
      DRes<List<Pair<DRes<SInt>, List<DRes<SInt>>>>> sortedSells = par.collections().sort(input.getSecond());
      return () -> new Pair<>(sortedBuys, sortedSells);
    }).par((par, input) -> {
      List<DRes<List<DRes<SInt>>>> res = new ArrayList<>();
      List<Pair<DRes<SInt>, List<DRes<SInt>>>> sortedBuys = input.getFirst().out();
      List<Pair<DRes<SInt>, List<DRes<SInt>>>> sortedSells = input.getSecond().out();
      int minListSize = Math.min(sortedBuys.size(), sortedSells.size());
      for (int i = 0; i < minListSize; i++) {
        final int currentIdx = i;
        // Construct list as a keyed pair based on the buyer ID, to check if it is blank
        res.add(par.seq( (seq) -> {
          // Select the user ID of the buyer if the buy price is >= sell price
        DRes<SInt> condition = seq.comparison()
            .compareLT(sortedBuys.get(currentIdx).getFirst(), sortedSells.get(minListSize-currentIdx-1).getFirst());
        DRes<SInt> currentBuyer = seq.advancedNumeric()
            .condSelect(condition, blankVal, sortedBuys.get(currentIdx).getSecond().get(0));
        DRes<SInt> currentSeller = seq.advancedNumeric()
            .condSelect(condition, blankVal, sortedSells.get(minListSize-currentIdx-1).getSecond().get(0));
        // Compute the average of buy and sell price
        DRes<SInt> price = seq.numeric().add(sortedBuys.get(currentIdx).getFirst(),
            sortedSells.get(minListSize-currentIdx-1).getFirst());
        DRes<SInt> hiddenPrice = seq.advancedNumeric()
            .condSelect(condition, blankVal, price);
        return () -> Arrays.asList(currentBuyer, currentSeller, hiddenPrice);
        }));
      }
      return () -> res;
    }).par((par, input) -> {
      List<List<DRes<BigInteger>>> temp = input.stream().map(current ->
          current.out().stream().map(internal -> par.numeric().open(internal)).collect(Collectors.toList())).
          collect(Collectors.toList());
      return () -> temp.stream().map(current ->
          current.stream().map(internal -> internal.out()).collect(Collectors.toList()))
          .collect(Collectors.toList());
    }).par((par, input) -> {
      // Clean up the result by removing "blank" elements in the list and computing the actual price
      List<OrderMatch> res = new ArrayList<>();
      for (int i = 0; i < input.size(); i++) {
        if (!input.get(i).get(0).equals(max)) {
          res.add(new OrderMatch(input.get(i).get(0).intValueExact(), input.get(i).get(1).intValueExact(),
                  input.get(i).get(2).shiftRight(1).intValueExact()));

        }
      }
      return () -> res;
    });
  }

  /** FOLLOWING CODE FOR BENCHMARKING **/
  static final int AMOUNT_OF_ORDERS = 64;
  static final int WARM_UP = 30;
  static final int ITERATIONS = 30;

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

  private static double mean(List<Long> times) {
    return ((double) times.stream().mapToInt(t -> t.intValue()).sum())/ITERATIONS;
  }

  private static double std(List<Long> times ) {
    double mean = mean(times);
    double temp = 0.0;
    for (long current : times) {
      temp += (((double)current) - mean)*(((double)current) - mean);
    }
    return Math.sqrt(temp/((double)(ITERATIONS - 1)));
  }

  public static <ResourcePoolT extends ResourcePool> void main(String[] args) throws IOException {
    CmdLineUtil<ResourcePoolT, ProtocolBuilderNumeric> cmdUtil = new CmdLineUtil<>();
    CommandLine cmd = cmdUtil.parse(args);
    NetworkConfiguration networkConfiguration = cmdUtil.getNetworkConfiguration();
    List<Order> inputBuy = new ArrayList<>();
    List<Order> inputSell = new ArrayList<>();
    if (networkConfiguration.getMyId() == 1) {
      // For the simple case we simply have one party supply all orders in plain
      inputBuy = generateOrders(AMOUNT_OF_ORDERS, true);
      inputSell = generateOrders(AMOUNT_OF_ORDERS, false);
    }
    OrderMatchingDemo orderDemo = new OrderMatchingDemo(networkConfiguration.getMyId(),
        AMOUNT_OF_ORDERS, inputBuy, AMOUNT_OF_ORDERS, inputSell);
    SecureComputationEngine<ResourcePoolT, ProtocolBuilderNumeric> sce = cmdUtil.getSce();
    cmdUtil.startNetwork();
    ResourcePoolT resourcePool = cmdUtil.getResourcePool();
    List<Long> times = new ArrayList<>();
    for (int i = 0; i < WARM_UP+ITERATIONS; i++) {
      long start = System.currentTimeMillis();
      List<OrderMatch> orders = sce.runApplication(orderDemo, resourcePool, cmdUtil.getNetwork());
      long end = System.currentTimeMillis();
      System.out.println("Size: " + orders.size());
      if (i >= WARM_UP) {
        times.add(end - start);
      }
    }
    System.out.println("Average time :" + mean(times) + ", std:" + std(times));
//    System.out.println("Orders are:");
//    for (OrderMatch currentOrder : orders) {
//      System.out.println("Transfer between " + currentOrder.firstId + " and " + currentOrder.secondId +
//          ". With rate " + currentOrder.rate + ".");
//    }
    cmdUtil.closeNetwork();
    sce.shutdownSCE();
  }
}
