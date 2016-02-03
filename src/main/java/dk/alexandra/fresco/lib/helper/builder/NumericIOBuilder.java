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
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.OIntFactory;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.framework.value.SIntFactory;
import dk.alexandra.fresco.lib.field.integer.generic.IOIntProtocolFactory;
import dk.alexandra.fresco.lib.helper.AbstractRepeatProtocol;

/**
 * A builder handling input/output related circuits for protocol suites supporting arithmetic.
 * 
 * @author psn
 * 
 */
public class NumericIOBuilder extends AbstractProtocolBuilder {

	private IOIntProtocolFactory iop;
	private SIntFactory sip;
	private OIntFactory oip;

	/**
	 * Constructs a new builder
	 * 
	 * @param iop
	 *            provider of input/output circuits
	 * @param sip
	 *            provider of SInts
	 * @param oip
	 *            provider of OInts
	 */
	public NumericIOBuilder(IOIntProtocolFactory iop,
			SIntFactory sip, OIntFactory oip) {
		super();
		this.iop = iop;
		this.sip = sip;
		this.oip = oip;
	}

	/**
	 * A convenient constructor when one provider implements all the needed
	 * interfaces (which will usually be the case)
	 * 
	 * @param provider
	 *            a provider providing SInt/OInt and input/output ciruicts.
	 */
	public <T extends IOIntProtocolFactory & SIntFactory & OIntFactory> NumericIOBuilder(
			T provider) {
		super();
		this.iop = provider;
		this.sip = provider;
		this.oip = provider;
	}

	/**
	 * Appends a circuit to input a matrix of BigIntegers.
	 * 
	 * @param is
	 *            the BigInteger values
	 * @param targetID
	 *            the party to input
	 * @return SInt's that will be loaded with the corresponding inputs, by the
	 *         appended circuit.
	 */
	public SInt[][] inputMatrix(BigInteger[][] is, int targetID) {
		SInt[][] sis = new SInt[is.length][is[0].length];
		beginParScope();
		for (int i = 0; i < is.length; i++) {
			sis[i] = inputArray(is[i], targetID);
		}
		endCurScope();
		return sis;
	}

	/**
	 * Appends a circuit to input an matrix of values by an other party. I.e.,
	 * the values or not held by this party.
	 * 
	 * @param h
	 *            height of matrix
	 * @param w
	 *            width of matrix
	 * @param targetID
	 *            the id of the inputing party
	 * @return SInts to be loaded with the inputted values.
	 */
	public SInt[][] inputMatrix(int h, int w, int targetID) {
		SInt[][] sis = new SInt[h][w];
		beginParScope();
		for (int i = 0; i < h; i++) {
			sis[i] = inputArray(w, targetID);
		}
		endCurScope();
		return sis;
	}

	/**
	 * Appends a circuit to input a array of BigIntegers.
	 * 
	 * @param is
	 *            the BigInteger values
	 * @param targetID
	 *            the party to input
	 * @return SInt's that will be loaded with the corresponding inputs, by the
	 *         appended circuit.
	 */
	public SInt[] inputArray(BigInteger[] is, int targetID) {
		SInt[] sis = new SInt[is.length];
		for (int i = 0; i < sis.length; i++) {
			sis[i] = sip.getSInt();
		}
		append(new InputArray(is, sis, targetID));
		return sis;
	}
	
	/**
	 * Appends a circuit to input a array of BigIntegers.
	 * 
	 * @param is
	 *            the BigInteger values
	 * @param targetID
	 *            the party to input
	 * @return SInt's that will be loaded with the corresponding inputs, by the
	 *         appended circuit.
	 */
	public SInt[] inputArray(int[] is, int targetID) {
		BigInteger[] tmp = new BigInteger[is.length];
		for(int i = 0; i < tmp.length; i++){
			tmp[i] = BigInteger.valueOf(is[i]);
		}
		return inputArray(tmp, targetID);
	}

	/**
	 * Appends a circuit to input an array of value by an other party. I.e., the
	 * values or not held by this party.
	 * 
	 * @param length
	 *            the length of the array
	 * @param targetID
	 *            the id of the party to input
	 * @return SInt's to be loaded with the input
	 */
	public SInt[] inputArray(int length, int targetID) {
		SInt[] sis = new SInt[length];
		for (int i = 0; i < length; i++) {
			sis[i] = sip.getSInt();
		}
		append(new InputArray(sis, targetID));
		return sis;
	}

