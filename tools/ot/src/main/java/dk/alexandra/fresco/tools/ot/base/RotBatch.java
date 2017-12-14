package dk.alexandra.fresco.tools.ot.base;

import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import java.util.List;

/**
 * Complete a batch of 1-out-of-2 random OTs, where the receiver gets to pick
 * its choice bits.
 * 
 * @author jot2re
 *
 * @param <T>
 *          The type of random elements to obliviously transfer
 */
public interface RotBatch<T> {

  /**
   * Send "numMessages" of size "messageSize" each, in a batch.
   * 
   * @param numMessages
   *          The number of messages in the batch
   * @param messageSize
   *          The size of the messages
   * @return The random messages construct for each of the OTs
   */
  List<Pair<T, T>> send(int numMessages, int messageSize);

  /**
   * Receive random messages based on the choice bits in "choiceBits".
   * 
   * @param choiceBits
   *          The bits indicating which messages to learn
   * @param messageSize
   *          The size of all the messages
   * @return The random messages chosen
   */
  List<T> receive(StrictBitVector choiceBits, int messageSize);
  
}
