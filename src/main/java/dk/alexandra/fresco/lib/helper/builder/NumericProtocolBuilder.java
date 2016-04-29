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
package dk.alexandra.fresco.lib.helper.builder;

import java.math.BigInteger;

import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.helper.AbstractRepeatProtocol;
import dk.alexandra.fresco.lib.helper.CopyProtocolImpl;
import dk.alexandra.fresco.lib.helper.builder.tree.TreeCircuit;
import dk.alexandra.fresco.lib.helper.builder.tree.TreeCircuitNodeGenerator;

public class NumericProtocolBuilder extends AbstractProtocolBuilder {

	private BasicNumericFactory bnp;

	public NumericProtocolBuilder(BasicNumericFactory bnp) {
		this.bnp = bnp;
	}

	/**
	 * Gets a (h x w) size matrix of new SInts.
	 * 
	 * @param values
	 *            values to fill the SInts with
	 * @return a new matrix of initialized SInts.
	 */
	public SInt[][] getSIntMatrix(int[][] values) {
		SInt[][] matrix = new SInt[values.length][values[0].length];
		for (int i = 0; i < values.length; i++) {
			matrix[i] = getSIntArray(values[i]);
		}
		return matrix;
	}

	/**
	 * Gets an array of newly initialized SInts
	 * 
	 * @return a new array of initialized SInts.
	 */
	public SInt[] getSIntArray(int[] values) {
		SInt[] array = new SInt[values.length];
		for (int i = 0; i < values.length; i++) {
			array[i] = getSInt(values[i]);
		}
		return array;
	}

	/**
	 * Get a new SInt.
	 * 
	 * @param value
	 *            the value to fill the SInt with
	 * @return a SInt.
	 */
	public SInt getSInt(int value) {
		return bnp.getSInt(value);
	}

	/**
	 * Gets a (h x w) size matrix of new SInts.
	 * 
	 * @param h
	 *            height
	 * @param w
	 *            width
	 * @return a new matrix of initialized SInts.
	 */
	public SInt[][] getSIntMatrix(int h, int w) {
		SInt[][] matrix = new SInt[h][w];
		for (int i = 0; i < h; i++) {
			matrix[i] = getSIntArray(w);
		}
		return matrix;
	}

	/**
	 * Gets an array of newly initialized SInts
	 * 
	 * @param length
	 *            length of array
	 * @return a new array of initialized SInts.
	 */
	public SInt[] getSIntArray(int length) {
		SInt[] array = new SInt[length];
		for (int i = 0; i < length; i++) {
			array[i] = getSInt();
		}
		return array;
	}

	/**
	 * Get a new SInt.
	 * 
	 * @return a SInt.
	 */
	public SInt getSInt() {
		return bnp.getSInt();
	}

	public SInt known(BigInteger value) {
		SInt sValue = bnp.getSInt();
		ProtocolProducer loader = bnp.getSInt(value, sValue);
		append(loader);
		return sValue;
	}

	public SInt[] known(BigInteger[] values) {
		SInt[] sValues = new SInt[values.length];
		beginParScope();
		for (int i = 0; i < sValues.length; i++) {
			sValues[i] = known(values[i]);
		}
		endCurScope();
		return sValues;
	}

	public SInt known(int value) {
		SInt sValue = bnp.getSInt();
		ProtocolProducer loader = bnp.getSInt(value, sValue);
		append(loader);
		return sValue;
	}

	public SInt[] known(int[] values) {
		SInt[] sValues = new SInt[values.length];
		beginParScope();
		for (int v : values) {
			known(v);
		}
		endCurScope();
		return sValues;
	}

	/**
	 * Adds to SInts
	 * 
	 * @param left
	 *            the lefthand input
	 * @param right
	 *            the righthand input
	 * @return an SInt representing the result of the addition
	 */
	public SInt add(SInt left, SInt right) {
		SInt out = bnp.getSInt();
		append(bnp.getAddProtocol(left, right, out));
		return out;
	}

	/**
	 * Adds the lefthand array of SInts element-wise to the righthand array.
	 * Note this means the righthand array must be at least as long as the
	 * lefthand array.
	 * 
	 * @param left
	 *            the lefthand input array
	 * @param right
	 *            the righthand input array
	 * @return an array of SInts representing the result of the addition. Note
	 *         this array has the same length as lefthand input array
	 */
	public SInt[] add(SInt[] left, SInt[] right) {
		SInt[] out = getSIntArray(left.length);
		beginParScope();
		try {
			append(new ParAdditions(left, right, out));
			for (int i = 0; i < left.length; i++) {
				out[i] = add(left[i], right[i]);
			}
		} catch (IndexOutOfBoundsException e) {
			throw new IllegalArgumentException(
					"The righthand input array " + "most be at least as long as the left hand input arry", e);
		}
		endCurScope();
		return out;
	}

