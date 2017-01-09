/*******************************************************************************
 * Copyright (c) 2016 FRESCO (http://github.com/aicis/fresco).
 *
 * This file is part of the FRESCO project.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * FRESCO uses SCAPI - http://crypto.biu.ac.il/SCAPI, Crypto++, Miracl, NTL,
 * and Bouncy Castle. Please see these projects for any further licensing issues.
 *******************************************************************************/
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
