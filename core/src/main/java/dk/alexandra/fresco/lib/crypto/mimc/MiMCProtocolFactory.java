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

import java.math.BigInteger;

import dk.alexandra.fresco.framework.ProtocolFactory;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;

/**
 * Creates the MiMC protocols needed for encryption and decryption. Description
 * was found here: https://eprint.iacr.org/2016/542.pdf
 * 
 * @author Kasper Damgaard
 *
 */
public class MiMCProtocolFactory implements ProtocolFactory {

	/**
	 * Returns an encryption protocol which takes a message and converts it into
	 * a ciphertext using the given key.
	 * 
	 * @param message
	 *            The message to encrypt
	 * @param key
	 *            The key to encrypt with
	 * @param encryptedMessage
	 *            Placeholder for the result.
	 * @param noOfRounds
	 *            The number of rounds the encryption protocol should use. If in
	 *            doubt, use log_3(modulus) rounded up.
	 * @param bnf
	 *            A basic numeric factory
	 * @param cube
	 *            If true, use the more advanced version which requires further
	 *            preprocessed material. If false, the naive version is used.
	 * @return A protocol implementing the MiMC encryption block cipher
	 */
	public MiMCEncryptionProtocol getMiMCEncryptor(SInt message, SInt key, SInt encryptedMessage, int noOfRounds,
			BasicNumericFactory bnf, boolean cube) {
		if (cube) {
			throw new RuntimeException("Not implemented yet.");
		} else {
			return new MiMCEncryptionProtocolNaiveImpl(message, key, encryptedMessage, noOfRounds, bnf);
		}
	}

	/**
	 * Returns the secret shared cleartext of the given ciphertext using the
	 * given key. Note that this is significantly slower (as in: A LOT) than
	 * encryption, so if possible, use only the encryption part in your
	 * application.
	 * 
	 * @param encMessage
	 *            The cipher text to decrypt
	 * @param key
	 *            The key to used when encrypting
	 * @param dec
	 *            Container for the result
	 * @param noOfRounds
	 *            The number of rounds the encryption protocol should use. If in
	 *            doubt, use log_3(modulus) rounded up. In any case it should be
	 *            the same as for the encryption scheme.
	 * @param threeInverse
	 *            The inverse of 3 mod (p-1)
	 * @param bnf
	 *            A basic numeric factory
	 * @param cube
	 *            If true, use the more advanced version which requires further
	 *            preprocessed material. If false, the naive version is used.
	 * @return A protocol implementing the MiMC decryption block cipher.
	 */
	public ProtocolProducer getMiMCDecryptor(SInt encMessage, SInt key, SInt dec, int noOfRounds,
			BigInteger threeInverse, BasicNumericFactory bnf, boolean cube) {
		if (cube) {
			throw new RuntimeException("Not implemented yet.");
		} else {
			return new MiMCDecryptionProtocolNaiveImpl(encMessage, key, dec, noOfRounds, threeInverse, bnf);
		}
	}
}
