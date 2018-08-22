package dk.alexandra.fresco.suite.tinytables.util;

import dk.alexandra.fresco.framework.util.ExceptionConverter;
import java.security.MessageDigest;
import java.util.BitSet;

public class Util {

  private static final String HASH_ALGO = "SHA-256";

  private Util() {
    // Do not instantiate
  }

  /**
   * Outputs a hash of <i>j</i> and the given bits of size <i>l</i>.
   *
   * <p>
   * We assume that <i>l &lt; 256</i> since the underlying hash function is {@value #HASH_ALGO}.
   * </p>
   *
   * @param j
   * @param bits
   * @param l
   * @return
   */
  public static BitSet hash(int j, BitSet bits, int l) {
    MessageDigest digest = ExceptionConverter.safe(() -> MessageDigest.getInstance(HASH_ALGO),
        "Unable to create " + HASH_ALGO + " digest");
    for (int i = 0; i < Integer.BYTES; i++) {
      digest.update((byte) (j >>> i * Byte.SIZE));
    }
    byte[] binary = digest.digest(bits.toByteArray());
    return BitSet.valueOf(binary).get(0, l);
  }

  public static int otherPlayerId(int myId) {
    return myId == 1 ? 2 : 1;
  }
}
