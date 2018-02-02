package dk.alexandra.fresco.suite.marlin.storage;

import dk.alexandra.fresco.suite.marlin.datatypes.BigUInt;
import dk.alexandra.fresco.suite.marlin.datatypes.MarlinInputMask;
import dk.alexandra.fresco.suite.marlin.datatypes.MarlinSInt;
import dk.alexandra.fresco.suite.marlin.datatypes.MarlinTriple;

public interface MarlinDataSupplier<T extends BigUInt<T>> {

  /**
   * Supplies the next triple.
   *
   * @return the next new triple
   */
  MarlinTriple getNextTriple();

  /**
   * Supplies the next inputmask for a given input player.
   *
   * @param towardPlayerId the id of the input player
   * @return the appropriate input mask
   */
  MarlinInputMask getNextInputMask(int towardPlayerId);

  /**
   * Supplies the next bit (SInt representing value in {0, 1}).
   */
  MarlinSInt<T> getNextBit();

  /**
   * Returns the Players share of the Shared Secret Key (alpha). This is never to be send to anyone
   * else!
   */
  T getSecretSharedKey();

  /**
   * Returns the next random field element.
   */
  MarlinSInt<T> getNextRandomFieldElement();

}
