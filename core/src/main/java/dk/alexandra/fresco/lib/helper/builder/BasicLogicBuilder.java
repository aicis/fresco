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
package dk.alexandra.fresco.lib.helper.builder;

import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.OBool;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.lib.logic.AbstractBinaryFactory;

/**
 * This protocol builder wraps the abstract binary factory. I.e., supports
 * building binary protocols.
 * 
 * Many methods comes in a plain and a 'inPlace' version. The plain version
 * generates and returns new SBools to hold the result of the computation. The
 * 'inPlace' versions allows to give specific SBools that should be overwritten
 * with the result. This way the 'inPlace' versions help save memory by not
 * creating temporary SBools when they are not needed.
 * 
 * 
 */
public class BasicLogicBuilder extends AbstractProtocolBuilder {

	/**
	 * The factory used to build all protocols
	 */
	private AbstractBinaryFactory bf;

	/**
	 * Construct a new builder for basic Boolean protocols.
	 * 
	 * @param bp
	 *            a factory of binary protocols.
	 */
	public BasicLogicBuilder(AbstractBinaryFactory bp) {
		this.bf = bp;
	}

	/**
	 * Appends an output protocol for an array of SBools
	 * 
	 * @param sb the SBools to be output
	 * @return the OBools holding the resulting output
	 */
	public OBool[] output(SBool... sbs) {
		OBool[] outs = new OBool[sbs.length];
		beginParScope();
		for (int i = 0; i < sbs.length; i++) {
			outs[i] = output(sbs[i]);
		}
		endCurScope();
		return outs;
	}

	/**
	 * Appends an output protocol for a single SBool
	 * 
	 * @param sb the SBool to be output
	 * @return the OBool holding the resulting output
	 */
	public OBool output(SBool sb) {
		OBool out = bf.getOBool();
		append(bf.getOpenProtocol(sb, out));
		return out;
	}

	/**
	 * Gets an array of known SBools from an array of values
	 * 
	 * @param bs
	 *            the values
	 * @return the resulting SBools
	 */
	public SBool[] knownSBool(boolean[] bs) {
		SBool[] result = new SBool[bs.length];
		for (int i = 0; i < bs.length; i++) {
			result[i] = bf.getKnownConstantSBool(bs[i]);
		}
		return result;
	}

	/**
	 * Gets a single known SBool of a given value
	 * 
	 * @param b
	 *            the value
	 * @return the resulting SBool
	 */
	public SBool knownSBool(boolean b) {
		SBool known = bf.getKnownConstantSBool(b);
		return known;
	}

	/**
	 * Gets an array of known SBools from an array of values with values given
	 * as bytes.
	 * 
	 * @param bs
	 *            the values
	 * @return the resulting SBools
	 */
	public SBool[] knownSBool(byte[] bs) {
		SBool[] result = new SBool[bs.length];
		for (int i = 0; i < bs.length; i++) {
			if (bs[i] == 0){
				result[i] = bf.getKnownConstantSBool(false);
			} else {
				result[i] = bf.getKnownConstantSBool(true);
			}
		}
		return result;
	}

	/**
	 * Creates a known public OBool value from the given boolean.
	 * 
	 * @param b
	 *            the value to assign to the OBool
	 * @return an OBool containing the given value
	 */
	public OBool getOBool(boolean b) {
		return bf.getKnownConstantOBool(b);
	}

	/**
	 * Appends a AND protocol to the current protocol.
	 * 
	 * @param left
	 *            the SBool holding the left argument.
	 * @param right
	 *            the SBool holding the right argument.
	 * @return an SBool holding the output of the appended protocol.
	 */
	public SBool and(SBool left, SBool right) {
		SBool result = bf.getSBool();
		append(bf.getAndProtocol(left, right, result));
		return result;
	}

	public void andInPlace(SBool result, SBool left, SBool right) {
		append(bf.getAndProtocol(left, right, result));
	}

	/**
	 * Appends AND protocols to the current protocol to do entry-wise AND of two bit
	 * strings.
	 * 
	 * @param left
	 *            an SBool array holding the left bit string.
	 * @param right
	 *            an SBool array holding the right bit string.
	 * @return an SBool array holding the output of the appended protocols.
	 */
	public SBool[] and(SBool[] left, SBool[] right) {
		checkLengths(left, right);
		SBool[] result = new SBool[left.length];
		beginParScope();
		for (int i = 0; i < left.length; i++) {
			result[i] = and(left[i], right[i]);
		}
		endCurScope();
		return result;
	}

