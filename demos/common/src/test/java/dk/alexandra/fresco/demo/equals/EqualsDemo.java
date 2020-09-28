package dk.alexandra.fresco.demo.equals;

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
import dk.alexandra.fresco.lib.common.compare.Comparison;
import dk.alexandra.fresco.lib.common.compare.DefaultComparison;
import java.io.IOException;
import java.math.BigInteger;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple demo revealing only if the provided values by the two parties are equals.
 */
public class EqualsDemo implements Application<BigInteger, ProtocolBuilderNumeric> {

  private static Logger log = LoggerFactory.getLogger(EqualsDemo.class);

  private int myId;
  private int myX; 

  /**
   * Construct a new EqualsDemo.
   * @param id The party id
   * @param x The value
   */
  public EqualsDemo(int id, int x) {
    this.myId = id;
    this.myX = x;
  }

  @Override
  public DRes<BigInteger> buildComputation(ProtocolBuilderNumeric producer) {
    return producer.par(par -> {
      Numeric numericIo = par.numeric();
      DRes<SInt> x1 = (myId == 1)
          ? numericIo.input(BigInteger.valueOf(myX), 1) : numericIo.input(null, 1);
      DRes<SInt> x2 = (myId == 2)
          ? numericIo.input(BigInteger.valueOf(myX), 2) : numericIo.input(null, 2);
      Pair<DRes<SInt>, DRes<SInt>> input = new Pair<>(x1, x2);
      return () -> input;
    }).seq((seq, input) -> {
      DRes<SInt> equals = Comparison.using(seq).equals(32, input.getFirst(), input.getSecond());
      DRes<BigInteger> open = seq.numeric().open(equals);
      return open;
    });
  }

  /**
   * Main method for EqualsDemo.
   * @param args Arguments for the application
   * @throws IOException In case of network problems
   */
  public static <ResourcePoolT extends ResourcePool> void main(String[] args) throws IOException {
    CmdLineUtil<ResourcePoolT, ProtocolBuilderNumeric> cmdUtil = new CmdLineUtil<>();
    int x = 0;
    cmdUtil.addOption(Option.builder("x").desc("The integer x value of this party. "
        + "Note only party 1 and 2 should supply this input.").hasArg().build());
    CommandLine cmd = cmdUtil.parse(args);
    NetworkConfiguration networkConfiguration = cmdUtil.getNetworkConfiguration();

    if (networkConfiguration.getMyId() == 1 || networkConfiguration.getMyId() == 2) {
      if (!cmd.hasOption("x")) {
        cmdUtil.displayHelp();
        throw new IllegalArgumentException("Party 1 and 2 must submit input");
      } else {
        x = Integer.parseInt(cmd.getOptionValue("x"));
      }
    } else {
      if (cmd.hasOption("x")) {
        throw new IllegalArgumentException("Only party 1 and 2 should submit input");
      }
    }

    EqualsDemo equalsDemo = new EqualsDemo(networkConfiguration.getMyId(), x);
    SecureComputationEngine<ResourcePoolT, ProtocolBuilderNumeric> sce = cmdUtil.getSce();
    ResourcePoolT resourcePool = cmdUtil.getResourcePool();
    BigInteger equals = sce.runApplication(equalsDemo, resourcePool, cmdUtil.getNetwork());
    log.info("The provided values are " + (equals.equals(BigInteger.ZERO) ? "NOT" : "") + "equal");
    cmdUtil.closeNetwork();
    sce.shutdownSCE();
  }
}
