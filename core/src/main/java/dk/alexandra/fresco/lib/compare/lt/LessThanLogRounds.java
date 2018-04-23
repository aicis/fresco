package dk.alexandra.fresco.lib.compare.lt;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;

/**
 * Given two secret values a and b computes a < b.
 */
public class LessThanLogRounds implements Computation<SInt, ProtocolBuilderNumeric> {
  // TODO add paper reference

  private final DRes<SInt> left;
  private final DRes<SInt> right;
  private final int k;
  private final int kappa;

  public LessThanLogRounds(DRes<SInt> left, DRes<SInt> right, int k, int kappa) {
    this.left = left;
    this.right = right;
    this.k = k;
    this.kappa = kappa;
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder) {
    DRes<SInt> difference = builder.numeric().sub(left, right);
    return builder.seq(new LessThanZero(difference, k, kappa));
  }

}
