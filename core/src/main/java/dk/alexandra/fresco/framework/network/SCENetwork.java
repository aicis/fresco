package dk.alexandra.fresco.framework.network;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Network towards the protocols. This does not expose the real network, and
 * sending has no effect on the TCP layer. A higher level should handle the
 * input/output (typically the evaluator)
 *
 */
public interface SCENetwork {

	/**
	 * Retrieves input from the given id
	 * 
	 * @param id
	 *            The id of the player you want input from. Id's start from 1.
	 * @return A byte buffer which the protocol suite can use to fetch the
	 *         wanted bytes from, or use the different converters to extract
	 *         certain data classes.
	 */
	ByteBuffer receive(int id);

	/**
	 * Retrieves input from all players (including yourself)
	 * 
	 * @return A list of byte buffers where the data from party 1 resides at
	 *         index 0 and so forth.
	 */
	List<ByteBuffer> receiveFromAll();

	/**
	 * Queues up a value to be send towards the given id. Values are not send by
	 * TCP by calling this method, but queued up for the higher layer to send
	 * later.
	 * 
	 * @param id
	 *            The id whom you want to send to. Id's start from 1.
	 * @param data
	 *            The value to send
	 */
	void send(int id, byte[] data);

	/**
	 * Queues up a value to be send to all parties (yourself included). Values
	 * are not send by TCP by calling this method, but queued up for the higher
	 * layer to send later.
	 * 
	 * @param data
	 *            The value to send to all parties
	 */
	void sendToAll(byte[] data);

	/**
	 * Let's the network strategy know that you expect input from the given id
	 * in the next round.
	 * 
	 * @param id
	 *            The id to expect input from in the next round. Id's start from
	 *            1.
	 */
	void expectInputFromPlayer(int id);

	/**
	 * Let's the network strategy know that you expect to receive input from
	 * everyone next round.
	 */
	void expectInputFromAll();
		
	//Methods used by the internal FRESCO evaluators. 
	//Should not be called by protocols
	
	/**
	 * Sets the inputs for the current round. Called when received. 
	 * @param inputForThisRound The inputs given this round.
	 */
	public void setInput(Map<Integer, ByteBuffer> inputForThisRound);
	
	/**
	 * Returns a map from target party ID to the output which should be send to the target.  
	 * @return A mapping determining output to parties this round. 
	 */
	public Map<Integer, byte[]> getOutputFromThisRound();

	/**
	 * Gets a set indicating whom we expect to receive data from next round. 
	 * @return A set of party IDs. 
	 */
	public Set<Integer> getExpectedInputForNextRound();

	/**
	 * Clears the internal maps to ensure that the returned values next round is
	 * correct.
	 */
	public void nextRound();

}
