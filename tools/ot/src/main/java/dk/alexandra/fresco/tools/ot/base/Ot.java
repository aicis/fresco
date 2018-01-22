package dk.alexandra.fresco.tools.ot.base;

import dk.alexandra.fresco.framework.util.StrictBitVector;

/**
 * Oblivious Transfer interface for 1-out-of-2 oblivious transfer. That is, the sending party inputs
 * two messages and the receiving party a single bit. If the bit is 0 the receiving party learns the
 * first of the sending party's messages. If it is 1 it instead learns the second of the sending
 * party's messages. The sending party does not learn anything besides that the transfer was carried
 * out.
 */
public interface Ot {

  /**
   * Send two possible messages for the recipient to choose from.
   *
   * @param messageZero
   *          Message zero to send
   * @param messageOne
   *          Message one to send
   */
  void send(StrictBitVector messageZero, StrictBitVector messageOne);

  /**
   * Receive one-out-of-two messages.
   *
   * @param choiceBit
   *          Bit indicating which message to receive. False means message zero
   *          and true means message one.
   * @return The message indicated by the choice bit
   */
  StrictBitVector receive(boolean choiceBit);
}
