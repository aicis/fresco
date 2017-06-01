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
package dk.alexandra.fresco.lib.math.integer.division;

import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;

/**
 * This factory creates protocols for performing division on integers. The
 * dividend is secret shared for all protocols, but there are protocols for both
 * secret shared and open divisor.
 * 
 * @author Jonas Lindstrøm (jonas.lindstrom@alexandra.dk)
 *
 */
public interface DivisionFactory {

	/**
	 * @param dividend
	 *            The dividend.
	 * @param maxDividendLength
	 *            An upper bound for <i>log<sub>2</sub>(dividend)</i>.
	 * @param divisor
	 *            The divisor.
	 * @param quotient
	 *            The quotient, <i>floor(dividend / divisor)</i>.
	 * 
	 * @return
	 */
	public DivisionProtocol getDivisionProtocol(SInt dividend, OInt divisor,
			SInt quotient);

	/**
	 * @param dividend
	 *            The dividend.
	 * @param maxDividendLength
	 *            An upper bound for <i>log<sub>2</sub>(dividend)</i>.
	 * @param divisor
	 *            The divisor.
	 * @param quotient
	 *            The quotient, <i>floor(dividend / divisor)</i>.
	 * @param remainder
	 *            The remainder: a nonnegative integer strictly smaller than the
	 *            divisor such that <i>dividend = quotient * divisor +
	 *            remainder</i>.
	 * 
	 * @return
	 */
	public DivisionProtocol getDivisionProtocol(SInt dividend, OInt divisor,
			SInt quotient, SInt remainder);

	/**
	 * This protocol calculates an approximation of
	 * <code>floor(dividend / divisor)</code>, which will be either correct or
	 * slightly smaller than the correct result.
	 * 
	 * @param dividend
	 *            The dividend.
	 * @param divisor
	 *            The divisor.
	 * @param quotient
	 *            An approximation of <i>dividend / divisor</i>.
	 * @return
	 */
	public DivisionProtocol getDivisionProtocol(SInt dividend, SInt divisor,
												SInt quotient);
	
	/**
	 * This protocol calculates an approximation of
	 * <code>floor(dividend / divisor)</code>, which will be either correct or
	 * slightly smaller than the correct result.
	 * 
	 * @param dividend
	 *            The dividend.
	 * @param divisor
	 *            The divisor.
	 * @param quotient
	 *            An approximation of <i>dividend / divisor</i>.
	 * @param precision
	 *            If this parameter is supplied, the protocol gives a guaranteed
	 *            lower bound for the number of correct bits of the approximation.
	 * @return
	 */
	public DivisionProtocol getDivisionProtocol(SInt dividend, SInt divisor,
												SInt quotient, OInt precision);


}