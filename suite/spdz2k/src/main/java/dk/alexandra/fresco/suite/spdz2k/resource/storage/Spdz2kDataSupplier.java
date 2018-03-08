package dk.alexandra.fresco.suite.spdz2k.resource.storage;

import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.Spdz2kInputMask;
import dk.alexandra.fresco.suite.spdz2k.datatypes.Spdz2kSInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.Spdz2kTriple;
import java.util.ArrayList;
import java.util.List;

/**
 * Interface for a supplier of pre-processing material. <p>Material includes random elements shares,
 * random bit shares, and multiplication triple shares.</p>
 */
public interface Spdz2kDataSupplier<PlainT extends CompUInt<?, ?, PlainT>> {

  /**
   * Supplies the next triple.
   *
   * @return the next new triple
   */
  Spdz2kTriple<PlainT> getNextTripleShares();

  /**
   * Supplies the next inputmask for a given input player.
   *
   * @param towardPlayerId the id of the input player
   * @return the appropriate input mask
   */
  Spdz2kInputMask<PlainT> getNextInputMask(int towardPlayerId);

  /**
   * Supplies the next bit (SInt representing value in {0, 1}).
   */
  Spdz2kSInt<PlainT> getNextBitShare();

  /**
   * Returns the player's share of the mac key.
   */
  PlainT getSecretSharedKey();

  /**
   * Returns the next random field element.
   */
  Spdz2kSInt<PlainT> getNextRandomElementShare();

  /**
   * Generate desired amount of triples.
   */
  default List<Spdz2kTriple<PlainT>> getNextTripleShares(int numTriples) {
    List<Spdz2kTriple<PlainT>> triples = new ArrayList<>(numTriples);
    for (int i = 0; i < numTriples; i++) {
      triples.add(getNextTripleShares());
    }
    return triples;
  }

}