	private class ParAdditions extends AbstractRepeatProtocol {
		int i = 0;
		SInt[] left;
		SInt[] right;
		SInt[] out;

		public ParAdditions(SInt[] left, SInt[] right, SInt[] out) {
			this.left = left;
			this.right = right;
		}

		@Override
		protected ProtocolProducer getNextGateProducer() {
			if (i < left.length) {
				ProtocolProducer addition = bnp.getAddProtocol(left[i], right[i], out[i]);
				i++;
				return addition;
			} else {
				return null;
			}
		}
	}

	/**
	 * Appends a circuit that sums an array of terms. The method uses a
	 * recursive tree algorithm to parallelize the computation.
	 * 
	 * @param terms
	 *            the terms to be summed up.
	 * @return an SInt that will be loaded with the sum.
	 */
	public SInt sum(SInt[] terms) {
		SInt sum = getSInt();
		ProtocolProducer sumTree = new TreeCircuit(new SumNodeGenerator(terms, sum));
		append(sumTree);
		return sum;
	}

	private class SumNodeGenerator implements TreeCircuitNodeGenerator {

		private SInt[] terms;
		private SInt[] intermediate;
		private SInt result;

		public SumNodeGenerator(SInt[] terms, SInt result) {
			this.terms = terms;
			this.intermediate = new SInt[terms.length];
			this.result = result;
		}

		@Override
		public ProtocolProducer getNode(int i, int j) {
			SInt left, right, out;
			if (intermediate[i] == null) {
				if (i == 0) {
					intermediate[i] = result;
				} else {
					intermediate[i] = getSInt();
				}
				left = terms[i];
			} else {
				left = intermediate[i];
			}
			if (intermediate[j] == null) {
				right = terms[j];
			} else {
				right = intermediate[j];
			}
			out = intermediate[i];
			ProtocolProducer addition = bnp.getAddProtocol(left, right, out);
			return addition;
		}

		@Override
		public int getLength() {
			return terms.length;
		}
	}

	/**
	 * Takes a number of values and multiplies them all.
	 * @param factors
	 * @return
	 */
	public SInt mult(SInt[] factors) {
		SInt sum = getSInt();
		ProtocolProducer multTree = new TreeCircuit(new MultNodeGenerator(factors, sum));
		append(multTree);
		return sum;
	}

	private class MultNodeGenerator implements TreeCircuitNodeGenerator {

		private SInt[] terms;
		private SInt[] intermediate;
		private SInt result;

		public MultNodeGenerator(SInt[] terms, SInt result) {
			this.terms = terms;
			this.intermediate = new SInt[terms.length];
			this.result = result;
		}

		@Override
		public ProtocolProducer getNode(int i, int j) {
			SInt left, right, out;
			if (intermediate[i] == null) {
				if (i == 0) {
					intermediate[i] = result;
				} else {
					intermediate[i] = getSInt();
				}
				left = terms[i];
			} else {
				left = intermediate[i];
			}
			if (intermediate[j] == null) {
				right = terms[j];
			} else {
				right = intermediate[j];
			}
			out = intermediate[i];
			ProtocolProducer mult = bnp.getMultCircuit(left, right, out);
			return mult;
		}

		@Override
		public int getLength() {
			return terms.length;
		}
	}

	/**
	 * Multiplies two SInts
	 * 
	 * @param left
	 *            the lefthand input
	 * @param right
	 *            the righthand input
	 * @return an SInt representing the result of the multiplication
	 */
	public SInt mult(SInt left, SInt right) {
		SInt out = bnp.getSInt();
		append(bnp.getMultCircuit(left, right, out));
		return out;
	}

	/**
	 * Scales the right side array of SInts.
	 * 
	 * @param scale
	 *            the scale
	 * @param right
	 *            the righthand input array
	 * @return an array of SInts representing the result of the multiplication.
	 * 
	 */
	public SInt[] scale(SInt scale, SInt[] right) {
		SInt[] out = new SInt[right.length];
		beginParScope();
		for (int i = 0; i < right.length; i++) {
			out[i] = mult(scale, right[i]);
		}
		endCurScope();
		return out;
	}