	public void andInPlace(SBool[] result, SBool[] left, SBool[] right) {
		checkLengths(left, right);
		checkLengths(left, result);
		beginParScope();
		for (int i = 0; i < left.length; i++) {
			andInPlace(result[i], left[i], right[i]);
		}
		endCurScope();
	}

	/**
	 * Appends a NOT protocol to the current protocol.
	 * 
	 * @param in
	 *            the SBool holding the argument to be neprotocold.
	 * @return an SBool holding the output of the appended protocol.
	 */
	public SBool not(SBool in) {
		SBool result = bf.getSBool();
		notInPlace(result, in);
		return result;
	}

	public void notInPlace(SBool result, SBool in) {
		append(bf.getNotProtocol(in, result));
	}

	public void notInPlace(SBool[] result, SBool[] in) {
		beginParScope();
		for (int i = 0; i < in.length; i++) {
			notInPlace(result[i], in[i]);
		}
		endCurScope();
		return;
	}

	/**
	 * Appends a XOR protocol to the current protocol.
	 * 
	 * @param left
	 *            the SBool holding the left argument.
	 * @param right
	 *            the SBool holding the right argument.
	 * @return an SBool holding the output of the appended protocol.
	 */
	public SBool xor(SBool left, SBool right) {
		SBool result = bf.getSBool();
		append(bf.getXorProtocol(left, right, result));
		return result;
	}

	public void xorInPlace(SBool result, SBool left, SBool right) {
		append(bf.getXorProtocol(left, right, result));
	}

	/**
	 * Appends XOR protocols to the current protocol to do entry-wise XOR of two bit
	 * strings.
	 * 
	 * @param left
	 *            an SBool array holding the left bit string.
	 * @param right
	 *            an SBool array holding the right bit string.
	 * @return an SBool array holding the output of the appended protocols.
	 */
	public SBool[] xor(SBool[] left, SBool[] right) {
		checkLengths(left, right);
		SBool[] result = new SBool[left.length];
		beginParScope();
		for (int i = 0; i < left.length; i++) {
			result[i] = xor(left[i], right[i]);
		}
		endCurScope();
		return result;
	}

	public void xorInPlace(SBool[] result, SBool[] left, SBool[] right) {
		checkLengths(left, right);
		checkLengths(left, result);
		beginParScope();
		for (int i = 0; i < left.length; i++) {
			xorInPlace(result[i], left[i], right[i]);
		}
		endCurScope();
	}

	/**
	 * Appends a OR protocol to the current protocol.
	 * 
	 * @param left
	 *            the SBool holding the left argument.
	 * @param right
	 *            the SBool holding the right argument.
	 * @return an SBool holding the output of the appended protocol.
	 */
	public SBool or(SBool left, SBool right) {
		SBool result = bf.getSBool();
		append(bf.getOrProtocol(left, right, result));
		return result;
	}

	public void orInPlace(SBool result, SBool left, SBool right) {
		append(bf.getOrProtocol(left, right, result));		
	}

	/**
	 * Appends OR protocols to the current protocol to do entry-wise OR of two bit
	 * strings.
	 * 
	 * @param left
	 *            an SBool array holding the left bit string.
	 * @param right
	 *            an SBool array holding the right bit string.
	 * @return an SBool array holding the output of the appended protocols.
	 */
	public SBool[] or(SBool[] left, SBool[] right) {
		checkLengths(left, right);
		SBool[] result = new SBool[left.length];
		beginParScope();
		for (int i = 0; i < left.length; i++) {
			result[i] = or(left[i], right[i]);
		}
		endCurScope();
		return result;
	}

	/**
	 * Appends a conditional select protocol to the current protocol. The output
	 * of this protocol on inputs a and b and condition bit c is the bit r := c ?
	 * a : b.
	 * 
	 * @param condition
	 *            the SBool holding the condition on which to select.
	 * 
	 * @param left
	 *            the SBool holding the left argument.
	 * @param right
	 *            the SBool holding the right argument.
	 * @return an SBool holding the output of the appended protocol.
	 */
	public SBool condSelect(SBool condition, SBool left, SBool right) {
		SBool result = bf.getSBool();
		beginSeqScope();
		SBool x = xor(left, right);
		SBool y = and(condition, x);
		append(bf.getXorProtocol(y, right, result));
		endCurScope();
		return result;
	}

