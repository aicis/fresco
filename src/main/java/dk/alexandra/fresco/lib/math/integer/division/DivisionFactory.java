/*******************************************************************************
 * Copyright (c) 2015 FRESCO (http://github.com/aicis/fresco).
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

public interface DivisionFactory {
	
	/**
	 * @param x
	 *            input
	 * @param maxInputLength
	 *            An upper bound for log_2(x)
	 * @param divisor
	 *            divisor
	 * @param result
	 *            floor(x / d)
	 * 
	 * @return
	 */
	DivisionProtocol getDivisionProtocol(SInt x, int maxInputLength, OInt divisor, SInt result);

	/**
	 * @param x
	 *            input
	 * @param maxInputLength
	 *            An upper bound for log_2(x)
	 * @param divisor
	 *            divisor
	 * @param result
	 *            floor(x / d)
	 * @param remainder
	 *            The remainder: a nonnegative integer < d such that x = result * d + r
	 * 
	 * @return
	 */
	public DivisionProtocol getDivisionProtocol(SInt x, int maxInputLength, OInt divisor, SInt result, SInt remainder);

	/**
	 * This protocol calculates an approximation of <code>x / divisor</code>,
	 * which will be either correct or slightly smaller than the correct result.
	 * 
	 * @param x
	 *            Input. To avoid overflow we require that
	 *            <i>2<sup>2<sup>p</sup>m</sup>x</i> should be smaller than the
	 *            modulus used, where <i>m</i> is <code>maxDivisorLength</code>
	 *            and <i>p</i> is <code>precision</code>.
	 * @param divisor
	 *            The divisor
	 * @param maxDivisor
	 *            Length An upper bound for <i>log<sub>2</sub>(divisor)</i>.
	 * @param precision
	 *            A parameter determining the precision of the approximation.
	 * @param result
	 *            The result which is an approximation of x / divisor. It will
	 *            be \leq the correct result. More precisely it will be equal to
	 *            <i>floor( (x + 1 / divisor) * (1 -
	 *            ((2<sup>m</sup>-d)/2<sup>m</sup>)<sup>2<sup>p</sup></sup>) )
	 *            where <i>m</i> is <code>maxInputLength</code> and <i>p</i> is
	 *            <code>precision</code>.
	 * @return
	 */
	public DivisionProtocol getDivisionProtocol(SInt x, SInt divisor, int maxDivisorLength, int precision, SInt result);
	
}