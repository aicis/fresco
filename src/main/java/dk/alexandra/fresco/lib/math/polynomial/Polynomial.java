package dk.alexandra.fresco.lib.math.polynomial;

import dk.alexandra.fresco.framework.value.SInt;

public interface Polynomial {

	/**
	 * Get an upper bound for the degree of this polynomial.
	 * 
	 * @return
	 */
	public int getMaxDegree();

	/**
	 * Set a new upper bound for the degree of this polynomial.
	 * 
	 * @param maxDegree
	 */
	public void setMaxDegree(int maxDegree);

	/**
	 * Get the coefficient of the term of degree <code>n</code> of this
	 * polynomial.
	 * 
	 * @param n
	 * @return
	 */
	public SInt getCoefficient(int n);

	/**
	 * Set the coefficient for the term of degree <code>n</code> of this
	 * polynomial.
	 * 
	 * @param n
	 * @param coefficient
	 */
	public void setCoefficient(int n, SInt coefficient);

}
