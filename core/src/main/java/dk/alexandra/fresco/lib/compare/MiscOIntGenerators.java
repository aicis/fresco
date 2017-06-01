/*******************************************************************************
 * Copyright (c) 2015, 2016 FRESCO (http://github.com/aicis/fresco).
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
package dk.alexandra.fresco.lib.compare;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.suite.spdz.utils.Util;


/**
 * Misc computation on OInts -- results are cached
 * @author ttoft
 *
 */
public class MiscOIntGenerators {

	private BasicNumericFactory factory;

	Map<Integer, OInt[]> coefficientsOfPolynomiums;
	OInt[] twoPowers;
	// should twoPowers be a List?
	
	
	public MiscOIntGenerators(BasicNumericFactory factory) {
		this.factory = factory;
		coefficientsOfPolynomiums = new HashMap<Integer, OInt[]>();

		twoPowers = new OInt[1];
		twoPowers[0] = factory.getOInt();
		twoPowers[0].setValue(BigInteger.ONE);		
	}
	
	
	/**
	 * Generate a degree l polynomium P such that P(1) = 1 and P(i) = 0 for i in {2,3,...,l+1}
	 * @param l degree of polynomium
	 * @param factory source of OInt's
	 * @return coefficients of P
	 */
	public OInt[] getPoly(int l) {
		// check that l is positive
		Integer lInt = new Integer(l);
		OInt[] result = coefficientsOfPolynomiums.get(lInt);
		if (result == null) {
			// Generate a new set of OInts and store them...
			result = new OInt[l+1];

			BigInteger[] coefficients = Util.constructPolynomial(l, 1);
			for (int i=0; i<=l ; i++) {
				result[i] = factory.getOInt();
				result[i].setValue(coefficients[coefficients.length - 1 - i]);
			}
			
			coefficientsOfPolynomiums.put(lInt, result);
		}
		return result;
	}
	
	/**
	 * Generate all two-powers 2^i for i<l
	 * @param l array length
	 * @return Array of length l with result[i] == 2^i
	 */
	public OInt[] getTwoPowers(int l) {
		if (l > twoPowers.length) {
			OInt[] newArray = new OInt[l];
			System.arraycopy(twoPowers, 0, newArray, 0, twoPowers.length);
			BigInteger currentValue = twoPowers[twoPowers.length-1].getValue();
			for (int i = twoPowers.length; i < newArray.length; i++) {
				newArray[i] = factory.getOInt();
				currentValue = currentValue.shiftLeft(1); // multiply previous value by two
				newArray[i].setValue(currentValue);
			}
			twoPowers = newArray;
		}
		// TODO: avoid copying.... also; since OInts are mutable, perhaps we should clone.
		OInt[] result = new OInt[l];
		System.arraycopy(twoPowers, 0, result, 0, l);
		return result;
	}
}
