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
package dk.alexandra.fresco.lib.helper.builder;

import java.math.BigInteger;

import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.crypto.mimc.MiMCProtocolFactory;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;

/**
 * Builder for constructing Symmetric encryption schemes within MPC. The MiMC
 * encryption scheme is described here: https://eprint.iacr.org/2016/492.pdf
 * 
 * @author Kasper Damgaard
 *
 */
public class SymmetricEncryptionBuilder extends AbstractProtocolBuilder {

	private MiMCProtocolFactory mimcFac;
	private BasicNumericFactory bnf;
	private final int noOfMiMCRounds;
	private final BigInteger threeInverse;

	public SymmetricEncryptionBuilder(BasicNumericFactory bnf) {
		this.bnf = bnf;
		this.mimcFac = new MiMCProtocolFactory();
		// TODO: If adversary has access to less plaintext/ciphertexts pairs
		// than the modulus, we can reduce the number of rounds to: 
		// max{log_3(n), log_3(p)-2log_3(log_3(p))}
		this.noOfMiMCRounds = (int) Math.ceil(Math.log(bnf.getModulus().doubleValue()) / Math.log(3));
		
		BigInteger expP = bnf.getModulus().subtract(BigInteger.ONE);
		this.threeInverse = BigInteger.valueOf(3).modInverse(expP);
	}

	/**
	 * Encrypts a message under the given key using the MiMC block encryption
	 * scheme.
	 * 
	 * @param message
	 *            The secret shared message to encrypt.
	 * @param key
	 *            The secret shared key to encrypt under.
	 * @return The result of the encryption - i.e. the cipher text.
	 */
	public SInt mimcEncrypt(SInt message, SInt key) {
		SInt enc = bnf.getSInt();

		append(mimcFac.getMiMCEncryptor(message, key, enc, noOfMiMCRounds, bnf, false));
		return enc;
	}

	@Override
	public void addProtocolProducer(ProtocolProducer pp) {
		this.append(pp);
	}

	/**
	 * Decrypts a cipher text under the given key using the MiMC block
	 * encryption scheme.
	 * 
	 * @param encMessage
	 *            The encrypted message - i.e. the cipher text.
	 * @param key
	 *            The key to decrypt under
	 * @return The decrypted message - i.e. a plain text message (but secret
	 *         shared still)
	 */
	public SInt mimcDecrypt(SInt encMessage, SInt key) {
		SInt dec = bnf.getSInt();
		append(mimcFac.getMiMCDecryptor(encMessage, key, dec, noOfMiMCRounds, threeInverse, bnf, false));
		return dec;
	}

}
