package dk.alexandra.fresco.lib.compare.zerotest;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;

/**
 * testing for equality with zero for a bitLength-bit number (positive or negative).
 *
 */
public class ZeroTest implements Computation<SInt, ProtocolBuilderNumeric> {

  private final int securityParameter;
  private final int bitLength;
  private final DRes<SInt> input;

  public ZeroTest(int bitLength, DRes<SInt> input, int securityParameter) {
    this.bitLength = bitLength;
    this.input = input;
    this.securityParameter = securityParameter;
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder) {
    DRes<SInt> reduced = builder.seq(new ZeroTestReducer(bitLength, input, securityParameter));
    return builder.seq(new ZeroTestBruteforce(bitLength, reduced));
  }
}
