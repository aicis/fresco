package dk.alexandra.fresco.lib.compare.lt;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.SIntPair;
import dk.alexandra.fresco.framework.value.SInt;

/**
 * Corresponds to circle operator in paper. <p>Given (p_{2}, g_{2}) and (p_{1}, g_{1}) computes (p,
 * g) where p = p_{2} AND p_{1} and g = g_{2} OR (p_{2} AND g_{1}).</p>
 */
public class CarryHelper implements Computation<SIntPair, ProtocolBuilderNumeric> {

  private final DRes<SIntPair> leftBitPair;
  private final DRes<SIntPair> rightBitPair;

  public CarryHelper(DRes<SIntPair> leftBitPair, DRes<SIntPair> rightBitPair) {
    this.leftBitPair = leftBitPair;
    this.rightBitPair = rightBitPair;
  }

  @Override
  public DRes<SIntPair> buildComputation(ProtocolBuilderNumeric builder) {
    if (leftBitPair == null) {
      return rightBitPair;
    }
    if (rightBitPair == null) {
      return leftBitPair;
    }
    SIntPair left = leftBitPair.out();
    SIntPair right = rightBitPair.out();
    DRes<SInt> p1 = left.getFirst();
    DRes<SInt> g1 = left.getSecond();
    DRes<SInt> p2 = right.getFirst();
    DRes<SInt> g2 = right.getSecond();
    return builder.par(par -> {
      DRes<SInt> p = par.logical().and(p1, p2);
      DRes<SInt> q = par.seq(seq -> {
        DRes<SInt> temp = seq.logical().and(p2, g1);
        return seq.logical().or(temp, g2);
      });
      return () -> new SIntPair(p, q);
    });
  }

}
