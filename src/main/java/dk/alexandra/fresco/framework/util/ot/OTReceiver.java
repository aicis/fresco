package dk.alexandra.fresco.framework.util.ot;

import java.util.List;

import dk.alexandra.fresco.framework.util.ot.datatypes.OTSigma;

public interface OTReceiver {
	
	/**
	 * Perform oblivious transfer as the receiver.
	 * 
	 * @param sigmas
	 * @param expectedLength
	 *            The expected bit length of each output.
	 * @return
	 */
	public List<boolean[]> receive(List<OTSigma> sigmas, int expectedLength);
	
}
