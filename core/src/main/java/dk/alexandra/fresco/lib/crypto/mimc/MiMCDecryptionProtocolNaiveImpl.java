/*******************************************************************************
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
 *******************************************************************************/
package dk.alexandra.fresco.lib.crypto.mimc;

import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.helper.AbstractRoundBasedProtocol;
import dk.alexandra.fresco.lib.helper.builder.NumericProtocolBuilder;
import java.math.BigInteger;

public class MiMCDecryptionProtocolNaiveImpl extends AbstractRoundBasedProtocol {

	// TODO: require that our modulus - 1 and 3 are co-prime
	
	private SInt mimcKey;
	private SInt plainText;
	private int requiredRounds;
	private int round;
	private BigInteger threeInverse;
	private BasicNumericFactory bnf;
	private SInt nextRoundMessage;

	/**
	 * Implementation of the MiMC decryption protocol.
	 * 
	 * @param cipherText
	 *            The secret-shared cipher text to decrypt.
	 * @param mimcKey
	 *            The symmetric (secret-shared) key we will use to decrypt.
	 * @param plainText
	 * 			  The secret-shared result of the decryption will be stored here.
	 * @param requiredRounds
	 *            The number of rounds to use. If you don't know the correct
	 *            number, always use log_3(modulus) rounded up.
	 * @param threeInverse
	 *            The inverse of 3 mod (p-1) where p is the modulus
	 *            we are working with. This will be used as an exponent
	 *            during decryption. Consequently, performance degrades as
	 *            this value increases.
	 * @param bnf
	 * 			  Factory we will use for arithmetic operations.
	 */
	public MiMCDecryptionProtocolNaiveImpl(SInt cipherText, SInt mimcKey, 
			SInt plainText, int requiredRounds, BigInteger threeInverse, 
			BasicNumericFactory bnf) {
		this.mimcKey = mimcKey;
		this.plainText = plainText;
		this.requiredRounds = requiredRounds;
		this.threeInverse = threeInverse;
		this.bnf = bnf;
		this.nextRoundMessage = cipherText;
		this.round = 0;
	}

	@Override
	public ProtocolProducer nextProtocolProducer() {
		NumericProtocolBuilder b = new NumericProtocolBuilder(bnf);
		if (this.round == 0) {
			/* 
			 * We're in the first round so we need to initialize
			 * by subtracting the key from the input cipher text
			 */			
			nextRoundMessage = b.sub(nextRoundMessage, mimcKey);
			round++;
			return b.getProtocol();
		}
		else if (this.round < requiredRounds) {
			/* 
			 * We're in an intermediate round where we compute
			 * c_{i} = c_{i - 1}^(3^(-1)) - K - r_{i}
			 * where K is the symmetric key
			 * i is the reverse of the current round count
			 * r_{i} is the round constant
			 * c_{i - 1} is the cipher text we have computed
			 * in the previous round
			 */
			SInt inverted = b.exp(nextRoundMessage, threeInverse);	
			
			/* 
			 * In order to obtain the correct round constants we will 
			 * use the reverse round count 
			 * (since for decryption we are going in the reverse order)
			 */
			int reverseRoundCount = requiredRounds - round;
			
			// Get round constant
			BigInteger _roundConstant = MiMCConstants.getConstant(
					reverseRoundCount, bnf.getModulus());
			SInt roundConstant = b.known(_roundConstant);
	
			// subtract key and round constant 
			nextRoundMessage = b.sub(b.sub(inverted, mimcKey), roundConstant);
			round++;
			return b.getProtocol();	
		}
		else if (this.round == requiredRounds) {
			/* 
			 * We're in the last round so we just need to compute
			 * c^{-3} - K
			 */
			// Compute c^{-3}
			SInt inverted = b.exp(nextRoundMessage, threeInverse);
			
			// Compute c^{-3} - K and store result
			b.copy(plainText, b.sub(inverted, mimcKey));
			
			round++;
			return b.getProtocol();
		}
		else {
			return null;
		}
	}
}
