/*******************************************************************************
 * Copyright (c) 2016 FRESCO (http://github.com/aicis/fresco).
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
package dk.alexandra.fresco.suite.tinytables.online.protocols;

import java.util.List;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.network.SCENetwork;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.Value;
import dk.alexandra.fresco.lib.field.bool.OpenBoolProtocol;
import dk.alexandra.fresco.suite.tinytables.datatypes.TinyTablesElement;
import dk.alexandra.fresco.suite.tinytables.online.TinyTablesProtocolSuite;
import dk.alexandra.fresco.suite.tinytables.online.datatypes.TinyTablesOBool;
import dk.alexandra.fresco.suite.tinytables.online.datatypes.TinyTablesSBool;

/**
 * <p>
 * This class represents an open-to-all protocol in the TinyTables preprocessing
 * phase.
 * </p>
 *
 * <p>
 * Here, each of the two players send his share of the masking parameter
 * <i>r</i> to the other player such that each player can add this to the masked
 * input <i>e = b + r</i> to get the unmasked output <i>b</i>.
 * </p>
 * 
 * @author Jonas Lindstrøm (jonas.lindstrom@alexandra.dk)
 *
 */
public class TinyTablesOpenToAllProtocol extends TinyTablesProtocol implements OpenBoolProtocol {

	private TinyTablesSBool toOpen;
	private TinyTablesOBool opened;

	public TinyTablesOpenToAllProtocol(int id, TinyTablesSBool toOpen, TinyTablesOBool opened) {
		super();
		this.id = id;
		this.toOpen = toOpen;
		this.opened = opened;
	}

	@Override
	public Value[] getInputValues() {
		return new Value[] { toOpen };
	}

	@Override
	public Value[] getOutputValues() {
		return new Value[] { opened };
	}

	@Override
	public EvaluationStatus evaluate(int round, ResourcePool resourcePool, SCENetwork network) {
		TinyTablesProtocolSuite ps = TinyTablesProtocolSuite.getInstance(resourcePool.getMyId());

		/*
		 * When opening a value, all players send their shares of the masking
		 * value r to the other players, and each player can then calculate the
		 * unmasked value as the XOR of the masked value and all the shares of
		 * the mask.
		 */
		switch (round) {
			case 0:
				TinyTablesElement myR = ps.getStorage().getMaskShare(id);
				network.sendToAll(myR);
				network.expectInputFromAll();
				return EvaluationStatus.HAS_MORE_ROUNDS;
			case 1:
				List<TinyTablesElement> maskShares = network.receiveFromAll();
				boolean mask = TinyTablesElement.open(maskShares);
				boolean result = toOpen.getValue().getShare() ^ mask;
				opened.setValue(result);
				return EvaluationStatus.IS_DONE;
			default:
				throw new MPCException("Cannot evaluate rounds larger than 1");
		}
	}

}
