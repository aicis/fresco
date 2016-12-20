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

import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.helper.AbstractSimpleProtocol;
import dk.alexandra.fresco.lib.helper.builder.NumericProtocolBuilder;

public class MiMCEncryptionProtocolNaiveImpl extends AbstractSimpleProtocol implements MiMCEncryptionProtocol{

	private SInt message;
	private SInt key;
	private SInt encryptedMessage;
	private int noOfRounds;
	BasicNumericFactory bnf;
	NumericProtocolBuilder builder;
	
	/**
	 * Naive implementation of the MiMC encryption.
	 * @param message The message to encrypt
	 * @param key The key to encrypt under
	 * @param noOfRounds The number of rounds to use. If you don't know the correct number, always use log_3(modulus) rounded up.
	 */
	public MiMCEncryptionProtocolNaiveImpl(SInt message, SInt key, SInt encryptedMessage, int noOfRounds, BasicNumericFactory bnf) {
		this.message = message;
		this.key = key;
		this.encryptedMessage = encryptedMessage;
		this.noOfRounds = noOfRounds;
		this.bnf = bnf; 
		this.builder = new NumericProtocolBuilder(bnf);
	}
	
	@Override
	protected ProtocolProducer initializeProtocolProducer() {		
		SInt nextRoundMessage = message;
		for(int i = 0; i < noOfRounds; i++) {
			nextRoundMessage = oneRound(i, nextRoundMessage);			
		}
		builder.addProtocolProducer(bnf.getAddProtocol(nextRoundMessage, key, encryptedMessage));
		
		return builder.getProtocol();
	}

	public SInt oneRound(int round, SInt thisRoundMessage) {
		BigInteger c_r_big = MiMCConstants.getConstant(round, bnf.getModulus());		
		SInt c_r = builder.known(c_r_big);
		
		SInt res = builder.add(c_r, builder.add(thisRoundMessage, key));
		return builder.mult(res, builder.mult(res, res));		
	}
}
