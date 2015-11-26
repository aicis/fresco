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
package dk.alexandra.fresco.lib.math.linalg;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.helper.AbstractRoundBasedProtocol;
import dk.alexandra.fresco.lib.helper.CopyProtocolImpl;
import dk.alexandra.fresco.lib.helper.builder.NumericProtocolBuilder;
import dk.alexandra.fresco.lib.helper.sequential.SequentialProtocolProducer;

public class InnerProductCircuitImpl extends AbstractRoundBasedProtocol
		implements InnerProductCircuit {

	private final BasicNumericFactory bnProvider;
	private final DotProductFactory dotProdProvider;
	private final SInt[] aVector, bVector;
	private final SInt result;
	private final OInt[] publicBVector;
	private ProtocolProducer gp;
	private int round = 0;
	private SInt[] results;

	public InnerProductCircuitImpl(SInt[] aVector, SInt[] bVector, SInt result,
			BasicNumericFactory bnProvider, DotProductFactory dotProdProvider) {
		if (aVector.length != bVector.length) {
			throw new MPCException("Lengths of input arrays do not match");
		}
		this.aVector = aVector;
		this.bVector = bVector;
		this.publicBVector = null;
		this.result = result;
		this.bnProvider = bnProvider;
		this.dotProdProvider = dotProdProvider;
	}

	public InnerProductCircuitImpl(SInt[] aVector, OInt[] bVector, SInt result,
			BasicNumericFactory bnProvider, DotProductFactory dotProdProvider) {
		if (aVector.length != bVector.length) {
			throw new MPCException("Lengths of input arrays do not match");
		}
		this.aVector = aVector;
		this.bVector = null;
		this.publicBVector = bVector;
		this.result = result;
		this.bnProvider = bnProvider;
		this.dotProdProvider = dotProdProvider;
	}

	@Override
	public ProtocolProducer nextGateProducer() {
		gp = null;
		if (round == 0) {
			int vectorLength = aVector.length;
			results = new SInt[vectorLength];
			if (vectorLength == 1) {
				round = 2;
				if (publicBVector == null) {
					gp = bnProvider.getMultCircuit(aVector[0], bVector[0],
							result);
				} else {
					gp = bnProvider.getMultCircuit(publicBVector[0],
							aVector[0], result);
				}
				return gp;
			}
			for (int i = 0; i < vectorLength; i++) {
				results[i] = bnProvider.getSInt();
			}
			if (publicBVector != null) {
				gp = dotProdProvider.getDotProductCircuit(aVector, publicBVector,
						results);
			} else {
				gp = dotProdProvider.getDotProductCircuit(aVector, bVector, results);
			}
			round++;
		} else if (round == 1) {
			NumericProtocolBuilder build = new NumericProtocolBuilder(bnProvider);
			SInt sumresult = build.sum(results);
			results = null;
			ProtocolProducer copy = new CopyProtocolImpl<SInt>(sumresult,  result);			
			gp = new SequentialProtocolProducer(build.getCircuit(), copy);
			round++;
		} else {
			gp = null;
		}
		return gp;
	}
}