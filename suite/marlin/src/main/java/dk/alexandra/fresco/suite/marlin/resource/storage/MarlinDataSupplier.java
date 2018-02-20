package dk.alexandra.fresco.suite.marlin.resource.storage;

import dk.alexandra.fresco.suite.marlin.datatypes.CompUInt;
import dk.alexandra.fresco.suite.marlin.datatypes.MarlinInputMask;
import dk.alexandra.fresco.suite.marlin.datatypes.MarlinSInt;
import dk.alexandra.fresco.suite.marlin.datatypes.MarlinTriple;
import dk.alexandra.fresco.suite.marlin.datatypes.UInt;
import java.util.ArrayList;
import java.util.List;

public interface MarlinDataSupplier<H extends UInt<H>, L extends UInt<L>, T extends CompUInt<H, L, T>> {

  /**
   * Supplies the next triple.
   *
   * @return the next new triple
   */
  MarlinTriple<H, L, T> getNextTripleShares();

  /**
   * Supplies the next inputmask for a given input player.
   *
   * @param towardPlayerId the id of the input player
   * @return the appropriate input mask
   */
  MarlinInputMask<H, L, T> getNextInputMask(int towardPlayerId);

  /**
   * Supplies the next bit (SInt representing value in {0, 1}).
   */
  MarlinSInt<H, L, T> getNextBitShare();

  /**
   * Returns the player's share of the mac key.
   */
  T getSecretSharedKey();

  /**
   * Returns the next random field element.
   */
  MarlinSInt<H, L, T> getNextRandomElementShare();

  default List<MarlinSInt<H, L, T>> getNextRandomElementShares(int numShares) {
    List<MarlinSInt<H, L, T>> shares = new ArrayList<>(numShares);
    for (int i = 0; i < numShares; i++) {
      shares.add(getNextRandomElementShare());
    }
    return shares;
  }

}
