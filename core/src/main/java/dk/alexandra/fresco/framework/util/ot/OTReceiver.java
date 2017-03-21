package dk.alexandra.fresco.framework.util.ot;

import java.util.BitSet;
import java.util.List;

import dk.alexandra.fresco.framework.util.ot.datatypes.OTSigma;

/**
 * Implementations of this interface represents an implementation of the
 * receiving part of an 1-2 oblivious transfer protocol
 * (https://en.wikipedia.org/wiki/Oblivious_transfer).
 * 
 * @author Jonas Lindstrøm (jonas.lindstrom@alexandra.dk)
 *
 */
public interface OTReceiver {
	
	/**
	 * Perform oblivious transfer as the receiver.
	 * 
	 * @param sigmas
	 *            The selection bits.
	 * 
	 * @param expectedLength
	 *            The expected bit length of each output. All outputs are
	 *            expected to have the same length.
	 * @return
	 */
	public List<BitSet> receive(List<OTSigma> sigmas, int expectedLength);
	
}
