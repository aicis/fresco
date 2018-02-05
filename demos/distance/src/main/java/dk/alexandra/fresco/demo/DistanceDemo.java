package dk.alexandra.fresco.demo;

import dk.alexandra.fresco.demo.cli.CmdLineUtil;
import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.sce.SecureComputationEngine;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import java.io.IOException;
import java.math.BigInteger;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple demo computing the distance between two secret points.
 */
public class DistanceDemo implements Application<BigInteger, ProtocolBuilderNumeric> {

  private static Logger log = LoggerFactory.getLogger(DistanceDemo.class);

  private int myId;
  private int myX; 
  private int myY;

  /**
   * Construct a new DistanceDemo.
   * @param id The party id
   * @param x The x coordinate
   * @param y The y coordinate
   */
  public DistanceDemo(int id, int x, int y) {
    this.myId = id;
    this.myX = x;
    this.myY = y;
  }

  @Override
  public DRes<BigInteger> buildComputation(ProtocolBuilderNumeric producer) {
    return producer.par(par -> {
      // Input points
      Numeric numericIo = par.numeric();
      DRes<SInt> x1 = (myId == 1)
          ? numericIo.input(BigInteger.valueOf(myX), 1) : numericIo.input(null, 1);
      DRes<SInt> y1 = (myId == 1)
          ? numericIo.input(BigInteger.valueOf(myY), 1) : numericIo.input(null, 1);
      DRes<SInt> x2 = (myId == 2)
          ? numericIo.input(BigInteger.valueOf(myX), 2) : numericIo.input(null, 2);
      DRes<SInt> y2 = (myId == 2)
          ? numericIo.input(BigInteger.valueOf(myY), 2) : numericIo.input(null, 2);
      Pair<DRes<SInt>, DRes<SInt>> input1 = new Pair<>(x1, y1);
      Pair<DRes<SInt>, DRes<SInt>> input2 = new Pair<>(x2, y2);
      Pair<Pair<DRes<SInt>, DRes<SInt>>, Pair<DRes<SInt>, DRes<SInt>>> inputs =
          new Pair<>(input1, input2);
      return () -> inputs;
    }).pairInPar((seq, input) -> {
      Numeric numeric = seq.numeric();
      DRes<SInt> differenceX = numeric.sub(input.getFirst().getFirst(),
          input.getSecond().getFirst());
      return numeric.mult(differenceX, differenceX);
    } , (seq, input) -> {
        Numeric numeric = seq.numeric();
        DRes<SInt> differenceY = numeric.sub(input.getFirst().getSecond(),
            input.getSecond().getSecond());
        return numeric.mult(differenceY, differenceY);
      }).seq((seq, distances) -> seq.numeric().add(distances.getFirst(),
          distances.getSecond()))
        .seq((seq, product) -> seq.numeric().open(product));
  }

  /**
   * Main method for DistanceDemo.
   * @param args Arguments for the application
   * @throws IOException In case of network problems
   */
  public static <ResourcePoolT extends ResourcePool> void main(String[] args) throws IOException {
    CmdLineUtil<ResourcePoolT, ProtocolBuilderNumeric> cmdUtil = new CmdLineUtil<>();
    int x = 0;
    int y = 0;
    cmdUtil.addOption(Option.builder("x").desc("The integer x coordinate of this party. "
        + "Note only party 1 and 2 should supply this input.").hasArg().build());
    cmdUtil.addOption(Option.builder("y").desc("The integer y coordinate of this party. "
        + "Note only party 1 and 2 should supply this input").hasArg().build());
    CommandLine cmd = cmdUtil.parse(args);
    NetworkConfiguration networkConfiguration = cmdUtil.getNetworkConfiguration();

    if (networkConfiguration.getMyId() == 1 || networkConfiguration.getMyId() == 2) {
      if (!cmd.hasOption("x") || !cmd.hasOption("y")) {
        cmdUtil.displayHelp();
        throw new IllegalArgumentException("Party 1 and 2 must submit input");
      } else {
        x = Integer.parseInt(cmd.getOptionValue("x"));
        y = Integer.parseInt(cmd.getOptionValue("y"));
      }
    } else {
      if (cmd.hasOption("x") || cmd.hasOption("y")) {
        throw new IllegalArgumentException("Only party 1 and 2 should submit input");
      }
    }

    DistanceDemo distDemo = new DistanceDemo(networkConfiguration.getMyId(), x, y);
    SecureComputationEngine<ResourcePoolT, ProtocolBuilderNumeric> sce = cmdUtil.getSce();
    cmdUtil.startNetwork();
    ResourcePoolT resourcePool = cmdUtil.getResourcePool();
    BigInteger bigInteger = sce.runApplication(distDemo, resourcePool, cmdUtil.getNetwork());
    double dist = Math.sqrt(bigInteger.doubleValue());
    log.info("Distance between party 1 and 2 is: " + dist);
    cmdUtil.closeNetwork();
    sce.shutdownSCE();

  }
}