	/**
	 * Multiplies the lefthand array of SInts element-wise on the righthand
	 * array. Note this means the righthand array must be at least as long as
	 * the lefthand array.
	 * 
	 * @param left
	 *            the lefthand input array
	 * @param right
	 *            the righthand input array
	 * @return an array of SInts representing the result of the multiplication.
	 *         Note this array has the same length as lefthand input array.
	 */
	public SInt[] mult(SInt[] left, SInt[] right) {
		SInt[] out = new SInt[left.length];
		beginParScope();
		try {
			for (int i = 0; i < left.length; i++) {
				out[i] = mult(left[i], right[i]);
			}
		} catch (IndexOutOfBoundsException e) {
			throw new IllegalArgumentException(
					"The righthand input array " + "most be at least as long as the left hand input arry", e);
		}
		endCurScope();
		return out;
	}

	/**
	 * Subtracts the righthand SInt from the lefthand SInt.
	 * 
	 * @param left
	 *            the lefthand input
	 * @param right
	 *            the righthand input
	 * @return an SInt representing the result of the subtraction.
	 */
	public SInt sub(SInt left, SInt right) {
		SInt out = bnp.getSInt();
		append(bnp.getSubtractCircuit(left, right, out));
		return out;
	}

	/**
	 * Subtracts the righthand array of SInts element-wise from the lefthand
	 * array. The righthand array must be at least as long as the lefthand
	 * array.
	 * 
	 * @param left
	 *            the lefthand input array
	 * @param right
	 *            the righthand input array
	 * @return an array of SInts representing the result of the subtraction.
	 *         Note this array has the same length as lefthand input array.
	 */
	public SInt[] sub(SInt[] left, SInt[] right) {
		SInt[] out = new SInt[left.length];
		beginParScope();
		try {
			for (int i = 0; i < left.length; i++) {
				append(bnp.getSubtractCircuit(left[i], right[i], out[i]));
			}
		} catch (IndexOutOfBoundsException e) {
			throw new IllegalArgumentException(
					"The righthand input array " + "most be at least as long as the left hand input arry", e);
		}
		endCurScope();
		return out;
	}

	/**
	 * Computes the conditional selection operation. I.e., concretely computes
	 * the value <code>r</code> as
	 * <code> selector*(left - right) + right </code> For
	 * <code>selector == 0</code> this gives a value equal to <code>right</code>
	 * , for <code>selector == 1</code> a value equal to <code>left</code>.
	 * 
	 * @param selector
	 *            should be either 0 or 1.
	 * @param left
	 *            the left hand value
	 * @param right
	 *            the right hand value
	 * @return a SInt holding the value of
	 *         <code> selector*(left - right) + right </code>
	 */
	public SInt conditionalSelect(SInt selector, SInt left, SInt right) {
		SInt r = getSInt();
		conditionalSelect(selector, left, right, r);
		return r;
	}

	/**
	 * In place version of {@link #conditionalSelect(SInt, SInt, SInt)}
	 * 
	 * @param selector
	 *            should be either 0 or 1.
	 * @param left
	 *            the left hand value
	 * @param right
	 *            the right hand value
	 * @param result
	 *            a SInt in which to put the result. I.e, the value of
	 *            <code> selector*(left - right) + right </code>
	 */
	public void conditionalSelect(SInt selector, SInt left, SInt right, SInt result) {
		beginSeqScope();
		SInt diff = sub(left, right);
		SInt prod = mult(diff, selector);
		append(bnp.getAddProtocol(prod, right, result));
		endCurScope();
		return;
	}

	public SInt innerProduct(SInt[] left, SInt[] right) {
		beginSeqScope();
		SInt[] directProduct = this.mult(left, right);
		SInt innerProduct = this.sum(directProduct);
		endCurScope();
		return innerProduct;
	}

	/**
	 * Copies the value of one SInt into an other SInt.
	 * 
	 * Note: this uses the generic CopyProtocol implementation, it is not clear
	 * if this is safe for all protocol suites.
	 * @param to
	 *            the SInt to copy to
	 * @param from
	 *            the SInt to copy from
	 */
	public void copy(SInt to, SInt from) {
		append(new CopyProtocolImpl<SInt>(from, to));
	}

	@Override
	public void addGateProducer(ProtocolProducer gp) {
		append(gp);
	}
}
