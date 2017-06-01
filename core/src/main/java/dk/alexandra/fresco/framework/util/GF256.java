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
package dk.alexandra.fresco.framework.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;


/**
 * This class represents an element in the finite field GF(2^8) =
 * GF(2)[x] / M, where M is the ideal over the polynomial x^8 + x^4 + x^3 + x + 1.
 * This is the same irreducible polynomial as the one used in AES scheme.
 * 
 * The simplest generator of GF(2^8) is the element x^2 + 1 = 0x03.
 * 
 * @author Jakob I. Pagter and Kasper Damgaard
 *
 */
public class GF256 {	
	
	/**
	 * Constructs an element from a binary description of the value.
	 * 
	 * @param val The value in binary notation.
	 * 
	 */
	public GF256(String val){
		int tmp = Integer.parseInt(val, 2);
		this.setElement((byte)tmp);
		//this.setElement(Byte.parseByte(val,2)); 
	}

	public static GF256 valueOf(long val) {
		if(GF256.elms.containsKey(val)) {
			return GF256.elms.get(val);
		} else {
			GF256 res = new GF256((byte)val);
			GF256.elms.put(val, res);
			return res;
		}
	}
	
	/**
	 * The actual value in the field; as an example, the value 10000111 represents the 
	 * polynomial x^7 + x^2 + x^1 + x^0.
	 * 
	 */
	private byte element;
	
	private static final Map<Long,GF256> elms = new HashMap<Long,GF256>();
	
	/**
	 * This is a fixed table of logarithms, such that x = g^L(x), where g is the
	 * generator x^2 + 1 = 0x03. The table is taken from the book
	 * http://www.cs.utsa.edu/~wagner/lawsbookcolor/laws.pdf.
	 */
	public static final byte[] L;
	
	/**
	 * This is a fixed table of exponentials, such that E(x) = g^x, where g is the
	 * generator x^2 + 1 = 0x03. The table is taken from the book
	 * http://www.cs.utsa.edu/~wagner/lawsbookcolor/laws.pdf. 
	 */
	public static final byte[] E;
	
	/**
	 * This is a fixed table of the S-box computation (used in AES) for each 
	 * possible input to an S-box. 
	 * Taken from the book: http://www.cs.utsa.edu/~wagner/lawsbookcolor/laws.pdf
	 */
	public static byte[] Sbox = new byte[256]; // SubBytes table
	
	/**
	 * This is a fixed table of the inverse S-box used in AES.
	 * Taken from the book: http://www.cs.utsa.edu/~wagner/lawsbookcolor/laws.pdf  
	 */
	public static byte[] invS = new byte[256]; // inv of SubBytes table
	
	/**
	 * This is a fixed table of multiplicative inverses.
	 * Taken from the book: http://www.cs.utsa.edu/~wagner/lawsbookcolor/laws.pdf
	 */
	public static byte[] inv = new byte[256]; // multi inverse table
	
	/**
	 * This is a fixed table of powers of 2, such that powX(x) = 2^x
	 * Taken from the book: http://www.cs.utsa.edu/~wagner/lawsbookcolor/laws.pdf
	 */
	public static byte[] powX = new byte[15]; // powers of x = 0x02
	
	/**
	 * The static constructor constructs the tables that are used for multiplication.
	 * @todo C#: cipher.Mode = CipherMode.CBC;
	 * @todo: C#: cipher.IV = new byte[crypt.BlockSize/8]; // initializes to all zero
	 */	
	
	public static int Nb = 4; //always has this value in AES, but might change some day.
	public static int keyCount = 0; //position in key for RoundKey algortihm (=0 in each encrypt)
		
	private static int ithBit(byte b, int i) {
		int m[] = {0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, 0x80};
		return (b & m[i]) >> i; 
	}
	
