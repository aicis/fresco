package dk.alexandra.fresco.demo;

import static java.lang.System.exit;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
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
//      List<DRes<List<DRes<SInt>>>> res = new ArrayList<>();
      List<Pair<DRes<SInt>, List<DRes<SInt>>>> sortedBuys = input.getFirst().out();
      List<Pair<DRes<SInt>, List<DRes<SInt>>>> sortedSells = input.getSecond().out();
      int minListSize = Math.min(sortedBuys.size(), sortedSells.size());
      List<DRes<SInt>> conditions = new ArrayList<>();
      for (int i = 0; i < minListSize; i++) {
        final int currentIdx = i;
        // Construct list as a keyed pair based on the buyer ID, to check if it is blank
//        res.add(par.seq( (seq) -> {
        // Select the user ID of the buyer if the buy price is >= sell price
        DRes<SInt> condition = par.comparison()
            .compareLT(sortedBuys.get(currentIdx).getFirst(),
                sortedSells.get(minListSize - currentIdx - 1).getFirst());
        conditions.add(condition);
      }
      return () -> new Pair<>(conditions, Arrays.asList(sortedBuys, sortedSells));
    }).par((par, input) -> {
      List<Pair<DRes<SInt>, List<DRes<SInt>>>> sortedBuys = input.getSecond().get(0);
      List<Pair<DRes<SInt>, List<DRes<SInt>>>> sortedSells = input.getSecond().get(1);
      int minListSize = Math.min(sortedBuys.size(), sortedSells.size());
      List<DRes<SInt>> buyers = new ArrayList<>();
      List<DRes<SInt>> sellers = new ArrayList<>();
      List<DRes<SInt>> prices = new ArrayList<>();
      for (int i = 0; i < input.getFirst().size(); i++) {
        DRes<SInt> currentBuyer = par.advancedNumeric()
            .condSelect(input.getFirst().get(i), blankVal, sortedBuys.get(i).getSecond().get(0));
        DRes<SInt> currentSeller = par.advancedNumeric()
            .condSelect(input.getFirst().get(i), blankVal,
                sortedSells.get(minListSize - i - 1).getSecond().get(0));
        // Compute the average of buy and sell price
        DRes<SInt> price = par.numeric().add(sortedBuys.get(i).getFirst(),
            sortedSells.get(minListSize - i - 1).getFirst());
        DRes<SInt> hiddenPrice = par.advancedNumeric()
            .condSelect(input.getFirst().get(i), blankVal, price);
        buyers.add(currentBuyer);
        sellers.add(currentSeller);
        prices.add(hiddenPrice);
      }
      return () -> Arrays.asList(buyers, sellers, prices);
//    }));

    }).par((par, input) -> {
      List<List<DRes<BigInteger>>> temp = input.stream().map(current ->
          current.stream().map(internal -> par.numeric().open(internal)).collect(Collectors.toList())).
          collect(Collectors.toList());
      return () -> temp.stream().map(current ->
          current.stream().map(internal -> internal.out()).collect(Collectors.toList()))
          .collect(Collectors.toList());
    }).par((par, input) -> {
      // Clean up the result by removing "blank" elements in the list and computing the actual price
      List<OrderMatch> res = new ArrayList<>();
      for (int i = 0; i < input.get(0).size(); i++) {
        if (!input.get(0).get(i).equals(max)) {
          res.add(new OrderMatch(input.get(0).get(i).intValueExact(), input.get(1).get(i).intValueExact(),
                  input.get(2).get(i).shiftRight(1).intValueExact()));
        }
      }
      return () -> res;
    });
  }

  /** FOLLOWING CODE FOR BENCHMARKING **/
  static final int[] AMOUNTS = {4, 8, 16, 32, 64, 128};
  static final int WARM_UP = 3;
  static final int ITERATIONS = 3;

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

  private static <ResourcePoolT extends ResourcePool> List<Long> runExperiment(
      int amount, CmdLineUtil<ResourcePoolT, ProtocolBuilderNumeric> cmdUtil) {
    NetworkConfiguration networkConfiguration = cmdUtil.getNetworkConfiguration();
    List<Order> inputBuy = new ArrayList<>();
    List<Order> inputSell = new ArrayList<>();
    if (networkConfiguration.getMyId() == 1) {
      // For the simple case we simply have one party supply all orders in plain
      inputBuy = generateOrders(amount, true);
      inputSell = generateOrders(amount, false);
    }
    OrderMatchingDemo orderDemo = new OrderMatchingDemo(networkConfiguration.getMyId(),
        amount, inputBuy, amount, inputSell);
    SecureComputationEngine<ResourcePoolT, ProtocolBuilderNumeric> sce = cmdUtil.getSce();
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
    return times;
  }

  public static <ResourcePoolT extends ResourcePool> void main(String[] args) throws IOException {
    CmdLineUtil<ResourcePoolT, ProtocolBuilderNumeric> cmdUtil = new CmdLineUtil<>();
    cmdUtil.parse(args);
    cmdUtil.startNetwork();
    Map<Integer, List<Long>> times = new HashMap<>();
    for (int amount : AMOUNTS) {
      List<Long> currentTimes = runExperiment(amount, cmdUtil);
      times.put(amount, currentTimes);
    }
    // Print result
    for (int currentAmount : times.keySet()) {
      System.out.println(currentAmount + ", " + mean(times.get(currentAmount)) +
          ", " + std(times.get(currentAmount)));
    }
    cmdUtil.closeNetwork();
    cmdUtil.getSce().shutdownSCE();
    exit(0);
  }
}
