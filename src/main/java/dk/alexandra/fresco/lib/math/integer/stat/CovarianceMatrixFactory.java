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
