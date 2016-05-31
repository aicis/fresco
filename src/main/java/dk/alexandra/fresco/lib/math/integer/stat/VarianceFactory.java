package dk.alexandra.fresco.lib.math.integer.stat;

import dk.alexandra.fresco.framework.value.SInt;

public interface VarianceFactory {

	/**
	 * 
	 * @param data
	 *            The data set to be analysed.
	 * @param maxInputLength
	 *            An upper bound for the bitlength of each entry
	 * @param mean
	 *            An approximation of the mean of the given data (use eg.
	 *            {@link #getMeanProtocol(SInt[], int, SInt)}).
	 * @param result
	 *            The floor of an approximation of the variance of the given
	 *            data calculated as sum((data[i] - mean)^2) / (data.length-1).
	 * @return
	 */
	public VarianceProtocol getVarianceProtocol(SInt[] data, int maxInputLength, SInt mean,
			SInt result);

	/**
	 * 
	 * @param data
	 *            The data set to be analysed.
	 * @param maxInputLength
	 *            An upper bound for the bitlength of each entry
	 * @param result
	 *            The floor of an approximation of the variance of the given
	 *            data calculated as sum((data[i] - mean)^2) / (data.length-1).
	 * @return
	 */
	public VarianceProtocol getVarianceProtocol(SInt[] data, int maxInputLength, SInt result);
	
}

