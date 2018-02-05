package dk.alexandra.fresco.lib.math.integer.exp;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;

/**
 * Computes the exponentiation when only the exponent is secret. We require the knowledge of the
 * maximum bit size of the exponent.
 */
public class ExponentiationOpenBase implements Computation<SInt, ProtocolBuilderNumeric> {

  private final BigInteger base;
  private final DRes<SInt> exponent;
  private final int maxExponentBitLength;

  public ExponentiationOpenBase(BigInteger base, DRes<SInt> exponent,
      int maxExponentBitLength) {
    this.base = base;
    this.exponent = exponent;
    this.maxExponentBitLength = maxExponentBitLength;
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.seq((seq) ->
    seq.advancedNumeric().toBits(exponent, maxExponentBitLength)
        ).seq((seq, bits) -> {
          BigInteger e = base;
          Numeric numeric = seq.numeric();
          DRes<SInt> result = null;
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
              BigInteger sub = e.subtract(BigInteger.ONE);
              result = numeric.add(BigInteger.ONE, numeric.mult(sub, () -> bit));
            } else {
              DRes<SInt> sub = numeric.sub(numeric.mult(e, result), result);
              result = numeric.add(result, numeric.mult(sub, () -> bit));
            }
            e = e.multiply(e);
          }
          return result;
        });
  }
}