	/**
	 * A class to efficiently handle large amounts of inputs.
	 * 
	 * @author psn
	 */
	private class InputArray extends AbstractRepeatProtocol {

		BigInteger[] is;
		SInt[] sis;
		int length;
		int targetID;
		int i = 0;

		public InputArray(SInt[] sis, int targetID) {
			this.length = sis.length;
			this.is = null;
			this.sis = sis;
			this.targetID = targetID;
		}

		public InputArray(BigInteger[] is, SInt[] sis, int targetID) {
			if (is.length != sis.length) {
				throw new IllegalArgumentException(
						"Array dimensions do not match.");
			}
			this.is = is;
			this.length = sis.length;
			this.sis = sis;
			this.targetID = targetID;
		}

		@Override
		protected ProtocolProducer getNextGateProducer() {
			ProtocolProducer input = null;
			if (i < length) {
				OInt oi = oip.getOInt();
				if (is != null) {
					oi.setValue(is[i]);
				}
				input = iop.getCloseProtocol(targetID, oi, sis[i]);
				i++;
			}
			return input;
		}
	}

	/**
	 * Appends a circuit to input a single BigInteger
	 * 
	 * @param i
	 *            the BigInteger value
	 * @param targetID
	 *            the party to input
	 * @return the SInt to be loaded with the input
	 */
	public SInt input(BigInteger i, int targetID) {
		SInt si = sip.getSInt();
		OInt oi = oip.getOInt();
		oi.setValue(i);
		append(iop.getCloseProtocol(targetID, oi, si));
		return si;
	}
	
	/**
	 * Appends a circuit to input a single BigInteger
	 * 
	 * @param i
	 *            the integer value
	 * @param targetID
	 *            the party to input
	 * @return the SInt to be loaded with the input
	 */
	public SInt input(int i, int targetID) {
		SInt si = sip.getSInt();
		OInt oi = oip.getOInt();
		oi.setValue(BigInteger.valueOf(i));
		append(iop.getCloseProtocol(targetID, oi, si));
		return si;
	}

	/**
	 * Appends a circuit to input a single value from an other party. I.e., the
	 * value is not given.
	 * 
	 * @param targetID
	 *            the id of the party inputting.
	 * @return SInt to be loaded with the input.
	 */
	public SInt input(int targetID) {
		SInt si = sip.getSInt();
		append(iop.getCloseProtocol(targetID, null, si));
		return si;
	}

	/**
	 * Appends a circuit to open a matrix of SInts. Output should be given to
	 * all parties.
	 * 
	 * @param sis
	 *            SInts to open
	 * @return the OInts to be loaded with the opened SInts
	 */
	public OInt[][] outputMatrix(SInt[][] sis) {
		OInt[][] ois = new OInt[sis.length][sis[0].length];
		beginParScope();
		for (int i = 0; i < sis.length; i++) {
			ois[i] = outputArray(sis[i]);
		}
		endCurScope();
		return ois;
	}

	/**
	 * Appends a circuit to open an array of SInts. Output should be given to
	 * all parties.
	 * 
	 * @param sis
	 *            SInts to open
	 * @return the OInts to be loaded with the opened SInts
	 */
	public OInt[] outputArray(SInt sis[]) {
		OInt[] ois = new OInt[sis.length];
		for (int i = 0; i < sis.length; i++) {
			ois[i] = oip.getOInt();
		}
		append(new OutputArray(sis, ois));
		return ois;
	}

	/**
	 * A class to efficiently handle large amounts of outputs.
	 * 
	 * @author psn
	 */
	private class OutputArray extends AbstractRepeatProtocol {

		OInt[] ois;
		SInt[] sis;
		int i = 0;

		public OutputArray(SInt[] sis, OInt[] ois) {
			this.ois = ois;
			this.sis = sis;
		}

		@Override
		protected ProtocolProducer getNextGateProducer() {
			ProtocolProducer output = null;
			if (i < ois.length) {
				output = iop.getOpenProtocol(sis[i], ois[i]);
				i++;
			}
			return output;
		}
	}

	/**
	 * Appends a circuit to open a single SInt. Output should be given to all
	 * parties.
	 * 
	 * @param sis
	 *            SInt to open
	 * @return the OInt to be loaded with the opened SInt
	 */
	public OInt output(SInt si) {
		OInt oi = oip.getOInt();
		append(iop.getOpenProtocol(si, oi));
		return oi;
	}

	@Override
	public void addGateProducer(ProtocolProducer gp) {
		append(gp);
	}
}
