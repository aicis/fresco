package dk.alexandra.fresco.lib.math.integer.stat;

import dk.alexandra.fresco.framework.value.SInt;

public interface MeanFactory {

	/**
	 * 
	 * @param data
	 *            The data set to be analysed.
	 * @param maxInputLength
	 *            An upper bound for the bitlength of each entry.
	 * @param result
	 *            The floor of the sample mean of the given data,
	 *            sum(data[i]) / data.length.
	 * @return
	 */
	public MeanProtocol getMeanProtocol(SInt[] data, int maxInputLength,
			SInt result);

	/**
	 * 
	 * @param data
	 *            The data set to be analysed.
	 * @param maxInputLength
	 *            An upper bound for the bitlength of each entry.
	 * @param degreesOfFreedom
	 *            The degrees of freedom of the sample whose expected value we
	 *            are estimating.
	 * @param result
	 *            The floor of the sample mean of the given data with the given
	 *            sum(data[i]) / degreesOfFreedom.
	 * @return
	 */
	public MeanProtocol getMeanProtocol(SInt[] data, int maxInputLength,
			int degreesOfFreedom, SInt result);

}
