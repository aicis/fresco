package dk.alexandra.fresco.lib.math.polynomial.evaluator;

import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.math.polynomial.Polynomial;

public class PolynomialEvaluatorFactoryImpl implements PolynomialEvaluatorFactory {

	private final BasicNumericFactory basicNumericFactory;

	public PolynomialEvaluatorFactoryImpl(BasicNumericFactory basicNumericFactory) {
		this.basicNumericFactory = basicNumericFactory;
	}
	
	@Override
	public PolynomialEvaluatorProtocol createPolynomialEvaluator(SInt x, Polynomial p, SInt result) {
		return new PolynomialEvaluatorProtocolImpl(x, p, result, basicNumericFactory);
	}

}
