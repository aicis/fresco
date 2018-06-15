package dk.alexandra.fresco.suite.spdz2k.resource.storage;

import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.Spdz2kInputMask;
import dk.alexandra.fresco.suite.spdz2k.datatypes.Spdz2kSIntArithmetic;
import dk.alexandra.fresco.suite.spdz2k.datatypes.Spdz2kSIntBoolean;
import dk.alexandra.fresco.suite.spdz2k.datatypes.Spdz2kTriple;
import dk.alexandra.fresco.suite.spdz2k.datatypes.Spdz2kTruncationPair;

/**
 * Interface for a supplier of pre-processing material. <p>Material includes random elements shares,
 * random bit shares, and multiplication triple shares.</p>
 */
public interface Spdz2kDataSupplier<T extends CompUInt<?, ?, T>> {

  /**
   * Supplies the next full multiplication triple.
   *
   * @return the next new triple
   */
  Spdz2kTriple<T, Spdz2kSIntArithmetic<T>> getNextTripleSharesFull();

  /**
   * Supplies the next boolean multiplication triple.
   *
   * @return the next new triple
   */
  Spdz2kTriple<T, Spdz2kSIntBoolean<T>> getNextBitTripleShares();

  /**
   * Supplies the next inputmask for a given input player.
   *
   * @param towardPlayerId the id of the input player
   * @return the appropriate input mask
   */
  Spdz2kInputMask<T> getNextInputMask(int towardPlayerId);

  /**
   * Supplies the next bit (SInt representing value in {0, 1}).
   */
  Spdz2kSIntArithmetic<T> getNextBitShare();

  /**
   * Returns the player's share of the mac key.
   */
  T getSecretSharedKey();

  /**
   * Supplies the next random field element.
   */
  Spdz2kSIntArithmetic<T> getNextRandomElementShare();

  /**
   * Supplies the next truncation pair (r^{prime}, r) where r = r^{prime} / 2^{d}.
   *
   * @param d number of shifts
   */
  Spdz2kTruncationPair<T> getNextTruncationPair(int d);

}
