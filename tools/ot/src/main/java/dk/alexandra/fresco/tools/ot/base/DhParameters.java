package dk.alexandra.fresco.tools.ot.base;

import java.math.BigInteger;

import javax.crypto.spec.DHParameterSpec;

/**
 * Class for generating Diffie-Hellman parameters using Java's internal functionality. The class can
 * be used to both generate the parameters securely using coin-tossing, locally using a seed or
 * simply to retrieve a pair of static parameters.
 */
public final class DhParameters {

  private DhParameters() {
    // Should not be instantiated
  }

  /**
   * 2048-bit MODP Group (see <a href="https://www.ietf.org/rfc/rfc3526.txt">group 14, RFC3526<a>)
   */
  private static final BigInteger DhGvalue = BigInteger.valueOf(2);
  private static final BigInteger DhPvalue = new BigInteger(
          "FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD1" +
          "29024E088A67CC74020BBEA63B139B22514A08798E3404DD" +
          "EF9519B3CD3A431B302B0A6DF25F14374FE1356D6D51C245" +
          "E485B576625E7EC6F44C42E9A637ED6B0BFF5CB6F406B7ED" +
          "EE386BFB5A899FA5AE9F24117C4B1FE649286651ECE45B3D" +
          "C2007CB8A163BF0598DA48361C55D39A69163FA8FD24CF5F" +
          "83655D23DCA3AD961C62F356208552BB9ED529077096966D" +
          "670C354E4ABC9804F1746C08CA18217C32905E462E36CE3B" +
          "E39E772C180E86039B2783A2EC07A28FB5C55DF06F4C52C9" +
          "DE2BCBF6955817183995497CEA956AE515D2261898FA0510" +
          "15728E5A8AACAA68FFFFFFFFFFFFFFFF", 16);



  /**
   * Returns a static set of 2048 bit Diffie-Hellman parameters.
   * <p>
   * These are computed using Java's internal parameter generator using a {@code SecureRandom}
   * randomness generator seeded with the byte 0x42.
   * </p>
   *
   * @return Static Diffie-Hellman parameters
   */
  public static DHParameterSpec getStaticDhParams() {
    return new DHParameterSpec(DhPvalue, DhGvalue);
  }

}
