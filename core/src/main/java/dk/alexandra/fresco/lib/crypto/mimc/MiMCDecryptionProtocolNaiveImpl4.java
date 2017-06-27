/*
 * Copyright (c) 2016 FRESCO (http://github.com/aicis/fresco).
 *
 * This file is part of the FRESCO project.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * FRESCO uses SCAPI - http://crypto.biu.ac.il/SCAPI, Crypto++, Miracl, NTL,
 * and Bouncy Castle. Please see these projects for any further licensing issues.
 */
package dk.alexandra.fresco.lib.crypto.mimc;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.AdvancedNumericBuilder;
import dk.alexandra.fresco.framework.builder.ComputationBuilder;
import dk.alexandra.fresco.framework.builder.NumericBuilder;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder.SequentialProtocolBuilder;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.OIntFactory;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import java.math.BigInteger;

public class MiMCDecryptionProtocolNaiveImpl4 implements ComputationBuilder<SInt> {

  // TODO: require that our modulus - 1 and 3 are co-prime

  private final Computation<SInt> encryptionKey;
  private final Computation<SInt> cipherText;
  private final Integer requestedRounds;

  /**
   * Implementation of the MiMC decryption protocol.
   *
   * @param cipherText The secret-shared cipher text to decrypt.
   * @param encryptionKey The symmetric (secret-shared) key we will use to decrypt.
   * @param requiredRounds The number of rounds to use.
   */
  public MiMCDecryptionProtocolNaiveImpl4(
      Computation<SInt> cipherText, Computation<SInt> encryptionKey, Integer requiredRounds) {
    this.cipherText = cipherText;
    this.encryptionKey = encryptionKey;
    this.requestedRounds = requiredRounds;
  }

  /**
   * Implementation of the MiMC decryption protocol.
   * Using default amount of rounds, log_3(modulus) rounded up.
   *
   * @param cipherText The secret-shared cipher text to decrypt.
   * @param encryptionKey The symmetric (secret-shared) key we will use to decrypt.
   */
  public MiMCDecryptionProtocolNaiveImpl4(
      Computation<SInt> cipherText, Computation<SInt> encryptionKey) {
    this(cipherText, encryptionKey, null);
  }

  @Override
  public Computation<SInt> build(SequentialProtocolBuilder builder) {
    BasicNumericFactory basicNumericFactory = builder.getBasicNumericFactory();
    int requiredRounds = MiMCEncryptionProtocolNaiveImpl4
        .getRequiredRounds(basicNumericFactory, requestedRounds);
    final OInt threeInverse = builder.getOIntFactory().getOInt(
        calculateThreeInverse(basicNumericFactory)
    );

    return builder.seq(seq -> {

			/*
       * We're in the first round so we need to initialize
			 * by subtracting the key from the input cipher text
			 */
      Computation<SInt> sub = seq.numeric().sub(cipherText, encryptionKey);
      return new IterationState(1, sub);
    }).whileLoop(
        (state) -> state.round < requiredRounds,
        (state, seq) -> {
          /*
           * We're in an intermediate round where we compute
           * c_{i} = c_{i - 1}^(3^(-1)) - K - r_{i}
           * where K is the symmetric key
           * i is the reverse of the current round count
           * r_{i} is the round constant
           * c_{i - 1} is the cipher text we have computed
           * in the previous round
           */
          Computation<SInt> inverted = seq.createAdvancedNumericBuilder()
              .exp(state.value, threeInverse);

          /*
           * In order to obtain the correct round constants we will
           * use the reverse round count
           * (since for decryption we are going in the reverse order)
           */
          int reverseRoundCount = requiredRounds - state.round;

          // Get round constant
          OIntFactory oIntFactory = seq.getOIntFactory();
          OInt roundConstant = oIntFactory.getOInt(
              MiMCConstants.getConstant(reverseRoundCount, basicNumericFactory.getModulus())
          );

          // subtract key and round constant
          NumericBuilder numeric = seq.numeric();
          Computation<SInt> updatedValue = numeric
              .sub(numeric.sub(inverted, encryptionKey), roundConstant);
          return new IterationState(state.round + 1, updatedValue);
        }
    ).seq((state, seq) -> {
      /*
       * We're in the last round so we just need to compute
			 * c^{-3} - K
			 */
      AdvancedNumericBuilder advancedNumericBuilder = seq.createAdvancedNumericBuilder();
      Computation<SInt> inverted = advancedNumericBuilder.exp(state.value, threeInverse);

      return seq.numeric().sub(inverted, encryptionKey);
    });
  }

  private BigInteger calculateThreeInverse(BasicNumericFactory basicNumericFactory) {
    BigInteger modulus = basicNumericFactory.getModulus();
    BigInteger expP = modulus.subtract(BigInteger.ONE);
    return BigInteger.valueOf(3).modInverse(expP);
  }

  private static final class IterationState implements Computation<IterationState> {

    private final int round;
    private final Computation<SInt> value;

    private IterationState(int round,
        Computation<SInt> value) {
      this.round = round;
      this.value = value;
    }

    @Override
    public IterationState out() {
      return this;
    }
  }
}
