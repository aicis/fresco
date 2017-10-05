package dk.alexandra.fresco.framework.network;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Set;

/**
 * Supplies the higher level layer with the outputs that the protocol queued up
 * within the {@link SCENetwork}. Separated from that interface since the protocols
 * only need to know about the methods contained within that interface.
 * Typically, a class implementing that interface, also implements this.
 * 
 * @author Kasper Damgaard
 *
 */
public interface SCENetworkSupplier {

	public void setInput(Map<Integer, ByteBuffer> inputForThisRound);
	
	public Map<Integer, byte[]> getOutputFromThisRound();

	public Set<Integer> getExpectedInputForNextRound();

	/**
	 * Clears the internal maps to ensure that the returned values next round is
	 * correct.
	 */
	public void nextRound();
}
