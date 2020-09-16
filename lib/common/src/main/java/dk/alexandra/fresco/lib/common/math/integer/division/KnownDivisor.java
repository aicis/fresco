package dk.alexandra.fresco.lib.common.math.integer.division;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.builder.numeric.field.FieldDefinition;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.compare.Comparison;
import dk.alexandra.fresco.lib.common.compare.DefaultComparison;
import dk.alexandra.fresco.lib.common.math.AdvancedNumeric;
import dk.alexandra.fresco.lib.common.math.DefaultAdvancedNumeric;
import dk.alexandra.fresco.lib.field.integer.BasicNumericContext;
import java.math.BigInteger;

/**
 * This protocol is an implementation of Euclidean division on integers with a secret shared
 * dividend and a known divisor. The dividend must have bit length smaller than half the
 * maxBitLength (available via {@link ProtocolBuilderNumeric#getBasicNumericContext().getMaxBitLength()})
 * in order for the division protocol to produce a precise result.
 */
public class KnownDivisor implements Computation<SInt, ProtocolBuilderNumeric> {

  private final DRes<SInt> dividend;
  private final BigInteger divisor;

  /**
   * Constructs a division computation where the divisor is publicly known.
   *
   * @param dividend the dividend
   * @param divisor  the publicly known divisor
   */
  public KnownDivisor(DRes<SInt> dividend, BigInteger divisor) {
    this.dividend = dividend;
    this.divisor = divisor;
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.seq(seq -> {

      BasicNumericContext basicNumericContext = seq.getBasicNumericContext();
      Numeric numeric = seq.numeric();

      /*
       * We use the fact that if 2^{N+l} \leq m * d \leq 2^{N+l} + 2^l, then floor(x/d) = floor(x * m
       * >> N+l) for all x of length <= N (see Thm 4.2 of
       * "Division by Invariant Integers using Multiplication" by Granlund and Montgomery).
       */

      /*
       * Numbers larger than half the field size is considered to be negative.
       */
      FieldDefinition fieldDefinition = basicNumericContext.getFieldDefinition();
      BigInteger signedDivisor = fieldDefinition.convertToSigned(divisor);
      int divisorSign = signedDivisor.signum();
      BigInteger divisorAbs = signedDivisor.abs();

      int n = basicNumericContext.getMaxBitLength() / 2;

      /*
       * Compute the sign of the dividend
       */
      Comparison comparison = new DefaultComparison(seq);
      DRes<SInt> dividendSign = comparison.sign(dividend);
      DRes<SInt> dividendAbs = numeric.mult(dividend, dividendSign);

      /*
       * We need m * d \geq 2^{N+l}, so we add one to the result of the division to ensure that this
       * is indeed the case.
       */
      BigInteger m = BigInteger.ONE.shiftLeft(2 * n - 1).divide(divisorAbs)
          .add(BigInteger.ONE);

      /*
       * Represent m in base 2^n.
       */
      BigInteger m0 = m.mod(BigInteger.ONE.shiftLeft(n));
      BigInteger m1 = m.mod(BigInteger.ONE.shiftLeft(2 * n)).shiftRight(n);

      /*
       * Compute the product m * dividend in this base. Note that we may have overflow.
       */
      DRes<SInt> mx0 = seq.numeric().mult(m0, dividendAbs);
      DRes<SInt> mx1 = seq.numeric().mult(m1, dividendAbs);

      /*
       * Now, the quotient is the result shifted SHIFTS bits to the left, so we shift it back to get the
       * result in absolute value. Only the most significant bits of mx0 may influence mx1 so a truncate
       * is sufficient here.
       */
      AdvancedNumeric advancedNumeric = new DefaultAdvancedNumeric(seq);
      DRes<SInt> q = advancedNumeric
          .rightShift(seq.numeric().add(mx1, advancedNumeric.truncate(mx0, n)), n - 1);

      /*
       * Adjust the sign of the result.
       */
      BigInteger openDivisorSign = BigInteger.valueOf(divisorSign);
      DRes<SInt> sign = numeric.mult(openDivisorSign, dividendSign);
      return numeric.mult(q, sign);
    });
  }
}
