package dk.alexandra.fresco.lib.compare.zerotest;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;

/**
 * testing for equality with zero for a bitLength-bit number (positive or negative).
 *
 */
public class ZeroTestConstRounds implements Computation<SInt, ProtocolBuilderNumeric> {

  private final DRes<SInt> input;
  private final int maxBitlength;

  public ZeroTestConstRounds(DRes<SInt> input, int maxBitlength) {
    this.input = input;
    this.maxBitlength = maxBitlength;
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder) {
    final int statisticalSecurity = builder.getBasicNumericContext().getStatisticalSecurityParam();
    DRes<SInt> reduced = builder.seq(new ZeroTestReducer(maxBitlength, input, statisticalSecurity));
    return builder.seq(new ZeroTestBruteforce(maxBitlength, reduced));
  }
}