	private static int subBytes(byte b) {
		int res = 0;
		/* If using a local AES scheme, this should be there, but 
		 * for MPC we assume b is already inverted.
		if (b != 0) // if b == 0, leave it alone
			b = (byte)(FFInv(b) & 0xff);
			*/ 
		byte c = (byte)0x63;
		for (int i = 0; i < 8; i++) {
			int temp = 0;
			temp = ithBit(b, i) ^ ithBit(b, (i+4)%8) ^
					ithBit(b, (i+5)%8) ^ ithBit(b, (i+6)%8) ^
					ithBit(b, (i+7)%8) ^ ithBit(c, i); 
			res = res | (temp << i);
		}
		return res; 
	}
	
	static 
	{
		// create the E and F tables
		E = new byte[256];
	      byte x = (byte)0x01;
	      int index = 0;
	      E[index++] = (byte)0x01;
	      for (int i = 0; i < 255; i++) {
	         byte y = FFMul(x, (byte)0x03);
	         E[index++] = y;
	         x = y;
	      }
		L = new byte[256];
		for (int i = 0; i < 255; i++) 
		{
			L[E[i] & 0xff] = (byte)i;
		}
				
		// create the inverse multiplication table
		for (int i = 0; i < 256; i++)
			inv[i] = (byte)(FFInv((byte)(i & 0xff)) & 0xff);
		
		// create the S-box table 	
		index = 0;
		for (int i = 0; i < 256; i++)
			Sbox[i] = (byte)(subBytes((byte)(i & 0xff)) & 0xff);
		
		// create the inverted S-box table
		for (int i = 0; i < 256; i++) 
			invS[Sbox[i] & 0xff] = (byte)i; 
		
		// create the powX table			
		x = (byte)0x02;
		byte xp = x;
		powX[0] = 1; powX[1] = x;
		for (int i = 2; i < 15; i++) {
			xp = FFMulFast(xp, x);
			powX[i] = xp;
		}
		
		GF256.elms.clear();
		GF256.zero = new GF256("0");
		GF256.one = new GF256("1");
		GF256.two = new GF256("10");
		//GF256.half = GF256.two.inverse();
		
		GF256.elms.put(0L, GF256.zero);
		GF256.elms.put(1L, GF256.one);
		GF256.elms.put(2L, GF256.two);
	}
	
	public static GF256 zero;
	public static GF256 one;
	public static GF256 two;
	//public static GF256 half;	


	/**
	 * Computes the product of the two elements using fast table lookup.
	 * The implementation is taken from 
	 * http://www.cs.utsa.edu/~wagner/laws/ATables.html.
	 */
	private static byte FFMulFast(byte a, byte b)
	{
	      int t = 0;
	      if (a == 0 || b == 0) return 0;
	      t = (L[(a & 0xff)] & 0xff) + (L[(b & 0xff)] & 0xff);
	      if (t > 255) {
	    	  t = t - 255; //TODO: Mod Util.p instead here?
	      }
	      return E[(t & 0xff)];
	}
	
	/**
	 * Computes the product of the two elements using shifting 
	 * technique. The implementation is taken from 
	 * http://www.cs.utsa.edu/~wagner/laws/ATables.html.
	 * 
	 */
	private static byte FFMul(byte a, byte b) 
	{
		byte aa = a, bb = b, r = 0, t;
		while (aa != 0) {
		   if ((aa & 1) != 0)
		      r = (byte)(r ^ bb);
		   t = (byte)(bb & 0x80);
		   bb = (byte)(bb << 1);
		   if (t != 0)
		      bb = (byte)(bb ^ 0x1b);
		   aa = (byte)((aa & 0xff) >> 1); 
		}
		return r;
	}
	
	
	/**
	 * Computes the multiplicative inverse of an elements. The 
	 * implementation is taken from 
	 * http://www.cs.utsa.edu/~wagner/laws/ATables.html.
	 * 
	 * @param b The element to invert. If this is zero, zero is returned.
	 * 
	 */
	private static byte FFInv(byte b) 
	{	
		byte e = L[b & 0xff];
		return E[0xff - (e & 0xff)];
	}

