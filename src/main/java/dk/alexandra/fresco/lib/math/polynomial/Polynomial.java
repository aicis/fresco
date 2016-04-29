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
	 * Get the <code>n</code>'th coefficient of this polynomial where n = 0
	 * corresponds to the constant term.
	 * 
	 * @param n
	 * @return
	 */
	public SInt getCoefficient(int n);

	/**
	 * Set the <code>n</code>'th coefficient of this polynomial where n = 0
	 * corresponds to the constant term.
	 * 
	 * @param n
	 * @param coefficient
	 */
	public void setCoefficient(int n, SInt coefficient);

}
