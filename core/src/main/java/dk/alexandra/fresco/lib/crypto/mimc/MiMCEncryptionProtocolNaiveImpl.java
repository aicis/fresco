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

public class MiMCEncryptionProtocolNaiveImpl extends AbstractRoundBasedProtocol implements MiMCEncryptionProtocol {

	// TODO: require that our modulus - 1 and 3 are co-prime
	
	private SInt mimcKey;
	private SInt cipherText;
	private int requiredRounds;
	private int round;
	private BasicNumericFactory bnf;
	private SInt nextRoundMessage;

	/**
	 * Implementation of the MiMC decryption protocol.
	 * 
	 * @param plainText
	 *            The secret-shared plain text to encrypt.
	 * @param mimcKey
	 *            The symmetric (secret-shared) key we will use to encrypt.
	 * @param cipherText
	 * 			  The secret-shared result of the encryption will be stored here.
	 * @param requiredRounds
	 *            The number of rounds to use. If you don't know the correct
	 *            number, we use log_3(modulus) rounded up.
	 * @param bnf
	 * 			  Factory we will use for arithmetic operations.
	 */
	public MiMCEncryptionProtocolNaiveImpl(SInt plainText, SInt mimcKey, SInt cipherText, 
			int requiredRounds, BasicNumericFactory bnf) {
		this.mimcKey = mimcKey;
		this.cipherText = cipherText;
		this.requiredRounds = requiredRounds;
		this.bnf = bnf; 
		this.nextRoundMessage = plainText;
		this.round = 0;
	}
	
	@Override
	public ProtocolProducer nextProtocolProducer() {
		NumericProtocolBuilder b = new NumericProtocolBuilder(bnf);
		
		if (round == 0) {
			/*
			 * In the first round we compute c = (p + K)^{3}
			 * where p is the plain text.
			 */
			SInt masked = b.add(nextRoundMessage, mimcKey);
			nextRoundMessage = b.exp(masked, 3);
			
			round++;
			return b.getProtocol();
		}
		if (round < requiredRounds) {
			/* 
			 * We're in an intermediate round where we compute
			 * c_{i} = (c_{i - 1} + K + r_{i})^{3}
			 * where K is the symmetric key
			 * i is the reverse of the current round count
			 * r_{i} is the round constant
			 * c_{i - 1} is the cipher text we have computed
			 * in the previous round
			 */
			BigInteger _roundConstant = MiMCConstants.getConstant(round, bnf.getModulus());		
			SInt roundConstant = b.known(_roundConstant);
			
			// Add key and round constant			
			SInt masked = b.add(roundConstant, b.add(nextRoundMessage, mimcKey));
			// Compute the cube
			nextRoundMessage = b.exp(masked, 3);
			
			round++;
			return b.getProtocol();
		}
		else if (round == requiredRounds) {
			/* 
			 * We're in the last round so we just mask the current
			 * cipher text with the encryption key
			 */
			b.copy(cipherText, b.add(nextRoundMessage, mimcKey));
			round++;
			return b.getProtocol();
		}
		else {
			return null;
		}
	}
}
