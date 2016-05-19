package dk.alexandra.fresco.lib.conversion;

import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.math.integer.binary.RightShiftFactory;

public class IntegerToBitsFactoryImpl implements IntegerToBitsFactory {

	private final BasicNumericFactory basicNumericFactory;
	private final RightShiftFactory rightShiftFactory;

	public IntegerToBitsFactoryImpl(BasicNumericFactory basicNumericFactory,
			RightShiftFactory rightShiftFactory) {
		this.basicNumericFactory = basicNumericFactory;
		this.rightShiftFactory = rightShiftFactory;
	}

	@Override
	public IntegerToBitsProtocol getIntegerToBitsCircuit(SInt input, int maxInputLength,
			SInt[] output) {
		return new IntegerToBitsByShiftProtocolImpl(input, maxInputLength, output, basicNumericFactory,
				rightShiftFactory);
	}

}
