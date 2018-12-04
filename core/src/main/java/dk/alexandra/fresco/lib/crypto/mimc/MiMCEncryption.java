package dk.alexandra.fresco.lib.crypto.mimc;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * MiMC encryption is a symmetric cipher which can be used to great effect when utilizing it within
 * MPC. Implementation is done using <a href="https://eprint.iacr.org/2016/492.pdf">this paper</a>
 * as base line. Encryption is both simple and fast, but decryption is a heavy operation.
 *
 */
public class MiMCEncryption implements Computation<SInt, ProtocolBuilderNumeric> {

  /**
   * Used as "required round" parameter when the default number of rounds should be used.
   */
  static final int USE_DEFAULT_ROUNDS = -1;
  // TODO: require that our modulus - 1 and 3 are co-prime
  private final DRes<SInt> encryptionKey;
  private final DRes<SInt> plainText;
  private final int requestedRounds;
  private static Map<BigInteger, Integer> rounds = new HashMap<>();

  /**
   * Implementation of the MiMC decryption protocol.
   *
   * @param plainText The secret-shared plain text to encrypt.
   * @param encryptionKey The symmetric (secret-shared) key we will use to encrypt.
   * @param requiredRounds The number of rounds to use.
   */
  public MiMCEncryption(DRes<SInt> plainText, DRes<SInt> encryptionKey, int requiredRounds) {
    this.encryptionKey = encryptionKey;
    this.plainText = plainText;
    this.requestedRounds = requiredRounds;
  }

  /**
   * Implementation of the MiMC decryption protocol. Using default amount of rounds, log_3(modulus)
   * rounded up.
   *
   * @param plainText The secret-shared plain text to encrypt.
   * @param encryptionKey The symmetric (secret-shared) key we will use to encrypt.
   */
  public MiMCEncryption(DRes<SInt> plainText, DRes<SInt> encryptionKey) {
    this(plainText, encryptionKey, USE_DEFAULT_ROUNDS);
  }


  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder) {
    BigInteger modulus = builder.getBasicNumericContext().getModulus();
    final int requiredRounds = getRequiredRounds(modulus, requestedRounds);
    BigInteger three = BigInteger.valueOf(3);
    /*
     * In the first round we compute c = (p + K)^{3} where p is the plaintext
     */
    return builder.seq(seq -> {
      DRes<SInt> add = seq.numeric().add(plainText, encryptionKey);
      return new IterationState(1, seq.advancedNumeric().exp(add, three));
    }).whileLoop((state) -> state.round < requiredRounds, (seq, state) -> {
      /*
       * We're in an intermediate round where we compute c_{i} = (c_{i - 1} + K + r_{i})^{3} where K
       * is the symmetric key i is the reverse of the current round count r_{i} is the round
       * constant c_{i - 1} is the cipher text we have computed in the previous round
       */
      BigInteger roundConstantInteger = MimcConstants.getConstant(state.round, modulus);
      Numeric numeric = seq.numeric();
      DRes<SInt> masked =
          numeric.add(roundConstantInteger, numeric.add(state.value, encryptionKey));
      DRes<SInt> updatedValue = seq.advancedNumeric().exp(masked, three);
      return new IterationState(state.round + 1, updatedValue);
    }).seq((seq, state) ->
    /*
     * We're in the last round so we just mask the current cipher text with the encryption key
     */
    seq.numeric().add(state.value, encryptionKey));
  }

  static int getRequiredRounds(BigInteger modulus, int requestedRounds) {
    final int requiredRounds;
    if (requestedRounds == USE_DEFAULT_ROUNDS) {
      if (!rounds.containsKey(modulus)) {
        rounds.put(modulus, (int) Math.ceil(Math.log(modulus.doubleValue()) / Math.log(3)));
      }
      requiredRounds = rounds.get(modulus);
    } else {
      requiredRounds = requestedRounds;
    }
    return requiredRounds;
  }

  private static final class IterationState implements DRes<IterationState> {

    private final int round;
    private final DRes<SInt> value;

    private IterationState(int round, DRes<SInt> value) {
      this.round = round;
      this.value = value;
    }

    @Override
    public IterationState out() {
      return this;
    }
  }
}
