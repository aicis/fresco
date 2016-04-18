package dk.alexandra.fresco.lib.math.integer.stat;

import dk.alexandra.fresco.framework.value.SInt;

public interface ArithmeticMeanFactory {

	/**
	 * 
	 * @param data
	 *            The data set to be analysed.
	 * @param maxInputLength
	 *            An upper bound for the bitlength of each entry.
	 * @param result
	 *            The floor of the arithetic mean of the given data,
	 *            sum(data[i]) / data.length.
	 * @return
	 */
	public ArithmeticMeanProtocol getArithmeticMeanProtocol(SInt[] data, int maxInputLength,
			SInt result);
	
}
