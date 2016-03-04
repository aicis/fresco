/*******************************************************************************
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

import org.bouncycastle.util.Arrays;

import dk.alexandra.fresco.suite.spdz.utils.Util;

public class SpdzCommitment {
	
	private byte[] value;
	private byte[] randomness;
	private Random rand;
	private byte[] commitment;
	private MessageDigest H;
	
	public SpdzCommitment(MessageDigest H, byte[] value, Random rand){
		this.value = value;
		this.rand = rand;
		this.H = H;
	}
	public byte[] getCommitment(){
		if (this.commitment != null){
			return this.commitment;
		}		
		H.update(new BigInteger(value).toByteArray());
		this.randomness = new BigInteger(Util.getModulus().bitLength(), rand).toByteArray(); 
		H.update(new BigInteger(randomness).toByteArray());
		this.commitment = new BigInteger(H.digest()).toByteArray();
		return this.commitment;
	}
	
	public byte[] getValue(){
		return this.value;		
	}

	public byte[] getRandomness(){
		return this.randomness;
	}
	
	/**
	 * Returns true if the given values match the commitment given. 
	 * @param commitment
	 * @param value
	 * @param randomness
	 * @return
	 */
	public static boolean checkCommitment(MessageDigest H, byte[] commitment, byte[] value, byte[] randomness){
		H.update(new BigInteger(value).toByteArray());
		H.update(new BigInteger(randomness).toByteArray());
		return Arrays.areEqual(new BigInteger(commitment).toByteArray(), new BigInteger(H.digest()).toByteArray());
	}
	
	@Override
	public String toString(){
		return "SpdzCommitment[v:"+this.value+", r:"+this.randomness+", comm:"+this.commitment+"]";
	}
}
