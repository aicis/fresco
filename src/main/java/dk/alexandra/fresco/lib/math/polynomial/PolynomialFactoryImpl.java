package dk.alexandra.fresco.lib.math.polynomial;

import dk.alexandra.fresco.framework.value.SInt;

public class PolynomialFactoryImpl implements PolynomialFactory {

	@Override
	public Polynomial createPolynomial(int maxDegree) {
		return new PolynomialImpl(maxDegree);
	}

	@Override
	public Polynomial createPolynomial(SInt[] coefficients) {
		return new PolynomialImpl(coefficients);
	}

}
