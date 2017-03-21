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
package dk.alexandra.fresco.suite.spdz.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.framework.value.SIntFactory;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.field.integer.OpenIntProtocol;
import dk.alexandra.fresco.lib.field.integer.generic.IOIntProtocolFactory;
import dk.alexandra.fresco.lib.helper.ParallelProtocolProducer;

/**
 * 
 * @author kasper
 *	Class containing all global variables and helper methods 
 */
public class Util {
	
	private static BigInteger p = null; //Should be set by an initiation call
	private static BigInteger p_half;
	private static int size = 0; //should be set by an initiation call
	public static int EXP_PIPE_SIZE = 200+1; //R^-1, R, R^2, ..., R^200		
	
	public static final String ENCODING = "UTF-8";	
	
	public static BigInteger getModulus() {
		if(p == null) {
			throw new IllegalStateException("You need to set the modulus before you can retrieve it.");
		}
		return p;
	}
	
	public static void setModulus(BigInteger p) {		
		Util.p = p;
		Util.p_half = p.divide(BigInteger.valueOf(2));
		Util.size = p.toByteArray().length;
	}
	
	public static int getModulusSize() {
		return size;
	}

	private MessageDigest H;
	
	public MessageDigest getHashFunction(){
		if(H != null){
			return H;
		}
		try{
			H = MessageDigest.getInstance("SHA-256");
		}catch(NoSuchAlgorithmException e){
			e.printStackTrace();
		}
		return H;
	}
	
//	public static ReadableByteChannel getInputStream(String resource) throws IOException{
//		File f = new File(resource);
//		if (!f.exists()) {
//			throw new IllegalStateException("Resource not found :  " + resource);
//		}
//		FileInputStream fileInputStream = new FileInputStream(f);
//		FileChannel is = fileInputStream.getChannel();
//		return is;
//	}
	
	public static BigInteger convertRepresentation(BigInteger b) {
		BigInteger actual = b.mod(p);
		if (actual.compareTo(p_half) > 0) {
			actual = actual.subtract(p);
		}
		return actual;
	}

	public static InputStream getInputStream(String resource) throws IOException{
		File f = new File(resource);
		if (!f.exists()) {
			throw new IllegalStateException("Resource not found :  " + resource);
		}
		FileInputStream fileInputStream = new FileInputStream(f);
		BufferedInputStream bis = new BufferedInputStream(fileInputStream, 100000*1024);
		return bis;
	}
	
	/**
	 * Returns the numbers: M, M^2, M^3, ..., M^maxExp
	 * @param M
	 * @return
	 */
	public static BigInteger[] getClearExpPipe(BigInteger M){
		BigInteger[] Ms = new BigInteger[EXP_PIPE_SIZE-1];
		Ms[0] = M;
		for(int i = 1; i < Ms.length; i++){
			Ms[i] = Ms[i-1].multiply(M).mod(p);
		}
		return Ms;
	}
	
	/**
	 * Returns the coefficients of a polynomial of degree <i>l</i> such that
	 * <i>f(m) = 1</i> and <i>f(n) = 0</i> for <i>1 &le; n &le; l+1</i> and <i>n
	 * &ne; m</i> in <i>Z<sub>p</sub></i> (<i>p</i> should be set in
	 * {@link #setModulus(BigInteger)}). The first element in the array is the
	 * coefficient of the term with the highest degree, eg. degree <i>l</i>.
	 * 
	 * @param l
	 *            The desired degree of <i>f</i>
	 * @param m
	 *            The only non-zero integer point for <i>f</i> in the range
	 *            <i>1,2,...,l+1</i>.
	 * @return
	 */
	public static BigInteger[] constructPolynomial(int l, int m) {
		
		/*
		 * Let f_i be the polynoimial which is the product of the first i of
		 * (x-1), (x-2), ..., (x-(m-1)), (x-(m+1)), ..., (x-(l+1)). Then f_0 = 1
		 * and f_i = (x-k) f_{i-1} where k = i if i < m and k = i+1 if i >= m.
		 * Note that we are interested in calculating f(x) = f_l(x) / f_l(m).
		 * 
		 * If we let f_ij denote the j'th coefficient of f_i we have the
		 * recurrence relations:
		 * 
		 * f_i0 = 1 for all i (highest degree coefficient)
		 * 
		 * f_ij = f_{i-1, j} - f_{i-1, j-1} * k for j = 1,...,i
		 * 
		 * f_ij = 0 for j > i
		 */
		BigInteger[] f = new BigInteger[l+1];

		// Initial value: f_0 = 1
		f[0] = BigInteger.valueOf(1);

		/*
		 * We also calculate f_i(m) in order to be able to normalize f such that
		 * f(m) = 1. Note that f_i(m) = f_{i-1}(m)(m - k) with the above notation.
		 */
		BigInteger fm = BigInteger.ONE;
		
		for (int i = 1; i <= l; i++) {
			int k = i;
			if (i >= m) {
				k++;
			}

			// Apply recurrence relation
			f[i] = f[i - 1].multiply(BigInteger.valueOf(-k)).mod(p);
			for (int j = i - 1; j > 0; j--) {
				f[j] = f[j].subtract(BigInteger.valueOf(k).multiply(f[j - 1]).mod(p)).mod(p);
			}
			
			fm = fm.multiply(BigInteger.valueOf(m - k)).mod(p);
		}

		// Scale all coefficients of f_l by f_l(m)^{-1}.
		fm = fm.modInverse(p);
		for (int i = 0; i < f.length; i++) {
			f[i] = f[i].multiply(fm).mod(p);
		}
		
		return f;
	}
	
