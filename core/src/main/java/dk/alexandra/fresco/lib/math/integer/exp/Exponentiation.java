package dk.alexandra.fresco.lib.math.integer.exp;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;

/**
 * Computes the exponentiation of a number. We require the knowledge of the maximum bit size of the
 * exponent.
 */
public class Exponentiation implements Computation<SInt, ProtocolBuilderNumeric> {

  private final DRes<SInt> input;
  private final DRes<SInt> exponent;
  private final int maxExponentBitLength;

  public Exponentiation(DRes<SInt> input, DRes<SInt> exponent,
      int maxExponentBitLength) {
    this.input = input;
    this.exponent = exponent;
    this.maxExponentBitLength = maxExponentBitLength;
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.seq((seq) ->
        seq.advancedNumeric().toBits(exponent, maxExponentBitLength)
    ).seq((seq, bits) -> {
      DRes<SInt> e = input;
      DRes<SInt> result = null;
      Numeric numeric = seq.numeric();
      for (SInt bit : bits) {
        /*
         * result += bits[i] * (result * r - r) + r
				 *
				 *  aka.
				 *
				 *            result       if bits[i] = 0
				 * result = {
				 *            result * e   if bits[i] = 1
				 */
        if (result == null) {
          DRes<SInt> sub = numeric.sub(e, BigInteger.ONE);
          result = numeric.add(BigInteger.ONE, numeric.mult(() -> bit, sub));
        } else {
          DRes<SInt> sub = numeric.sub(numeric.mult(result, e), result);
          result = numeric.add(result, numeric.mult(() -> bit, sub));
        }
        e = numeric.mult(e, e);
      }
      return result;
    });
  }
}