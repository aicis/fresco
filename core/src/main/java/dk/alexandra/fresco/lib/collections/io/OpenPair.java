package dk.alexandra.fresco.lib.collections.io;

import java.math.BigInteger;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.ComputationParallel;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;

/**
 * Implements a open operation on a pair of Computation<SInt>.
 */
public class OpenPair implements
    ComputationParallel<Pair<DRes<BigInteger>, DRes<BigInteger>>, ProtocolBuilderNumeric> {

  private final DRes<Pair<DRes<SInt>, DRes<SInt>>> closedPair;

  /**
   * Makes a new OpenPair
   *
   * @param closedPair the pair to open.
   */
  public OpenPair(DRes<Pair<DRes<SInt>, DRes<SInt>>> closedPair) {
    super();
    this.closedPair = closedPair;
  }

  @Override
  public DRes<Pair<DRes<BigInteger>, DRes<BigInteger>>> buildComputation(
      ProtocolBuilderNumeric par) {
    Pair<DRes<SInt>, DRes<SInt>> closedPairOut = closedPair.out();
    Numeric nb = par.numeric();
    Pair<DRes<BigInteger>, DRes<BigInteger>> openPair =
        new Pair<>(nb.open(closedPairOut.getFirst()), nb.open(closedPairOut.getSecond()));
    return () -> openPair;
  }

}
