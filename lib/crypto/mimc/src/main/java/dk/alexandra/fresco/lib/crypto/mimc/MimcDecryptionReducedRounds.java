package dk.alexandra.fresco.lib.crypto.mimc;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;

/**
* A version of MiMC with a reduced number of rounds suitable when using as PRF. As described in
* <a href="https://eprint.iacr.org/2016/542.pdf">MPC-Friendly Symmetric Key Primitives</a> of
* Grassi <i>et al.</i>.
*
* <p>
* As described above this implementation is suitable only when using MIMC as an PRF and an
* adversary has access to at most <i>n</i> ciphertexts where <i>&lceil;log<sub>3</sub>(n)&rceil;
* &lt; &lceil;log<sub>3</sub>(p) - 2log<sub>3</sub>(log<sub>3</sub>(p))&rceil;</i>, where <i>p</i>
* is the modulus.
* </p>
*/
public class MimcDecryptionReducedRounds implements Computation<SInt, ProtocolBuilderNumeric> {

  private final DRes<SInt> cipherText;
  private final DRes<SInt> encryptionKey;

  /**
   * Reduced round MiMC decryption.
   *
   * <p>
   * This implementation is suitable only when using MIMC as an PRF and an adversary has access to
   * at most <i>n</i> ciphertexts where <i>&lceil;log<sub>3</sub>(n)&rceil; &lt;
   * &lceil;log<sub>3</sub>(p) - 2log<sub>3</sub>(log<sub>3</sub>(p))&rceil;</i>
   * </p>
   *
   * @param cipherText the ciphertext to decrypt.
   * @param encryptionKey the symmetric key used to decrypt.
   */
  public MimcDecryptionReducedRounds(DRes<SInt> cipherText, DRes<SInt> encryptionKey) {
    this.cipherText = cipherText;
    this.encryptionKey = encryptionKey;
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder) {
    int reducedRounds = MimcEncryptionReducedRounds
        .computeReducedRounds(builder.getBasicNumericContext().getModulus());
    return (new MiMCDecryption(cipherText, encryptionKey, reducedRounds)).buildComputation(builder);
  }

}
