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