	public void condSelectInPlace(SBool result, SBool condition, SBool left, SBool right) {
		beginSeqScope();
		SBool x = xor(left, right);
		SBool y = and(condition, x);
		append(bf.getXorProtocol(y, right, result));
		endCurScope();
	}

	/**
	 * Appends a conditional select protocol on bit strings to the current
	 * protocol. The output of this protocol on input strings A and B and
	 * condition c is the string R := c ? A : B.
	 * 
	 * @param condition
	 *            the SBool holding the condition on which to select.
	 * @param left
	 *            the SBool array holding the left argument.
	 * @param right
	 *            the SBool array holding the right argument.
	 * @return an SBool array holding the output of the appended protocol.
	 */
	public SBool[] condSelect(SBool condition, SBool[] left, SBool[] right) {
		checkLengths(left, right);
		SBool[] result = new SBool[left.length];
		beginParScope();
		for (int i = 0; i < left.length; i++) {
			result[i] = condSelect(condition, left[i], right[i]);
		}
		endCurScope();
		return result;
	}

	public void condSelectInPlace(SBool[] result, SBool condition, SBool[] left, SBool[] right) {
		checkLengths(left, right);
		checkLengths(left, result);
		beginParScope();
		for (int i = 0; i < left.length; i++) {
			condSelectInPlace(result[i], condition, left[i], right[i]);
		}
		endCurScope();
	}

	/**
	 * Appends a greater-than protocol to the current protocol. The output of this
	 * protocol is the greater-than (<) relation between its two input strings.
	 * 
	 * @param left
	 *            the SBool array holding the left argument.
	 * @param right
	 *            the SBool array holding the right argument.
	 * @return an SBool holding the output of the appended protocol.
	 */
	public SBool greaterThan(SBool[] left, SBool[] right) {
		checkLengths(left, right);
		SBool result = bf.getSBool();
		append(bf.getBinaryComparisonProtocol(left, right, result));
		return result;
	}

	/**
	 * Appends a keyed compare and swap protocol. This protocol swaps two
	 * key-value pairs to that the left pair becomes the pair with the largest
	 * key.
	 * 
	 * @param leftKey
	 *            an SBool array representing the key of the left pair
	 * @param leftValue
	 *            an SBool array representing the value of the left pair
	 * @param rightKey
	 *            an SBool array representing the key of the right pair
	 * @param rightValue
	 *            an SBool array representing the value of the right pair
	 */
	public void keyedCompareAndSwap(SBool[] leftKey, SBool[] leftValue, SBool[] rightKey, SBool[] rightValue) {
		checkLengths(leftKey, rightKey);
		checkLengths(leftValue, rightValue);
		append(bf.getKeyedCompareAndSwapProtocol(leftKey, leftValue, rightKey,
				rightValue));
		return;
	}

	/**
	 * Appends a equality protocol to the current protocol. The output of this
	 * protocol is the equals (==) relation between its two input strings.
	 * 
	 * @param left
	 *            the SBool array holding the left argument.
	 * @param right
	 *            the SBool array holding the right argument.
	 * @return an SBool holding the output of the appended protocol.
	 */
	public SBool equality(SBool[] left, SBool[] right) {
		checkLengths(left, right);
		SBool result = bf.getSBool();
		append(bf.getBinaryEqualityProtocol(left, right, result));
		return result;
	}

	/**
	 * Appends a copy protocol to the current protocol copying the value of one
	 * SBool to an other.
	 * 
	 * @param src
	 *            the source SBool
	 * @param dest
	 *            the destination SBool
	 */
	public void copy(SBool src, SBool dest) {
		append(bf.getCopyProtocol(src, dest));
		return;
	}

	/**
	 * Appends a copy protocol to the current protocol copying the values of an
	 * SBool array to an other array of SBools.
	 * 
	 * @param src
	 *            the source SBool array
	 * @param dest
	 *            the destination SBool array
	 */
	public void copy(SBool[] src, SBool[] dest) {
		checkLengths(src, dest);
		beginParScope();
		for (int i = 0; i < src.length; i++) {
			copy(src[i], dest[i]);
		}
		endCurScope();
		return;
	}

	/**
	 * Checks the length of two arrays to see if they are equal.
	 * 
	 * @param left
	 *            the left array
	 * @param right
	 *            the right array
	 */
	private void checkLengths(SBool[] left, SBool[] right) {
		if (left.length != right.length) {
			throw new IllegalArgumentException("Arguments must be of equal length");
		}
		return;
	}

	@Override
	public void addProtocolProducer(ProtocolProducer gp) {
		append(gp);
	}
}
