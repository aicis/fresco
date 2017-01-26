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

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.network.SCENetwork;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.OBool;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.framework.value.Value;
import dk.alexandra.fresco.lib.field.bool.CloseBoolProtocol;
import dk.alexandra.fresco.suite.tinytables.datatypes.TinyTablesElement;
import dk.alexandra.fresco.suite.tinytables.online.TinyTablesProtocolSuite;
import dk.alexandra.fresco.suite.tinytables.online.datatypes.TinyTablesOBool;
import dk.alexandra.fresco.suite.tinytables.online.datatypes.TinyTablesSBool;
import dk.alexandra.fresco.suite.tinytables.prepro.protocols.TinyTablesPreproCloseProtocol;

/**
 * <p>
 * This class represents a close protocol in the online phase of the TinyTables
 * protocol.
 * </p>
 * <p>
 * Here one of the players is the inputter and knows the unmasked input value
 * <i>b</i>. During the preprocessing phase (see
 * {@link TinyTablesPreproCloseProtocol}, the inputter picked a random mask
 * <i>r</i>. Now, he sends the masked value <i>e = b + r</i> to the other
 * player.
 * </p>
 * 
 * @author Jonas Lindstrøm (jonas.lindstrom@alexandra.dk)
 *
 */
public class TinyTablesCloseProtocol extends TinyTablesProtocol implements CloseBoolProtocol {

	private int inputter;
	private TinyTablesOBool in;
	private TinyTablesSBool out;

	public TinyTablesCloseProtocol(int id, int inputter, OBool in, SBool out) {
		this.id = id;
		this.inputter = inputter;
		this.in = (TinyTablesOBool) in;
		this.out = (TinyTablesSBool) out;
	}

	@Override
	public Value[] getInputValues() {
		return new Value[] { in };
	}

	@Override
	public Value[] getOutputValues() {
		return new Value[] { out };
	}

	@Override
	public EvaluationStatus evaluate(int round, ResourcePool resourcePool, SCENetwork network) {
		TinyTablesProtocolSuite ps = TinyTablesProtocolSuite.getInstance(resourcePool.getMyId());
		switch (round) {
			case 0:
				if (resourcePool.getMyId() == this.inputter) {
					TinyTablesElement r = ps.getStorage().getMaskShare(id);
					TinyTablesElement e = new TinyTablesElement(this.in.getValue() ^ r.getShare());
					out.setValue(e);
					network.sendToAll(e);
				}
				network.expectInputFromPlayer(this.inputter);
				return EvaluationStatus.HAS_MORE_ROUNDS;

			case 1:
				TinyTablesElement share = network.receive(this.inputter);
				out.setValue(share);
				return EvaluationStatus.IS_DONE;

			default:
				throw new MPCException("Cannot evaluate rounds larger than 1");
		}
	}

}
