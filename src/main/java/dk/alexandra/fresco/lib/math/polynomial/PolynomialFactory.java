package dk.alexandra.fresco.lib.math.polynomial;

import dk.alexandra.fresco.framework.value.SInt;

public interface PolynomialFactory {

	/**
	 * Create a new polynomial. All coefficients are intially <code>null</code>.
	 * 
	 * @param maxDegree
	 *            An upper bound for the degree of the polynomial.
	 * 
	 * @return
	 */
	public Polynomial createPolynomial(int maxDegree);

	/**
	 * Create a polynomial with the given coefficients.
	 * 
	 * @param coefficients
	 *            The coefficients of the polynomial,
	 *            <code>coefficients[n]</code> being the coefficient for the
	 *            term of degree <code>n</code>.
	 * @return
	 */
	public Polynomial createPolynomial(SInt[] coefficients);

}
