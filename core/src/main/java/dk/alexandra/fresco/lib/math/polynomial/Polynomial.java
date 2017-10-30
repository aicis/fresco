package dk.alexandra.fresco.lib.math.polynomial;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.value.SInt;

public interface Polynomial {

	/**
	 * Get an upper bound for the degree of this polynomial.
	 * 
	 * @return
	 */
	public int getMaxDegree();

	/**
	 * Get the coefficient of the term of degree <code>n</code> of this
	 * polynomial.
	 * 
	 * @param n
	 * @return
	 */
	public DRes<SInt> getCoefficient(int n);


}
