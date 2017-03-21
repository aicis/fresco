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
package dk.alexandra.fresco.lib.helper.builder;

import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.math.integer.stat.StatisticsFactory;

public class StatisticsProtocolBuilder extends AbstractProtocolBuilder {

	private final StatisticsFactory statFactory;
	private final BasicNumericFactory bnf;

	protected StatisticsFactory getStatFactory() {
		return statFactory;
	}

	protected BasicNumericFactory getBnf() {
		return bnf;
	}

	public StatisticsProtocolBuilder(StatisticsFactory statFactory, BasicNumericFactory bnf) {
		this.statFactory = statFactory;
		this.bnf = bnf;
	}

	/**
	 * @param data1
	 *            The first data set.
	 * @param data2
	 *            The second data set. Must have same length as the first.
	 * @param maxInputLength
	 *            An upper bound for the bitLenght of each entry.
	 * @return The covariance of the two data sets.
	 */
	public SInt covariance(SInt[] data1, SInt[] data2, int maxInputLength) {
		SInt result = bnf.getSInt();
		ProtocolProducer pp = getStatFactory().getCovarianceProtocol(data1, data2, maxInputLength, result);
		append(pp);
		return result;
	}

	/**
	 * @param data1
	 *            The first data set.
	 * @param data2
	 *            The second data set. Must have same length as the first.
	 * @param mean1
	 *            An approximation of the arithmetic mean of the first data set.
	 * @param mean2
	 *            An approximation of the arithmetic mean of the second data
	 *            set.
	 * @param maxInputLength
	 *            An upper bound for the bitLenght of each entry.
	 * @return The covariance of the two data sets.
	 */
	public SInt covariance(SInt[] data1, SInt[] data2, SInt mean1, SInt mean2, int maxInputLength) {
		SInt result = bnf.getSInt();
		ProtocolProducer pp = getStatFactory().getCovarianceProtocol(data1, data2, maxInputLength, mean1, mean2,
				result);
		append(pp);
		return result;
	}

	/**
	 * @param data
	 *            The data set to be analysed. One set for each column.
	 * @param maxInputLength
	 *            An upper bound for the bitlength of each entry
	 * @return The covariance matrix of the given data sets. Note that only
	 *         upper triangle of matrix (i,j for j \geq i) has to be initialized
	 *         SInt's - the symmetric entry will be a copy of the one from the
	 *         upper triangle, M[j][i] := M[i][j].
	 */
	public SInt[][] covarianceMatrix(SInt[][] data, int maxInputLength) {
		NumericProtocolBuilder helper = new NumericProtocolBuilder(bnf);
		SInt[][] results = helper.getSIntMatrix(data.length, data[0].length);
		ProtocolProducer pp = getStatFactory().getCovarianceMatrixProtocol(data, maxInputLength, results);
		append(pp);
		return results;
	}

	/**
	 * @param data
	 *            The data set to be analysed. One set for each column.
	 * @param maxInputLength
	 *            An upper bound for the bitlength of each entry
	 * @param means
	 *            An approximation of the means of the data sets. This can be
	 *            calculated using {@link #mean(SInt[], int)}
	 * @return Only upper triangle of matrix (i,j for j \geq i) has to be
	 *            initialized SInt's - the symmetric entry will be a copy of the
	 *            one from the upper triangle, eg. M[j][i] := M[i][j].
	 */
	public SInt[][] covarianceMatrix(SInt[][] data, int maxInputLength, SInt[] means) {
		NumericProtocolBuilder helper = new NumericProtocolBuilder(bnf);
		SInt[][] results = helper.getSIntMatrix(data.length, data[0].length);
		ProtocolProducer pp = getStatFactory().getCovarianceMatrixProtocol(data, maxInputLength, means, results);
		append(pp);
		return results;
	}

	/**
	 * 
	 * @param data
	 *            The data set to be analysed.
	 * @param maxInputLength
	 *            An upper bound for the bitlength of each entry
	 * @return The floor of an approximation of the variance of the given data
	 *         calculated as sum((data[i] - mean)^2) / (data.length-1)
	 */
	public SInt variance(SInt[] data, int maxInputLength) {
		SInt result = bnf.getSInt();
		ProtocolProducer pp = getStatFactory().getVarianceProtocol(data, maxInputLength, result);
		append(pp);
		return result;
	}

	/**
	 * 
	 * @param data
	 *            The data set to be analysed.
	 * @param maxInputLength
	 *            An upper bound for the bitlength of each entry
	 * @param mean
	 *            An approximation of the mean of the given data (use eg.
	 *            {@link #mean(SInt [], int)}.
	 * @return The floor of an approximation of the variance of the given data
	 *         calculated as sum((data[i] - mean)^2) / (data.length-1).
	 */
	public SInt variance(SInt[] data, int maxInputLength, SInt mean) {
		SInt result = bnf.getSInt();
		ProtocolProducer pp = getStatFactory().getVarianceProtocol(data, maxInputLength, mean, result);
		append(pp);
		return result;
	}

	/**
	 * @param data
	 *            The data set to be analysed.
	 * @param maxInputLength
	 *            An upper bound for the bitlength of each entry.
	 * @return The floor of the sample mean of the given data, sum(data[i]) /
	 *         data.length.
	 */
	public SInt mean(SInt[] data, int maxInputLength) {
		SInt result = bnf.getSInt();
		ProtocolProducer pp = getStatFactory().getMeanProtocol(data, maxInputLength, result);
		append(pp);
		return result;
	}

	/**
	 * @param data
	 *            The data set to be analysed.
	 * @param maxInputLength
	 *            An upper bound for the bitlength of each entry.
	 * @param degreesOfFreedom
	 *            The degrees of freedom of the sample whose expected value we
	 *            are estimating.
	 * @return The floor of the sample mean of the given data with the given
	 *         sum(data[i]) / degreesOfFreedom
	 */
	public SInt mean(SInt[] data, int maxInputLength, int degreesOfFreedom) {
		SInt result = bnf.getSInt();
		ProtocolProducer pp = getStatFactory().getMeanProtocol(data, maxInputLength, degreesOfFreedom, result);
		append(pp);
		return result;
	}

	@Override
	public void addProtocolProducer(ProtocolProducer pp) {
		append(pp);
	}
}