	public GF256(byte val) 
	{
		this.setElement(val);
	}

	
	/*
	public synchronized GF256 generateRandom(Random ran, int id)
	{
		byte[] r = new byte[1];
		ran.nextBytes(r);
		for (int i=0; i<4; i++) r[0] ^= (id >> (i*8));
		GF256 res = new GF256(r[0]);

		return res;
	}
	*/
	
//	private byte[] encryptStringAES(SecretKey skey, String s) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException 
//	{
//		// Generate the secret key specs.
//		byte[] raw = skey.getEncoded();
//		SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
//		
//		// Instantiate the cipher
//		Cipher cipher = Cipher.getInstance("AES");
//		cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
//
//		// encrypt
//		byte[] input = s.getBytes();
//		byte[] res = cipher.doFinal(input);
//		return res;
//	}



//	/**
//	 * Creates pseudo random element based on an aes key and a public integer; the integer
//	 * is first encrypted under the key and the result is then used to generate a random
//	 * number that is either 0 or 1. Here, we just use the first bit of the encrypted id.
//	 * 
//	 * @param sKey the AES key
//	 * @param public_stuff the identifier
//	 * 
//	 */
//	public GF256(SecretKey skey, int public_stuff)  throws Exception
//	{
//		gflock.acquire();
//		byte[] enc = encrypt(public_stuff, skey); // this gives 128 bits
//		this.setElement((enc[0] & 0x80) == 0 ? (byte)0 : (byte)1); 
//		gflock.release();
//	}


	/**
	 *  The equality of two GF256Elements are solely based on the
	 *  equality of the values that they represent.
	 *  
	 */ 
	public boolean equals(Object o){
		assert(o instanceof GF256);
		return this.element == ((GF256)o).element;
	}


	/**
	 * Two GF256 objects representing same element are considered 
	 * identical.
	 * 
	 */
	public int hashCode() {
		return (int)this.element;	
	}


	// members
 
	public GF256 pow(int pow) {
		// really stupid implementation, but power is only used in the unit test
		if (pow == 0) return one();
		if (pow == 1) return new GF256(this.getElement());		
		GF256 res = new GF256(this.getElement());
		for (int i=1; i<pow; i++) 
			res = res.multiply(this);
		return res;
	}
	
	public GF256 multiply(GF256 number)	{
		return new GF256(GF256.FFMulFast(this.getElement(), number.getElement())); 
	}

	public GF256 add(GF256 number) {			
		// in GF(2^8) addition is just bitwise xor
		return new GF256( (byte)(this.getElement() ^ number.getElement()) );
	}
	
	public GF256 add(byte b) {		
		return new GF256( (byte)(this.getElement() ^ b));
	}
	

	public GF256 inverse() {
		return new GF256(inv[this.element & 0xff]);
	}
	
	/**
	 * This method assumes that the input byte is already inverted 
	 * @return The affine linear transformation on this. (aka. the last 
	 * part of the S-box)
	 */
	public GF256 Sbox() {
		return new GF256(Sbox[this.element & 0xff]);
	}
	
	public GF256 invSbox() {
		return new GF256(invS[this.element & 0xff]);
	}
	
	/**
	 * 
	 * @param i the exponent
	 * @return 2^i 
	 */
	public static GF256 Rcon(int i) {
		return new GF256(powX[i]);
	}


	public GF256 zero()
	{
		return GF256.zero;
	}

	public GF256 one() {
		return GF256.one;
	}
	
	/*
	public GF256 half() {
		return GF256.half;
	}
	*/
	
	public GF256 generateFromString(String val) 
	{
		return new GF256(val);
	}

	public static GF256 generateRandom(Random rgen) 
	{
		return new GF256((byte)rgen.nextInt());
	}

	public void setElement(byte element) {
		this.element = element;
	}
	
	public byte getElement() {
		return element;
	}

	public String toString() {
		return String.format("GF256(%X)", element);		
	}

	/**
	 * As we know that a-b = a+b in GF(2^8), we just call add.
	 * @param number
	 * @return this+number
	 */
	public GF256 subtract(GF256 number) {		
		return add(number);
	}
	
}
