package dk.alexandra.fresco.lib.conversion;

import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.helper.AbstractSimpleProtocol;
import dk.alexandra.fresco.lib.math.integer.binary.RightShiftFactory;

public class IntegerToBitsByShiftProtocolImpl extends AbstractSimpleProtocol implements
		IntegerToBitsProtocol {

	private final SInt input;
	private final int maxInputLength;
	private final SInt[] output;

	private final BasicNumericFactory basicNumericFactory;
	private final RightShiftFactory rightShiftFactory;

	/**
	 * Create a protocol which finds the bit representation of a given integer.
	 * This is done by repeadetly shifting the input to the right, so we need to
	 * supply the number of bits we want to find as a parameter.
	 * 
	 * @param input
	 *            An integer.
	 * @param maxInputLength
	 *            The number of bits we want to find.
	 * @param output
	 *            An array of bits.
	 * @param basicNumericFactory
	 * @param rightShiftFactory
	 */
	public IntegerToBitsByShiftProtocolImpl(SInt input, int maxInputLength, SInt[] output,
			BasicNumericFactory basicNumericFactory, RightShiftFactory rightShiftFactory) {
		this.input = input;
		this.maxInputLength = maxInputLength;
		this.output = output;

		this.basicNumericFactory = basicNumericFactory;
		this.rightShiftFactory = rightShiftFactory;
	}

	@Override
	protected ProtocolProducer initializeGateProducer() {

		/*
		 * The result of the shift is ignored - we only need the bits that was
		 * thrown away.
		 */
		SInt tmp = basicNumericFactory.getSInt();

		ProtocolProducer rightShiftProtocol = rightShiftFactory.getRepeatedRightShiftProtocol(
				input, maxInputLength, tmp, output);
		return rightShiftProtocol;
	}

}
