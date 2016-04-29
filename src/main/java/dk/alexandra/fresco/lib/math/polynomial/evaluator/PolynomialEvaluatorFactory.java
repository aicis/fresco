package dk.alexandra.fresco.lib.math.polynomial.evaluator;

import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.math.polynomial.Polynomial;

public interface PolynomialEvaluatorFactory {

	/**
	 * Create a new polynomial evaluator protocol which evaluates the polynomial
	 * <code>p</code> in the point <code>x</code>.
	 * 
	 * @param x
	 * @param p
	 * @param result
	 * @return
	 */
	public PolynomialEvaluatorProtocol createPolynomialEvaluator(SInt x, Polynomial p, SInt result);

}
