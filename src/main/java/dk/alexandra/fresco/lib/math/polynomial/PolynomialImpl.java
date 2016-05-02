package dk.alexandra.fresco.lib.math.polynomial;

import java.util.Arrays;

import dk.alexandra.fresco.framework.value.SInt;

public class PolynomialImpl implements Polynomial {

	private SInt[] coefficients;

	/**
	 * Create a new Polynomial with <code>maxDegree</code> coefficients, all
	 * which are initialized as <code>null</code>.
	 * 
	 * @param maxDegree
	 */
	public PolynomialImpl(int maxDegree) {
		coefficients = new SInt[maxDegree];
	}

	/**
	 * Create a new polynomial with the given coefficients.
	 * 
	 * @param coefficients
	 *            The coefficients of the polynomial,
	 *            <code>coefficients[n]</code> being the coefficient for the
	 *            term of degree <code>n</code>.
	 */
	public PolynomialImpl(SInt[] coefficients) {
		this.coefficients = coefficients;
	}

	@Override
	public void setCoefficient(int n, SInt a) {
		coefficients[n] = a;
	}

	@Override
	public SInt getCoefficient(int n) {
		return coefficients[n];
	}

	@Override
	public int getMaxDegree() {
		return coefficients.length;
	}

	@Override
	public void setMaxDegree(int maxDegree) {
		if (coefficients.length >= maxDegree) {
			this.coefficients = Arrays.copyOfRange(this.coefficients, 0, maxDegree);
		} else {
			SInt[] tmp = new SInt[maxDegree];
			System.arraycopy(this.coefficients, 0, tmp, 0, this.coefficients.length);
			this.coefficients = tmp;
		}
	}

}
