package dk.alexandra.fresco.tools.ot.base;

import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import java.util.List;

/**
 * Complete a batch of 1-out-of-2 random OTs, where the receiver gets to pick
 * its choice bits.
 */
public interface RotBatch {

  /**
   * Send {@code numMessages} of size {@code messageSize} each, in a batch.
   *
   * @param numMessages The number of messages in the batch
   * @param messageSize The size of the messages
   * @return The random messages construct for each of the OTs
   */
  List<Pair<StrictBitVector, StrictBitVector>> send(int numMessages,
      int messageSize);

  /**
   * Receive random messages based on the choice bits in {@code choiceBits}.
   *
   * @param choiceBits The bits indicating which messages to learn
   * @param messageSize The size of all the messages
   * @return The random messages chosen
   */
  List<StrictBitVector> receive(StrictBitVector choiceBits, int messageSize);

}
