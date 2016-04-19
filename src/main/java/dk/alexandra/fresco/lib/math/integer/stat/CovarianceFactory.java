package dk.alexandra.fresco.lib.math.integer.stat;

import dk.alexandra.fresco.framework.value.SInt;

public interface CovarianceFactory {

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
	public CovarianceProtocol getCovarianceProtocol(SInt[][] data, int maxInputLength,
			SInt[][] result);

	/**
	 * 
	 * @param data1
	 *            The first data set.
	 * @param data2
	 *            The second data set. Must have same length as the first.
	 * @param maxInputLength
	 *            An upper bound for the bitLenght of each entry.
	 * @param result
	 *            The covariance of the two data sets.
	 * @return
	 */
	public CovarianceProtocol getCovarianceProtocol(SInt[] data1, SInt[] data2, int maxInputLength,
			SInt result);

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
	public CovarianceProtocol getCovarianceProtocol(SInt[][] data, int maxInputLength, SInt[] mean,
			SInt[][] result);

	/**
	 * 
	 * @param data1
	 *            The first data set.
	 * @param data2
	 *            The second data set. Must have same length as the first.
	 * @param maxInputLength
	 *            An upper bound for the bitLenght of each entry.
	 * @param mean1
	 *            An approximation of the arithmetic mean of the first data set.
	 * @param mean2
	 *            An approximation of the arithmetic mean of the second data
	 *            set.
	 * @param result
	 *            The covariance of the two data sets.
	 * @return
	 */
	public CovarianceProtocol getCovarianceProtocol(SInt[] data1, SInt[] data2, int maxInputLength,
			SInt mean1, SInt mean2, SInt result);

}
