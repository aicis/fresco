package dk.alexandra.fresco.lib.math.integer.division;

import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.helper.AbstractSimpleProtocol;
import dk.alexandra.fresco.lib.helper.builder.NumericProtocolBuilder;
import dk.alexandra.fresco.lib.helper.builder.OmniBuilder;
import dk.alexandra.fresco.lib.math.integer.binary.RightShiftFactory;
import java.math.BigInteger;

/**
 * This protocol is an implementation Euclidean division (finding quotient and
 * remainder) on integers with a secret shared divedend and a known divisor. In
 * the implementation we calculate a constant <i>m</i> such that multiplication
 * with <i>m</i> will correspond to the desired division -- just shifted a
 * number of bits to the left. To get the right result we just need to shift
 * back again.
 *
 * @author Jonas Lindstrøm (jonas.lindstrom@alexandra.dk)
 */
public class KnownDivisorProtocol extends AbstractSimpleProtocol implements DivisionProtocol {

  private final BigInteger modulusHalf;
  // Input
  private SInt dividend;
  private OInt divisor;
  private SInt result, remainder;

  // Factories
  private final BasicNumericFactory basicNumericFactory;
  private final RightShiftFactory rightShiftFactory;
  private BigInteger modulus;

  KnownDivisorProtocol(SInt dividend, OInt divisor, SInt result,
      BasicNumericFactory basicNumericFactory,
      RightShiftFactory rightShiftFactory) {
    this.dividend = dividend;
    this.divisor = divisor;
    this.result = result;

    this.basicNumericFactory = basicNumericFactory;
    modulus = basicNumericFactory.getModulus();
    modulusHalf = modulus.divide(BigInteger.valueOf(2));
    this.rightShiftFactory = rightShiftFactory;
  }

  private BigInteger convertRepresentation(BigInteger b) {
    // Stolen from Spdz Util
    BigInteger actual = b.mod(modulus);
    if (actual.compareTo(modulusHalf) > 0) {
      actual = actual.subtract(modulus);
    }
    return actual;
  }

  KnownDivisorProtocol(SInt x, OInt divisor, SInt result, SInt remainder,
      BasicNumericFactory basicNumericFactory, RightShiftFactory rightShiftFactory) {
    this(x, divisor, result, basicNumericFactory, rightShiftFactory);
    this.remainder = remainder;
  }

  @Override
  protected ProtocolProducer initializeProtocolProducer() {

		/*
     * We use the fact that if 2^{N+l} \leq m * d \leq 2^{N+l} + 2^l, then
		 * floor(x/d) = floor(x * m >> N+l) for all x of length <= N (see Thm
		 * 4.2 of "Division by Invariant Integers using Multiplication" by
		 * Granlund and Montgomery).
		 * 
		 * TODO: Note that if the dividend is nonnegative, the sign
		 * considerations can be omitted, giving a significant speed-up.
		 */

    OmniBuilder builder = new OmniBuilder(basicNumericFactory);
    NumericProtocolBuilder numeric = builder.getNumericProtocolBuilder();

    builder.beginSeqScope();

		/*
     * Numbers larger than half the field size is considered to be negative.
		 * 
		 * TODO: This should be handled differently because it will not
		 * necessarily work with another arithmetic protocol suite.
		 */
    BigInteger signedDivisor = convertRepresentation(divisor.getValue());
    int divisorSign = signedDivisor.signum();
    BigInteger divisorAbs = signedDivisor.abs();

		/*
     * The quotient will have bit length < 2 * maxBitLength, and this has to
		 * be shifted maxBitLength + divisorBitLength. So in total we need 3 *
		 * maxBitLength + divisorBitLength to be representable.
		 */
    int maxBitLength = (basicNumericFactory.getMaxBitLength() - divisorAbs.bitLength()) / 3;
    int shifts = maxBitLength + divisorAbs.bitLength();

		/*
     * Compute the sign of the dividend
		 */
    SInt dividendSign = builder.getComparisonProtocolBuilder().sign(dividend);
    SInt dividendAbs = numeric.mult(dividend, dividendSign);

		/*
     * We need m * d \geq 2^{N+l}, so we add one to the result of the
		 * division to ensure that this is indeed the case.
		 */
    OInt m = numeric
        .knownOInt(BigInteger.ONE.shiftLeft(shifts).divide(divisorAbs).add(BigInteger.ONE));
    SInt quotientAbs = numeric.mult(m, dividendAbs);

		/*
		 * Now quotientAbs is the result shifted SHIFTS bits to the left, so we
		 * shift it back to get the result in absolute value, q.
		 */
    SInt q = numeric.getSInt();
    builder.addProtocolProducer(
        rightShiftFactory.getRepeatedRightShiftProtocol(quotientAbs, shifts, q));

		/*
		 * Adjust the sign of the result.
		 */
    SInt sign = builder.getNumericProtocolBuilder()
        .mult(numeric.knownOInt(divisorSign), dividendSign);
    numeric.copy(result, numeric.mult(q, sign));

		/*
		 * If the remainder is requested, we calculate it here. Note that this
		 * only makes sense if both divisor and dividend are nonnegative -
		 * otherwise the remainder could be negative.
		 */
    if (remainder != null) {
      numeric.copy(remainder, numeric.sub(dividend, numeric.mult(divisor, result)));
    }

    builder.endCurScope();

    return builder.getProtocol();
  }

}
