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
package dk.alexandra.fresco.lib.collections;

import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.helper.AbstractRoundBasedProtocol;
import dk.alexandra.fresco.lib.helper.ParallelProtocolProducer;
import dk.alexandra.fresco.lib.helper.sequential.SequentialProtocolProducer;
import dk.alexandra.fresco.lib.lp.LPFactory;

/**
 * Implements a lookup protocol using a linear number of equality protocols. A
 * lookup protocol is essentially a combination of a search and a conditional
 * select protocol. This does the search by simply comparing the lookup key to
 * all the keys in the list.
 * 
 * 
 */
public class LinearLookUpProtocol extends AbstractRoundBasedProtocol implements
		LookUpProtocol<SInt> {

	private final int securityParameter;
	private final SInt lookUpKey;
	private final SInt[] keys;
	private final SInt[] values;
	private final SInt[][] valueArrays;
	private final SInt[] outputArray;
	private final SInt outputValue;
	private LPFactory lpp;
	private BasicNumericFactory bnp;
	private final boolean singleValue;
	private final int size;

	private enum ROUND {
		COMPARE, SELECT, DONE
	}

	private ROUND round = ROUND.COMPARE;
	private SInt[] index;

	/**
	 * Makes a new LinearLookUpprotocol
	 * 
	 * @param securityParameter The statistical security parameter for the equality protocol
	 * @param lookUpKey
	 *            the key to look up.
	 * @param keys
	 *            the list of keys to search among.
	 * @param values
	 *            the values corresponding to each key.
	 * @param outputValue
	 *            the SInt to hold the outputValue. Note this will be unchanged
	 *            in case the look up key is not present in the key set.
	 * @param lpFactory
	 *            an LPFactory only here to provide the equality protocol.
	 * @param bnFactory
	 *            a basic numeric Factory.
	 */
	public LinearLookUpProtocol(int securityParameter, SInt lookUpKey, SInt[] keys, SInt[] values,
			SInt outputValue, LPFactory lpFactory,
			BasicNumericFactory bnFactory) {
		if (keys.length != values.length) {
			throw new IllegalArgumentException();
		}
		this.securityParameter = securityParameter;
		this.singleValue = true;
		this.lookUpKey = lookUpKey;
		this.keys = keys;
		this.values = values;
		this.valueArrays = null;
		this.outputValue = outputValue;
		this.outputArray = null;
		this.lpp = lpFactory;
		this.bnp = bnFactory;
		this.size = values.length;
	}

	public LinearLookUpProtocol(int securityParameter, SInt lookUpKey, SInt[] keys,
			SInt[][] valueArrays, SInt[] outputValues, LPFactory lpFactory,
			BasicNumericFactory bnFactory) {
		this.securityParameter = securityParameter;
		this.singleValue = false;
		this.lookUpKey = lookUpKey;
		this.keys = keys;
		this.values = null;
		this.valueArrays = valueArrays;
		this.outputValue = null;
		this.outputArray = outputValues;
		this.lpp = lpFactory;
		this.bnp = bnFactory;
		this.size = valueArrays.length;
	}

	@Override
	public ProtocolProducer nextProtocolProducer() {
		ProtocolProducer pp = null;
		if (round == ROUND.COMPARE) {
			ParallelProtocolProducer par = new ParallelProtocolProducer();
			index = new SInt[keys.length];
			for (int i = 0; i < keys.length; i++) {
				index[i] = bnp.getSInt();
				ProtocolProducer comp = lpp.getEqualityProtocol(bnp.getMaxBitLength(), 
						this.securityParameter, lookUpKey, keys[i], index[i]);
				par.append(comp);
			}
			pp = par;
			round = ROUND.SELECT;
		} else if (round == ROUND.SELECT) {
			if (singleValue) {
				ParallelProtocolProducer par = new ParallelProtocolProducer();
				for (int i = 0; i < size; i++) {
					ProtocolProducer select = lpp.getConditionalSelectProtocol(
							index[i], values[i], outputValue, outputValue);
					par.append(select);
				}
				pp = par;
			} else {
				SequentialProtocolProducer seq = new SequentialProtocolProducer();
				for (int i = 0; i < size; i++) {
					ParallelProtocolProducer par = new ParallelProtocolProducer();
					for (int j = 0; j < valueArrays[i].length; j++) {
						ProtocolProducer select = lpp.getConditionalSelectProtocol(
								index[i], valueArrays[i][j], outputArray[j],
								outputArray[j]);
						par.append(select);
					}
					seq.append(par);
				}
				pp = seq;
			}
			round = ROUND.DONE;
		}
		return pp;
	}
}