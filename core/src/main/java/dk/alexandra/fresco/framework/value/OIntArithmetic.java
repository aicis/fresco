package dk.alexandra.fresco.framework.value;

import dk.alexandra.fresco.framework.DRes;
import java.util.List;

/**
 * Helper class for implementing various arithmetic operations on open values.
 */
public interface OIntArithmetic {

  /**
   * Turns input value into bits in big-endian order. <p>If the actual bit length of the value is
   * smaller than numBits, the result is padded with 0s. If the bit length is larger only the first
   * numBits bits are used.</p>
   */
  List<DRes<OInt>> toBits(OInt openValue, int numBits);

  /**
   * Returns a list of powers of two in ascending order, up to maxPower ([2^0, 2^1, ...,
   * 2^maxPower]).
   */
  List<DRes<OInt>> getPowersOfTwo(int maxPower);

}
