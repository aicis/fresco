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
package dk.alexandra.fresco.lib.helper;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.field.integer.OpenIntProtocol;


/**
 * Helper for various matrix and vector/array operations.
 * 
 */
public class AlgebraUtil {
	
	/**
	 * Opens a matrix of SInts, using the provided factory.
	 * @param closed The SInt matrix 
	 * @param open The output OInt matrix
	 * @param provider The factory for creating the open circuits.
	 * @return ProtocolProducer
	 */
	public static ProtocolProducer makeOpenCircuit(SInt[][] closed, OInt[][] open, BasicNumericFactory provider) {
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

	/**
	 * Opens an array of SInts, using the provided factory.
	 * @param closed The SInt array 
	 * @param open The output OInt array
	 * @param provider The factory for creating the open circuits.
	 * @return ProtocolProducer
	 */
	public static ProtocolProducer makeOpenCircuit(SInt[] closed, OInt[] open, BasicNumericFactory provider) {
		if (open.length != closed.length) {
			throw new IllegalArgumentException("Amount of closed and open integers does not match. " +
					"Open: " + open.length + " Closed: " + closed.length);
		}
		OpenIntProtocol[] openings = new OpenIntProtocol[open.length]; 
		for (int i = 0; i < open.length; i++) {
			openings[i] = provider.getOpenProtocol(closed[i], open[i]);
		}
		return new ParallelProtocolProducer(openings);
	}

	/**
	 * Convert an array of arrays into a list of lists.
	 * @param array The array to convert
	 * @return An ArrayList 
	 */
	public static <T> List<List<T>> arrayToList(T[][] array){
		List<List<T>> output = new ArrayList<List<T>>(array.length);
		for(int i=0; i< array.length; i++){
			output.add(arrayToList(array[i]));
		}
		return output;
	}
	
	/**
	 * Convert an array into a list.
	 * @param array The array to convert
	 * @return An ArrayList 
	 */
	public static <T> List<T> arrayToList(T[] array){
		List<T> output = new ArrayList<T>(array.length);
		for(int i=0; i< array.length; i++){
			output.add(array[i]);
		}
		return output;
	}
	
	/**
	 * Construct a matrix of OInts, using the provided factory.
	 * @param matrix The output OInt matrix
	 * @param provider The factory for creating the OInts.
	 * @return The output OInt matrix
	 */
	public static OInt[][] oIntFill(OInt[][] matrix, BasicNumericFactory provider) {
		for(OInt[] vector: matrix) {
			vector = oIntFill(vector, provider);
		}
		return matrix;
	}
	/**
	 * Construct an array of OInts, using the provided factory.
	 * @param open The output OInt array
	 * @param provider The factory for creating the OInts.
	 * @return The output OInt vector
	 */
	public static OInt[] oIntFill(OInt[] vector, BasicNumericFactory provider) {
		for(int i = 0; i < vector.length; i++) {
			vector[i] = provider.getOInt();
		}
		return vector;
	}
	
	/**
	 * Fill a matrix with SInts, using the provided factory. Existing SInts
	 * will not be overwritten. 
	 * @param matrix The output SInt matrix
	 * @param provider The factory for creating the SInts.
	 * @return The output SInt matrix
	 */
	public static SInt[][] sIntFillRemaining(SInt[][] matrix, BasicNumericFactory provider) {
		for(SInt[] vector: matrix) {
			vector = sIntFillRemaining(vector, provider);
		}
		return matrix;
	}

	/**
	 * Fill a matrix with SInts, using the provided factory. Existing SInts
	 * will be overwritten. 
	 * @param matrix The output SInt matrix
	 * @param provider The factory for creating the SInts.
	 * @return The output SInt matrix
	 */		
	public static SInt[][] sIntFill(SInt[][] matrix, BasicNumericFactory provider) {
		for(SInt[] vector: matrix) {
			vector = sIntFill(vector, provider);
		}
		return matrix;
	}
	
	/**
	 * Fill an array with SInts, using the provided factory. Existing SInts
	 * will not be overwritten. 
	 * @param vector The output SInt array
	 * @param provider The factory for creating the SInts.
	 * @return The output SInt array
	 */
	public static SInt[] sIntFillRemaining(SInt[] vector, BasicNumericFactory provider) {
		for(int i = 0; i < vector.length; i++) {
			if (vector[i] == null) {
				vector[i] = provider.getSInt();
			}
		}
		return vector;
	}
	
	/**
	 * Fill an array with SInts, using the provided factory. Existing SInts
	 * will be overwritten. 
	 * @param vector The output SInt array
	 * @param provider The factory for creating the SInts.
	 * @return The output SInt array
	 */	
	public static SInt[] sIntFill(SInt[] vector, BasicNumericFactory provider) {
		for(int i = 0; i < vector.length; i++) {
			vector[i] = provider.getSInt();
		}
		return vector;
	}

	/**
	 * Fill the provided matrix with random numbers
	 * @param matrix The output matrix
	 * @param bitLenght The size of the random rumbers
	 * @param rand A source of randomness
	 * @return The output matrix
	 */
	public static BigInteger[][] randomFill(BigInteger[][] matrix, int bitLenght, Random rand) {
		for(BigInteger[] vector: matrix) {
			vector = randomFill(vector, bitLenght, rand);
		}
		return matrix;
	}

	/**
	 * Fill the provided array with random numbers
	 * @param vector The output array
	 * @param bitLenght The size of the random rumbers
	 * @param rand A source of randomness
	 * @return The output array
	 */
	public static BigInteger[] randomFill(BigInteger[] vector, int bitLength, Random rand) {
		for(int i = 0; i < vector.length; i++) {
			vector[i] = new BigInteger(bitLength, rand); 
		}
		return vector;
	}

}
