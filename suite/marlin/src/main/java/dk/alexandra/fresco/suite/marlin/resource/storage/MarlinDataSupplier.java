package dk.alexandra.fresco.suite.marlin.resource.storage;

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
  MarlinTriple<T> getNextTripleShares();

  /**
   * Supplies the next inputmask for a given input player.
   *
   * @param towardPlayerId the id of the input player
   * @return the appropriate input mask
   */
  MarlinInputMask<T> getNextInputMask(int towardPlayerId);

  /**
   * Supplies the next bit (SInt representing value in {0, 1}).
   */
  MarlinSInt<T> getNextBitShare();

  /**
   * Returns the player's share of the mac key.
   */
  T getSecretSharedKey();

  /**
   * Returns the next random field element.
   */
  MarlinSInt<T> getNextRandomElementShare();

}
