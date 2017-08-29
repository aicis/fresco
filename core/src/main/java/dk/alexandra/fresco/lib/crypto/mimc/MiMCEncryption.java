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
import dk.alexandra.fresco.framework.builder.ComputationBuilder;
import dk.alexandra.fresco.framework.builder.numeric.NumericBuilder;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumeric;
import java.math.BigInteger;

public class MiMCEncryption implements ComputationBuilder<SInt, ProtocolBuilderNumeric> {

  // TODO: require that our modulus - 1 and 3 are co-prime

  private final Computation<SInt> encryptionKey;
  private final Computation<SInt> plainText;
  private final Integer requestedRounds;

  /**
   * Implementation of the MiMC decryption protocol.
   *
   * @param plainText The secret-shared plain text to encrypt.
   * @param encryptionKey The symmetric (secret-shared) key we will use to encrypt.
   * @param requiredRounds The number of rounds to use.
   */
  public MiMCEncryption(
      Computation<SInt> plainText, Computation<SInt> encryptionKey, Integer requiredRounds) {
    this.encryptionKey = encryptionKey;
    this.plainText = plainText;
    this.requestedRounds = requiredRounds;
  }

  /**
   * Implementation of the MiMC decryption protocol.
   * Using default amount of rounds, log_3(modulus) rounded up.
   *
   * @param plainText The secret-shared plain text to encrypt.
   * @param encryptionKey The symmetric (secret-shared) key we will use to encrypt.
   */
  public MiMCEncryption(
      Computation<SInt> plainText, Computation<SInt> encryptionKey) {
    this(plainText, encryptionKey, null);
  }


  @Override
  public Computation<SInt> buildComputation(ProtocolBuilderNumeric builder) {
    final int requiredRounds = getRequiredRounds(builder.getBasicNumeric(), requestedRounds);
    BigInteger three = BigInteger.valueOf(3);
    /*
     * In the first round we compute c = (p + K)^{3}
		 * where p is the plain text.
		 */
    return builder.seq(seq -> {
      Computation<SInt> add = seq.numeric().add(plainText, encryptionKey);
      return new IterationState(1, seq.advancedNumeric().exp(add, three));
    }).whileLoop(
        (state) -> state.round < requiredRounds,
        (seq, state) -> {
          /*
           * We're in an intermediate round where we compute
           * c_{i} = (c_{i - 1} + K + r_{i})^{3}
           * where K is the symmetric key
           * i is the reverse of the current round count
           * r_{i} is the round constant
           * c_{i - 1} is the cipher text we have computed
           * in the previous round
           */
          BigInteger roundConstantInteger = MiMCConstants
              .getConstant(state.round, seq.getBasicNumeric().getModulus());
          NumericBuilder numeric = seq.numeric();
          Computation<SInt> masked = numeric.add(
              roundConstantInteger,
              numeric.add(state.value, encryptionKey)
          );
          Computation<SInt> updatedValue = seq.advancedNumeric().exp(masked, three);
          return new IterationState(state.round + 1, updatedValue);
        }
    ).seq((seq, state) ->
        /*
         * We're in the last round so we just mask the current
         * cipher text with the encryption key
         */
        seq.numeric().add(state.value, encryptionKey)
    );
  }

  static int getRequiredRounds(BasicNumeric basicNumeric, Integer requestedRounds) {
    final int requiredRounds;
    if (requestedRounds == null) {
      BigInteger modulus = basicNumeric.getModulus();
      requiredRounds = (int) Math.ceil(Math.log(modulus.doubleValue()) / Math.log(3));
    } else {
      requiredRounds = requestedRounds;
    }
    return requiredRounds;
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
