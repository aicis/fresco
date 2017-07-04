/*
 * Copyright (c) 2015 FRESCO (http://github.com/aicis/fresco).
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
package dk.alexandra.fresco.suite.spdz.datatypes;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Random;

public class SpdzCommitment {

	private BigInteger value;
	private BigInteger randomness;
	private Random rand;
	private BigInteger commitment;
	private MessageDigest H;

	public SpdzCommitment(MessageDigest H, BigInteger value, Random rand){
		this.value = value;
		this.rand = rand;
		this.H = H;
	}

	public BigInteger getCommitment(BigInteger modulus) {
		if (this.commitment != null){
			return this.commitment;
		}
		H.update(value.toByteArray());
		this.randomness = new BigInteger(modulus.bitLength(), rand);
		H.update(this.randomness.toByteArray());
		this.commitment = new BigInteger(H.digest()).mod(modulus);
		return this.commitment;
	}

	public BigInteger getValue(){
		return this.value;
	}

	public BigInteger getRandomness(){
		return this.randomness;
	}

	@Override
	public String toString(){
		return "SpdzCommitment[v:"+this.value+", r:"+this.randomness+", comm:"+this.commitment+"]";
	}
}