	public static ProtocolProducer makeOpenProtocol(SInt[][] closed, OInt[][] open, IOIntProtocolFactory factory) {
		if (open.length != closed.length) {
			throw new IllegalArgumentException("Amount of closed and open integers does not match. " +
					"Open: " + open.length + " Closed: " + closed.length);
		}
		ProtocolProducer[] openings = new ProtocolProducer[open.length];
		for (int i = 0; i < open.length; i++) {
			openings[i] = makeOpenProtocol(closed[i], open[i], factory);
		}
		return new ParallelProtocolProducer(openings);
	}
	
	public static ProtocolProducer makeOpenProtocol(SInt[] closed, OInt[] open, IOIntProtocolFactory factory) {
		if (open.length != closed.length) {
			throw new IllegalArgumentException("Amount of closed and open integers does not match. " +
					"Open: " + open.length + " Closed: " + closed.length);
		}
		OpenIntProtocol[] openings = new OpenIntProtocol[open.length]; 
		for (int i = 0; i < open.length; i++) {
			openings[i] = factory.getOpenProtocol(closed[i], open[i]);
		}
		return new ParallelProtocolProducer(openings);
	}
	
	public static OInt[][] oIntFill(OInt[][] matrix, BasicNumericFactory factory) {
		for(OInt[] vector: matrix) {
			vector = oIntFill(vector, factory);
		}
		return matrix;
	}
	
	public static OInt[] oIntFill(OInt[] vector, BasicNumericFactory factory) {
		for(int i = 0; i < vector.length; i++) {
			vector[i] = factory.getOInt();
		}
		return vector;
	}
	
	public static SInt[][] sIntFillRemaining(SInt[][] matrix, BasicNumericFactory factory) {
		for(SInt[] vector: matrix) {
			vector = sIntFillRemaining(vector, factory);
		}
		return matrix;
	}
		
	public static SInt[][] sIntFill(SInt[][] matrix, BasicNumericFactory factory) {
		for(SInt[] vector: matrix) {
			vector = sIntFill(vector, factory);
		}
		return matrix;
	}
	
	public static SInt[] sIntFillRemaining(SInt[] vector, BasicNumericFactory factory) {
		for(int i = 0; i < vector.length; i++) {
			if (vector[i] == null) {
				vector[i] = factory.getSInt();
			}
		}
		return vector;
	}
	
	
	public static SInt[] sIntFill(SInt[] vector, SIntFactory factory) {
		for(int i = 0; i < vector.length; i++) {
			vector[i] = factory.getSInt();
		}
		return vector;
	}
	
	public static BigInteger[][] randomFill(BigInteger[][] matrix, int bitLenght, Random rand) {
		for(BigInteger[] vector: matrix) {
			vector = randomFill(vector, bitLenght, rand);
		}
		return matrix;
	}
	
	public static BigInteger[] randomFill(BigInteger[] vector, int bitLength, Random rand) {
		for(int i = 0; i < vector.length; i++) {
			vector[i] = new BigInteger(bitLength, rand); 
			vector[i] = vector[i].subtract(BigInteger.valueOf(2).pow(bitLength-1)).mod(dk.alexandra.fresco.suite.spdz.utils.Util.p);
		}
		return vector;
	}
	
	public static BigInteger[][] zeroFill(BigInteger[][] matrix) {
		for(BigInteger[] vector: matrix) {
			vector = zeroFill(vector);
		}
		return matrix;
	}
	
	public static BigInteger[] zeroFill(BigInteger[] vector) {
		for(int i = 0; i < vector.length; i++) {
			vector[i] = BigInteger.valueOf(0);
		}
		return vector;
	}
	
	public static ProtocolProducer makeInputProtocols(BigInteger[][] values, int[][] pattern, SInt[][] matrix, BasicNumericFactory factory) {
		if (matrix.length != values.length || values.length != pattern.length || 
				values[0].length != matrix[0].length || values[0].length != pattern[0].length) {
			throw new RuntimeException("Input Dimensions are not equal");
		}
		ParallelProtocolProducer par = new ParallelProtocolProducer();
		for(int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++) {
				if (pattern[i][j] != 0) {
					par.append(factory.getCloseProtocol(values[i][j], matrix[i][j], pattern[i][j]));
				}
			}			
		}
		return par;
	}
	
	public static ProtocolProducer makeInputProtocols(BigInteger[] values, int[] pattern, SInt[] vector, BasicNumericFactory factory) {
		if (vector.length != values.length || vector.length != pattern.length) {throw new RuntimeException("Inputs are not equal length");}
		ParallelProtocolProducer input = new ParallelProtocolProducer();
		for(int i = 0; i < vector.length; i++) {
			if (pattern[i] != 0) {
				input.append(factory.getCloseProtocol(values[i], vector[i], pattern[i]));
			}
		}
		return input;
	}
	
	public static BigInteger getRandomNumber(Random rand) {
		byte[] bytes = new byte[size];
		rand.nextBytes(bytes);
		return new BigInteger(bytes).mod(p);
	}
}
