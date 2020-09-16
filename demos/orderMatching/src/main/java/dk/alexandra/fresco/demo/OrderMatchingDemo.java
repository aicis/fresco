package dk.alexandra.fresco.demo;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
    Application<List<List<BigInteger>>, ProtocolBuilderNumeric> {

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
  public OrderMatchingDemo(int serverId, int buySize, List<Order> buyOrders, int sellSize, List<Order> sellOrders) {
    if (serverId == 1 && (buySize != buyOrders.size() || sellSize != sellOrders.size())) {
      throw new IllegalArgumentException("Size must match list of orders from player 1");
    }
    this.serverId = serverId;
    this.buySize = buySize;
    this.buyOrders = buyOrders;
    this.sellSize = sellSize;
    this.sellOrders = sellOrders;
  }

  @Override
  public DRes<List<List<BigInteger>>> buildComputation(ProtocolBuilderNumeric builder) {
    final BigInteger max = BigInteger.ONE.shiftLeft(builder.getBasicNumericContext().getMaxBitLength());
    // We use the maximal integer to be used as indicator for a non-match
    final DRes<SInt> blankVal = builder.numeric().known(max.multiply(new BigInteger("2")).subtract(BigInteger.ONE));
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
      List<List<BigInteger>> res = new ArrayList<>();
      for (int i = 0; i < input.size(); i++) {
        if (input.get(i).get(0) != max) {
          res.add(Arrays.asList(input.get(i).get(0), input.get(i).get(1),
              input.get(i).get(2).shiftRight(1)));
        }
      }
      return () -> res;
    });
  }

//  public static <ResourcePoolT extends ResourcePool> void main(String[] args) throws IOException {
//    CmdLineUtil<ResourcePoolT, ProtocolBuilderNumeric> cmdUtil = new CmdLineUtil<>();
//    int limit = 0;
//    boolean buy = false;
//    cmdUtil.addOption(Option.builder("limit").desc("Must express the limit rates for transaction"
//        + " using the option \"limit\".").hasArg().build());
////    cmdUtil.addOption(Option.builder("quantity").desc("Must express the quantity for the "
////        + "transaction using the option \"quantity\".").hasArg().build());
//    cmdUtil.addOption(Option.builder("buy").desc("Must express whether the transaction is a buy"
//        + "(or sell) transaction using the option \"buy\" followed by a list of integers, non-zero for buy and "
//        + "zero for sell.").hasArg().build());
//    CommandLine cmd = cmdUtil.parse(args);
//    NetworkConfiguration networkConfiguration = cmdUtil.getNetworkConfiguration();
//
//    if (!cmd.hasOption("limit") || !cmd.hasOption("buy")) {
//      cmdUtil.displayHelp();
//      throw new IllegalArgumentException("Parties must submit input");
//    } else {
//      limit = Integer.parseInt(cmd.getOptionValue("limit"));
//      buy = Integer.parseInt(cmd.getOptionValue("buy")) != 0 ? true : false;
//    }
//
//    OrderMatchingDemo orderDemo = new OrderMatchingDemo(networkConfiguration.getMyId(), limit, buy);
//    SecureComputationEngine<ResourcePoolT, ProtocolBuilderNumeric> sce = cmdUtil.getSce();
//    cmdUtil.startNetwork();
//    ResourcePoolT resourcePool = cmdUtil.getResourcePool();
//    List<List<BigInteger>> orders = sce.runApplication(orderDemo, resourcePool, cmdUtil.getNetwork());
//    log.info("Orders are:");
//    for (List<BigInteger> currentOrder : orders) {
//      log.info("Transfer between " + currentOrder.get(0) + " and " + currentOrder.get(1) +
//          ". With rate " + currentOrder.get(2) + ".");
//    }
//    cmdUtil.closeNetwork();
//    sce.shutdownSCE();
//  }
}
