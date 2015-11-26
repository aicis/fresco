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
	public static int size = 0; //should be set by an initiation call
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
	 * Returns the coefficients of a polynomial of degree l such that f(1) = 1
	 * and f(n) = 0 for n=2,3,...,l+1 in Z_p. The first element in the array is
	 * the coefficient of the term with the highest degree.
	 * 
	 * @param l
	 * @return
	 */
	public static BigInteger[] constructPolynomial(int l) {
		BigInteger[] coefficients = new BigInteger[2];
		BigInteger[] oldCoefficients;
		
		// First we find the coefficients of f_l(x) = (x-2)(x-3)...(x-(l+1))
		// recursively using the formula a_{i,j} = a_{i-1,j} - (i+1) a_{i-1,j-1}
		// where a_{i,j} is the j'th coefficient of f_i.
		
		// Initial values: When l=1 the polynomial is x-2
		coefficients[0] = BigInteger.valueOf(1);
		coefficients[1] = BigInteger.valueOf(-2);
		oldCoefficients = coefficients;
		
		int i,j;
		for (i=2; i<=l; i++) {
			coefficients = new BigInteger[i+1];
			
			coefficients[0] = BigInteger.valueOf(1); // First coefficient is always 1
			for (j=1; j<i; j++)
				coefficients[j] = oldCoefficients[j].subtract(BigInteger.valueOf(i+1).multiply(oldCoefficients[j-1]));
			coefficients[i] = oldCoefficients[i-1].multiply(BigInteger.valueOf(-i-1));
			
			oldCoefficients = coefficients;
		}
		
		// The polynomial now satisfies that f_l(n) = 0 for n=2,3,...,l+1. To make
		// f_l(1) = 1 true, we need to divide by f_l(1). Note that f_l(x) =
		// (x-2)(x-3)...(x-(l+1)) so f(1) = (-1)(-2)(-3)...(-l)
		BigInteger f = BigInteger.ONE;
		for (i=1; i<=l; i++)
			f = f.multiply(BigInteger.valueOf(i)).negate().mod(p);
		f = f.modInverse(p);
		
		for (i=0; i<coefficients.length; i++)
			coefficients[i] = coefficients[i].multiply(f).mod(p);
		
		return coefficients;
	}
	
	public static ProtocolProducer makeOpenCircuit(SInt[][] closed, OInt[][] open, IOIntProtocolFactory provider) {
		if (open.length != closed.length) {
			throw new IllegalArgumentException("Amount of closed and open integers does not match. " +
					"Open: " + open.length + " Closed: " + closed.length);
		}
		ProtocolProducer[] openings = new ProtocolProducer[open.length];
		for (int i = 0; i < open.length; i++) {
			openings[i] = makeOpenCircuit(closed[i], open[i], provider);
		}
		return new ParallelProtocolProducer(openings);
	}
	
	public static ProtocolProducer makeOpenCircuit(SInt[] closed, OInt[] open, IOIntProtocolFactory provider) {
		if (open.length != closed.length) {
			throw new IllegalArgumentException("Amount of closed and open integers does not match. " +
					"Open: " + open.length + " Closed: " + closed.length);
		}
		OpenIntProtocol[] openings = new OpenIntProtocol[open.length]; 
		for (int i = 0; i < open.length; i++) {
			openings[i] = provider.getOpenCircuit(closed[i], open[i]);
		}
		return new ParallelProtocolProducer(openings);
	}
	
	public static OInt[][] oIntFill(OInt[][] matrix, BasicNumericFactory provider) {
		for(OInt[] vector: matrix) {
			vector = oIntFill(vector, provider);
		}
		return matrix;
	}
	
	public static OInt[] oIntFill(OInt[] vector, BasicNumericFactory provider) {
		for(int i = 0; i < vector.length; i++) {
			vector[i] = provider.getOInt();
		}
		return vector;
	}
	
	public static SInt[][] sIntFillRemaining(SInt[][] matrix, BasicNumericFactory provider) {
		for(SInt[] vector: matrix) {
			vector = sIntFillRemaining(vector, provider);
		}
		return matrix;
	}
		
	public static SInt[][] sIntFill(SInt[][] matrix, BasicNumericFactory provider) {
		for(SInt[] vector: matrix) {
			vector = sIntFill(vector, provider);
		}
		return matrix;
	}
	
	public static SInt[] sIntFillRemaining(SInt[] vector, BasicNumericFactory provider) {
		for(int i = 0; i < vector.length; i++) {
			if (vector[i] == null) {
				vector[i] = provider.getSInt();
			}
		}
		return vector;
	}
	
	
	public static SInt[] sIntFill(SInt[] vector, SIntFactory provider) {
		for(int i = 0; i < vector.length; i++) {
			vector[i] = provider.getSInt();
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
	
	public static ProtocolProducer makeInputGates(BigInteger[][] values, int[][] pattern, SInt[][] matrix, BasicNumericFactory provider) {
		if (matrix.length != values.length || values.length != pattern.length || 
				values[0].length != matrix[0].length || values[0].length != pattern[0].length) {
			throw new RuntimeException("Input Dimensions are not equal");
		}
		ParallelProtocolProducer par = new ParallelProtocolProducer();
		for(int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++) {
				if (pattern[i][j] != 0) {
					par.append(provider.getCloseCircuit(values[i][j], matrix[i][j], pattern[i][j]));
				}
			}			
		}
		return par;
	}
	
	public static ProtocolProducer makeInputGates(BigInteger[] values, int[] pattern, SInt[] vector, BasicNumericFactory provider) {
		if (vector.length != values.length || vector.length != pattern.length) {throw new RuntimeException("Inputs are not equal length");}
		ParallelProtocolProducer inputGates = new ParallelProtocolProducer();
		for(int i = 0; i < vector.length; i++) {
			if (pattern[i] != 0) {
				inputGates.append(provider.getCloseCircuit(values[i], vector[i], pattern[i]));
			}
		}
		return inputGates;
	}
}
