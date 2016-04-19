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
package dk.alexandra.fresco.lib.math.integer.stat;

import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.math.integer.division.DivisionFactory;

public class StatisticsFactoryImpl implements StatisticsFactory {

	private BasicNumericFactory basicNumericFactory;
	private DivisionFactory divisionFactory;

	public StatisticsFactoryImpl(BasicNumericFactory basicNumericFactory,
			DivisionFactory euclideanDivisionFactory) {
		this.basicNumericFactory = basicNumericFactory;
		this.divisionFactory = euclideanDivisionFactory;
	}

	@Override
	public MeanProtocol getMeanProtocol(SInt[] data, int maxInputLength,
			SInt result) {
		return new MeanProtocolImpl(data, maxInputLength, result, basicNumericFactory,
				divisionFactory);
	}

	@Override
	public MeanProtocol getMeanProtocol(SInt[] data, int maxInputLength, int degreesOfFreedom,
			SInt result) {
		return new MeanProtocolImpl(data, maxInputLength, degreesOfFreedom, result, basicNumericFactory,
				divisionFactory);
	}

	
	@Override
	public VarianceProtocol getVarianceProtocol(SInt[] data, int maxInputLength, SInt mean,
			SInt result) {
		return new VarianceProtocolImpl(data, maxInputLength, mean, result, basicNumericFactory, this);
	}

	@Override
	public VarianceProtocol getVarianceProtocol(SInt[] data, int maxInputLength, SInt result) {
		return new VarianceProtocolImpl(data, maxInputLength, result, basicNumericFactory, this);
	}

	@Override
	public CovarianceMatrixProtocol getCovarianceMatrixProtocol(SInt[][] data, int maxInputLength, SInt[] mean,
			SInt[][] result) {
		return new CovarianceMatrixProtocolImpl(data, maxInputLength, mean, result, this, this);
	}

	@Override
	public CovarianceMatrixProtocol getCovarianceMatrixProtocol(SInt[][] data, int maxInputLength,
			SInt[][] result) {
		return new CovarianceMatrixProtocolImpl(data, maxInputLength, result, this, this);
	}

	@Override
	public CovarianceProtocol getCovarianceProtocol(SInt[] data1, SInt[] data2, int maxInputLength,
			SInt result) {
		return new CovarianceProtocolImpl(data1, data2, maxInputLength, result,
				basicNumericFactory, this);
	}

	@Override
	public CovarianceProtocol getCovarianceProtocol(SInt[] data1, SInt[] data2, int maxInputLength,
			SInt mean1, SInt mean2, SInt result) {
		return new CovarianceProtocolImpl(data1, data2, maxInputLength, mean1, mean2, result,
				basicNumericFactory, this);
	}

}
