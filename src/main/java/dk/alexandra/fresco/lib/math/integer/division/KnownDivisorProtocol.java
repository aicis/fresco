package dk.alexandra.fresco.lib.math.integer.division;

import java.math.BigInteger;

import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.helper.AbstractSimpleProtocol;
import dk.alexandra.fresco.lib.helper.builder.OmniBuilder;
import dk.alexandra.fresco.lib.math.integer.binary.RightShiftFactory;
import dk.alexandra.fresco.suite.spdz.utils.Util;

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

	// Input
	private SInt dividend;
	private OInt divisor;
	private SInt result, remainder;

	// Factories
	private final BasicNumericFactory basicNumericFactory;
	private final RightShiftFactory rightShiftFactory;

	public KnownDivisorProtocol(SInt dividend, OInt divisor, SInt result, BasicNumericFactory basicNumericFactory,
			RightShiftFactory rightShiftFactory) {
		this.dividend = dividend;
		this.divisor = divisor;
		this.result = result;

		this.basicNumericFactory = basicNumericFactory;
		this.rightShiftFactory = rightShiftFactory;
	}

	public KnownDivisorProtocol(SInt x, OInt divisor, SInt result, SInt remainder,
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

		builder.beginSeqScope();
		
		/*
		 * Numbers larger than half the field size is considered to be negative.
		 * 
		 * TODO: This should be handled differently because it will not
		 * necessarily work with another arithmetic protocol suite.
		 */
		BigInteger signedDivisor = Util.convertRepresentation(divisor.getValue());
		int divisorSign = signedDivisor.signum();
		BigInteger divisorAbs = signedDivisor.abs();

		/*
		 * The quotient will have length 2 * maxBitLength + divisorBitLength,
		 * and this has to be shifted maxBitLength + divisorBitLength. So in
		 * total we need 3 * maxBitLength + 2 * divisorBitLength to be
		 * representable.
		 */
		int maxBitLength = (basicNumericFactory.getMaxBitLength() - 2 * divisorAbs.bitLength()) / 3;
		int shifts = maxBitLength + divisorAbs.bitLength();

		/*
		 * Compute the sign of the dividend
		 */
		SInt dividendSign = builder.getComparisonProtocolBuilder().sign(dividend);
		SInt dividendAbs = builder.getNumericProtocolBuilder().mult(dividend, dividendSign);

		/*
		 * We need m * d \geq 2^{N+l}, so we add one to the result of the
		 * division to ensure that this is indeed the case.
		 */
		OInt m = builder.getNumericProtocolBuilder()
				.knownOInt(BigInteger.ONE.shiftLeft(shifts).divide(divisorAbs).add(BigInteger.ONE));
		SInt quotientAbs = builder.getNumericProtocolBuilder().mult(m, dividendAbs);

		SInt q = builder.getNumericProtocolBuilder().getSInt();
		builder.addProtocolProducer(rightShiftFactory.getRepeatedRightShiftProtocol(quotientAbs, shifts, q));

		/*
		 * Adjust sign
		 */
		SInt sign = builder.getNumericProtocolBuilder().mult(builder.getNumericProtocolBuilder().knownOInt(divisorSign),
				dividendSign);
		builder.getNumericProtocolBuilder().copy(result, builder.getNumericProtocolBuilder().mult(q, sign));

		if (remainder != null) {
			builder.getNumericProtocolBuilder().copy(remainder, builder.getNumericProtocolBuilder().sub(dividend,
					builder.getNumericProtocolBuilder().mult(divisor, result)));
		}

		builder.endCurScope();
		
		return builder.getProtocol();
	}

}
