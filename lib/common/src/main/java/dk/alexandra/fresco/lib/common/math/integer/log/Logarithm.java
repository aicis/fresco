package dk.alexandra.fresco.lib.common.math.integer.log;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.math.AdvancedNumeric;
import dk.alexandra.fresco.lib.common.math.DefaultAdvancedNumeric;
import java.math.BigInteger;

/**
 * This class implements a protocol for finding the natural logarithm of a
 * secret shared integer. It is based on approximating the logarithm of base 2
 * using the bitlength of a number and then scaling it to the natural logarithm.
 * <p>
 * Since the bitlength of a number is only an approximation of the logarithm of
 * base 2, this protocol is not nessecarily correct on the least significant
 * bit.
 * </p>
 */
public class Logarithm implements Computation<SInt, ProtocolBuilderNumeric> {

  // Input
  private DRes<SInt> input;
  private int maxInputLength;


  public Logarithm(DRes<SInt> input, int maxInputLength) {
    this.input = input;
    this.maxInputLength = maxInputLength;
  }


  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder) {
    /*
     * ln(2) = 45426 >> 16;
     */
    BigInteger ln2 = BigInteger.valueOf(45426);
    int shifts = 16;

    /*
     * Find the bit length of the input. Note that bit length - 1 is the floor of the the logartihm
     * with base 2 of the input.
     */
    AdvancedNumeric advancedNumeric = new DefaultAdvancedNumeric(builder);
    DRes<SInt> bitLength = advancedNumeric.bitLength(input, maxInputLength);
    DRes<SInt> log2 = builder.numeric().sub(bitLength, BigInteger.ONE);

    /*
     * ln(x) = log_2(x) * ln(2), and we use 45426 >> 16 as an approximation of ln(2).
     */
    DRes<SInt> scaledLog = builder.numeric().mult(ln2, log2);
    return advancedNumeric.rightShift(scaledLog, shifts);
  }
}
