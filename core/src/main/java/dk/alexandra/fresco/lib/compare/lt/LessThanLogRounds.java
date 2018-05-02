package dk.alexandra.fresco.lib.compare.lt;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;

/**
 * Given two secret values a and b computes a < b.
 */
public class LessThanLogRounds implements Computation<SInt, ProtocolBuilderNumeric> {
  private final DRes<SInt> left;
  private final DRes<SInt> right;
  private final int maxLength;
  private final int securityParameter;

  public LessThanLogRounds(DRes<SInt> left, DRes<SInt> right, int maxLength,
      int securityParameter) {
    this.left = left;
    this.right = right;
    this.maxLength = maxLength;
    this.securityParameter = securityParameter;
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder) {
    DRes<SInt> difference = builder.numeric().sub(left, right);
    return builder.seq(new LessThanZero(difference, maxLength,
        securityParameter));
  }

}
