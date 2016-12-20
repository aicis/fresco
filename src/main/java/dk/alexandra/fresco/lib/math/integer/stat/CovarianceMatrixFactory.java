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
package dk.alexandra.fresco.lib.math.integer.stat;

import dk.alexandra.fresco.framework.value.SInt;

public interface CovarianceMatrixFactory {

	/**
	 * 
	 * @param data
	 *            The data set to be analysed. One set for each column.
	 * @param maxInputLength
	 *            An upper bound for the bitlength of each entry
	 * @param result
	 *            The covariance matrix of the given data sets. Note that only
	 *            upper triangle of matrix (i,j for j \geq i) has to be
	 *            initialized SInt's - the symmetric entry will be a copy of the
	 *            one from the upper triangle, M[j][i] := M[i][j].
	 * @return
	 */
	public CovarianceMatrixProtocol getCovarianceMatrixProtocol(SInt[][] data, int maxInputLength,
			SInt[][] result);

	/**
	 * 
	 * @param data
	 *            The data set to be analysed. One set for each column.
	 * @param maxInputLength
	 *            An upper bound for the bitlength of each entry
	 * @param mean
	 *            An approximation of the means of the data sets. This can be
	 *            calculated using
	 *            {@link #getMeanProtocol(SInt[], int, SInt)}
	 * @param result
	 *            Only upper triangle of matrix (i,j for j \geq i) has to be
	 *            initialized SInt's - the symmetric entry will be a copy of the
	 *            one from the upper triangle, eg. M[j][i] := M[i][j].
	 * @return
	 */
	public CovarianceMatrixProtocol getCovarianceMatrixProtocol(SInt[][] data, int maxInputLength, SInt[] mean,
			SInt[][] result);

}
