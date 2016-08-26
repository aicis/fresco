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
package dk.alexandra.fresco.framework.math;

import java.math.BigInteger;

public class Util {

	private static int m[] = {0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, 0x80};
	
	/**
	 * Computes the floor(log_2(x))
	 */
	public static int log2(int n){
	    if(n <= 0) {
			throw new IllegalArgumentException();
		}
	    return 31 - Integer.numberOfLeadingZeros(n);
	}
	
	
	public static int IthBit(byte b, int i) {		
		return (b & m[i]) >> i; 
	}
	
	public static boolean ithBit(int no, int i){
		return ((no >> i)&0x01) == 1;
	}
	
	public static byte[] intToBits(int no){
		return intToBits(no, 32);
	}
	
	public static byte[] intToBits(int no, int amountOfBitsUsed){
		byte[] res = new byte[amountOfBitsUsed];		
		for(int i = 0; i < amountOfBitsUsed; i++){
			res[amountOfBitsUsed-i-1] = (byte) ((no >> i)&0x00000001);
		}
		return res;
	}
	
	public static int bitsToInt(byte[] bits){		
		int res = 0;
		BigInteger tmp = new BigInteger("2");
		for(int i = 0; i < bits.length; i++){
			if(bits[bits.length-1-i] == (byte)1){
				res+= tmp.pow(i).intValue();
			}
		}
		return res;
	}
	
	public static boolean isPowerOfTwo(int n) {
		return (n & (n - 1)) == 0;
	}
}
