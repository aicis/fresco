/*******************************************************************************
 * Copyright (c) 2015, 2016 FRESCO (http://github.com/aicis/fresco).
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
package dk.alexandra.fresco.framework.util;

import java.util.BitSet;

public class ByteArithmetic {
	
	private static final int[] M = {0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, 0x80};
	
	// This is supposed to be a "static class", ie no instantiation
	private ByteArithmetic() {}
	
	/**
	 * @return x XOR y
	 */
	public static byte xor(byte x, byte y) {
		return (byte) (x ^ y);
	}
	
	
	/**
	 * @return x XOR y in res
	 * It is OK if x=res , but NOT OK if y=res.
	 */
	public static void xor(byte[] x, byte[] y, byte[] res) {
		for (int i=0; i<x.length; i++) {
			res[i] = x[i];
			res[i]^= y[i];
		}
	}

	public static byte mult(byte x, byte y) {
		return (byte) (x * y);
	}

	/**
	 * It is NOT OK if y=res.
	 */
	public static void mult(byte x, byte[] y, byte[] res) {
		for (int i=0; i<y.length; i++) {
			res[i]=0;
			if (x==1) res[i]^=y[i];		// TODO: why the XOR?
		}
	}	
	
	public static byte not(byte value) {
		if(value == 0) {
			return 1;
		} else {
			return 0;
		}
	}
	
	public static void packBits(byte[] unpacked, byte[] packed) {
		int packedLength = packed.length;
		for (int i=0; i<packedLength; i++) {
			int nextByte = 0;
			for (int j=0; j<8; j++) {
				final byte nextBit = unpacked[8*i+j];
				nextByte = (nextByte<<1 ^ nextBit);				
			}
			packed[i] = (byte)nextByte;
		}
	}
	public static void unpackBits(byte[] packed, byte[] unpacked) {
		int packedLength = packed.length;
		for (int i=0; i<packedLength; i++) {
			int nextByte = packed[i];
			for (int j=8-1; j>=0; j--) {
				if ((nextByte%2) == 0) unpacked[8*i+j] = 0;
				else unpacked[8*i+j] = 1;
				nextByte = nextByte>>1;
			}
		}
	}
	
	public static byte ithBit(byte b, int pos){		
		return (byte)((b & M[pos]) >> pos);
	}

	public static final byte[] intToByteArray(int value) {
		return new byte[] {
				(byte)(value >>> 24),
				(byte)(value >>> 16),
				(byte)(value >>> 8),
				(byte)value};
	}

	public static final int byteArrayToInt(byte [] b) {
		return (b[0] << 24)
				+ ((b[1] & 0xFF) << 16)
				+ ((b[2] & 0xFF) << 8)
				+ (b[3] & 0xFF);
	}
	
	public static BitSet intToBitSet(int i) {
	    BitSet bs = new BitSet(Integer.SIZE);
	    for (int k = 0; k < Integer.SIZE; k++) {
	        if ((i & (1 << k)) != 0) {
	            bs.set(k);
	        }
	    }
	    return bs;
	}
	
	public static int bitSetToInt(BitSet bs) {
	    int i = 0;
	    for (int pos = -1; (pos = bs.nextSetBit(pos+1)) != -1; ) {
	        i |= (1 << pos);
	    }
	    return i;
	}
	
	
	
	
	/**
	 * Convert hex string to boolean array.
	 * 1 --> true, 0 --> false
	 * 
	 */
	public static boolean[] toBoolean(String hex) throws IllegalArgumentException {
		if (hex.length() % 2 != 0)
			throw new IllegalArgumentException("Illegal hex string");
		boolean[] res = new boolean[hex.length() * 4]; //8
		//System.out.println("Lenght: " + hex.length());
		for (int i=0; i<hex.length() / 2; i++) {
			String sub = hex.substring(2*i,2*i +2);
			int value = Integer.parseInt(sub, 16);
			int numOfBits = 8;
			for (int j = 0; j < numOfBits; j++) {
				boolean val = (value & 1 << j) != 0;
		        res[8*i + (numOfBits - j - 1)] = val;
		    }
		}
		return res;
	}


	/**
	 * Convert boolean array to hex string.
	 * true --> 1, false --> 0
	 * 
	 */
	public static String toHex(boolean[] bits) {
		StringBuilder hex = new StringBuilder();
		// TODO: Assert bits.length % 4 == 0
		StringBuilder binb = new StringBuilder();
		for (int i=0; i<bits.length; i++) {
			binb.append(bits[i] ? "1" : "0");
		}
		String bin = binb.toString();
		
		for (int i=0; i<bin.length() / 4; i++) {
			String digit = bin.substring(i*4, i*4 + 4);
			Integer dec = Integer.parseInt(digit, 2);
			String hexStr = Integer.toHexString(dec);
			//System.out.println("Digit -> " + digit + " --> " + dec + " --> " + hexStr);
			hex.append(hexStr);
		}
		return hex.toString();
	}

	
	
}
