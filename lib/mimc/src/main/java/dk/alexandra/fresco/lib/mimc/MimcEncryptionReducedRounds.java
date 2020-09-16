package dk.alexandra.fresco.lib.mimc;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;

/**
 * A version of MiMC with a reduced number of rounds suitable when using as PRF. As described in <a
 * href="https://eprint.iacr.org/2016/542.pdf">MPC-Friendly Symmetric Key Primitives</a> of Grassi
 * <i>et al.</i>.
 *
 * <p>As described above this implementation is suitable only when using MIMC as an PRF and an
 * adversary has access to at most <i>n</i> ciphertexts where <i>&lceil;log<sub>3</sub>(n)&rceil;
 * &lt; &lceil;log<sub>3</sub>(p) - 2log<sub>3</sub>(log<sub>3</sub>(p))&rceil;</i>, where <i>p</i>
 * is the modulus.
 */
public class MimcEncryptionReducedRounds implements Computation<SInt, ProtocolBuilderNumeric> {

  private final DRes<SInt> plainText;
  private final DRes<SInt> encryptionKey;
  private static final double LOG_THREE = Math.log(3);

  /**
   * Reduced round MiMC encryption.
   *
   * <p>This implementation is suitable only when using MIMC as an PRF and an adversary has access
   * to at most <i>n</i> ciphertexts where <i>&lceil;log<sub>3</sub>(n)&rceil; &lt;
   * &lceil;log<sub>3</sub>(p) - 2log<sub>3</sub>(log<sub>3</sub>(p))&rceil;</i>
   *
   * @param plainText the plain text to encrypt.
   * @param encryptionKey the symmetric key used to encrypt.
   */
  public MimcEncryptionReducedRounds(DRes<SInt> plainText, DRes<SInt> encryptionKey) {
    this.plainText = plainText;
    this.encryptionKey = encryptionKey;
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder) {
    int reducedRounds = computeReducedRounds(builder.getBasicNumericContext().getModulus());
    return (new MiMCEncryption(plainText, encryptionKey, reducedRounds)).buildComputation(builder);
  }

  /**
   * Computes the number of rounds required for the given modulus.
   *
   * @param modulus the modulus
   * @return the number of required rounds
   */
  static int computeReducedRounds(BigInteger modulus) {
    double logThreeMod = Math.log(modulus.doubleValue()) / LOG_THREE;
    return (int) Math.ceil(logThreeMod - 2 * (Math.log(logThreeMod) / LOG_THREE));
  }
}
