package dk.alexandra.fresco.tools.ot.base;

import dk.alexandra.fresco.framework.util.StrictBitVector;

/**
 * Oblivious Transfer interface.
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
