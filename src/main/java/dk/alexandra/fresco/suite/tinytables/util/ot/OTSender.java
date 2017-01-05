package dk.alexandra.fresco.suite.tinytables.util.ot;

import java.util.List;

import dk.alexandra.fresco.suite.tinytables.util.ot.datatypes.OTInput;

public interface OTSender {

	/**
	 * Perform oblivious transfer as the sender with the given inputs.
	 * 
	 * @param inputs
	 */
	public void send(List<OTInput> inputs);

}
