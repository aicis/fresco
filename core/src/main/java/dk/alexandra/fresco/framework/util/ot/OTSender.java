package dk.alexandra.fresco.framework.util.ot;

import java.util.List;

import dk.alexandra.fresco.framework.util.ot.datatypes.OTInput;

/**
 * Implementations of this interface represents an implementation of the
 * sending part of an 1-2 oblivious transfer protocol
 * (https://en.wikipedia.org/wiki/Oblivious_transfer).
 * 
 * @author Jonas Lindstrøm (jonas.lindstrom@alexandra.dk)
 *
 */
public interface OTSender {

	/**
	 * Perform oblivious transfer as the sender with the given {@link OTInput}'s.
	 * 
	 * @param inputs
	 */
	public void send(List<OTInput> inputs);

}
