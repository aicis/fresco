package dk.alexandra.fresco.lib.math.integer.division;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericContext;
import java.math.BigInteger;


/**
 * This protocol is an implementation Euclidean division (finding quotient and
 * remainder) on integers with a secret shared divedend and a known divisor. In
 * the implementation we calculate a constant <i>m</i> such that multiplication
 * with <i>m</i> will correspond to the desired division -- just shifted a
 * number of bits to the left. To get the right result we just need to shift
 * back again.
 *
 */
public class KnownDivisor implements Computation<SInt, ProtocolBuilderNumeric> {

  private final DRes<SInt> dividend;
  private final BigInteger divisor;

  public KnownDivisor(
      DRes<SInt> dividend,
      BigInteger divisor) {

    this.dividend = dividend;
    this.divisor = divisor;
  }

  private BigInteger convertRepresentation(
      BigInteger modulus,
      BigInteger modulusHalf,
      BigInteger b) {
    // Stolen from Spdz Util
    BigInteger actual = b.mod(modulus);
    if (actual.compareTo(modulusHalf) > 0) {
      actual = actual.subtract(modulus);
    }
    return actual;
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder) {
    BasicNumericContext basicNumericContext = builder.getBasicNumericContext();
    BigInteger modulus = basicNumericContext.getModulus();
    BigInteger modulusHalf = modulus.divide(BigInteger.valueOf(2));
    /*
     * We use the fact that if 2^{N+l} \leq m * d \leq 2^{N+l} + 2^l, then floor(x/d) = floor(x * m
     * >> N+l) for all x of length <= N (see Thm 4.2 of
     * "Division by Invariant Integers using Multiplication" by Granlund and Montgomery).
     * 
     * TODO: Note that if the dividend is nonnegative, the sign considerations can be omitted,
     * giving a significant speed-up.
     */

    Numeric numeric = builder.numeric();
    /*
     * Numbers larger than half the field size is considered to be negative.
     * 
     * TODO: This should be handled differently because it will not necessarily work with another
     * arithmetic protocol suite.
     */
    BigInteger signedDivisor = convertRepresentation(modulus, modulusHalf, divisor);
    int divisorSign = signedDivisor.signum();
    BigInteger divisorAbs = signedDivisor.abs();

    /*
     * The quotient will have bit length < 2 * maxBitLength, and this has to be shifted maxBitLength
     * + divisorBitLength. So in total we need 3 * maxBitLength + divisorBitLength to be
     * representable.
     */
    int maxBitLength =
        (builder.getBasicNumericContext().getMaxBitLength() - divisorAbs.bitLength()) / 3;
    int shifts = maxBitLength + divisorAbs.bitLength();

    /*
     * Compute the sign of the dividend
     */
    DRes<SInt> dividendSign = builder.comparison().sign(dividend);
    DRes<SInt> dividendAbs = numeric.mult(dividend, dividendSign);

    /*
     * We need m * d \geq 2^{N+l}, so we add one to the result of the division to ensure that this
     * is indeed the case.
     */
    BigInteger m = BigInteger.ONE.shiftLeft(shifts).divide(divisorAbs).add(BigInteger.ONE);
    DRes<SInt> quotientAbs = numeric.mult(m, dividendAbs);

    /*
     * Now quotientAbs is the result shifted SHIFTS bits to the left, so we shift it back to get the
     * result in absolute value, q.
     */
    DRes<SInt> q = builder.advancedNumeric().rightShift(quotientAbs, shifts);

    /*
     * Adjust the sign of the result.
     */
    BigInteger oInt = BigInteger.valueOf(divisorSign);
    DRes<SInt> sign = numeric.mult(oInt, dividendSign);
    return numeric.mult(q, sign);
  }
}
