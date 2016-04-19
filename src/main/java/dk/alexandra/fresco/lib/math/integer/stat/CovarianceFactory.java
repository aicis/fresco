package dk.alexandra.fresco.lib.math.integer.stat;

import dk.alexandra.fresco.framework.value.SInt;

public interface CovarianceFactory {



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
