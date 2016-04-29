package dk.alexandra.fresco.lib.math.polynomial;

import dk.alexandra.fresco.framework.value.SInt;

public interface PolynomialFactory {

	public Polynomial createPolynomial(int degree);
	
	public Polynomial createPolynomial(SInt[] coefficients);
	
}
