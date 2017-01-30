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
import dk.alexandra.fresco.lib.field.bool.AndProtocol;
import dk.alexandra.fresco.suite.tinytables.datatypes.TinyTable;
import dk.alexandra.fresco.suite.tinytables.datatypes.TinyTablesElement;
import dk.alexandra.fresco.suite.tinytables.online.TinyTablesProtocolSuite;
import dk.alexandra.fresco.suite.tinytables.online.datatypes.TinyTablesSBool;

/**
 * <p>
 * This class represents an AND protocol in the TinyTables protocol's online
 * phase.
 * </p>
 * <p>
 * Here it is assumed that each of the two players have computed a TinyTable for
 * the protocol such that the <i>(c,d)</i>'th entries from the two tables is an
 * additive secret sharing of <i>(r<sub>u</sub> + c)(r<sub>v</sub> + d) +
 * r<sub>o</sub></i>.
 * <p>
 * Now, both players know the encrypted inputs of the input wires wires
 * <i>e<sub>u</sub> = b<sub>u</sub>+r<sub>u</sub></i> and <i>e<sub>v</sub> =
 * b<sub>v</sub>+r<sub>v</sub></i> where <i>b<sub>u</sub></i> and
 * <i>b<sub>v</sub></i> are the clear text bits, and each now looks up entry
 * <i>(e<sub>u</sub>, e<sub>v</sub>)</i> in his TinyTable and shares this with
 * the other player. Both players now add their share with the other players
 * share to get the masked value of the output wire.
 * </p>
 * 
 * @author Jonas Lindstrøm (jonas.lindstrom@alexandra.dk)
 */
public class TinyTablesANDProtocol extends TinyTablesProtocol implements AndProtocol {

	private int id;
	private TinyTablesSBool inLeft, inRight, out;

	public TinyTablesANDProtocol(int id, TinyTablesSBool inLeft, TinyTablesSBool inRight,
			TinyTablesSBool out) {
		super();
		this.id = id;
		this.inLeft = inLeft;
		this.inRight = inRight;
		this.out = out;
	}

	@Override
	public Value[] getInputValues() {
		return new Value[] { inLeft, inRight };
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
				TinyTable tinyTable = ps.getStorage().getTinyTable(id);
				if (tinyTable == null) {
					throw new MPCException("Unable to find TinyTable for gate with id " + id);
				}
				TinyTablesElement myShare = tinyTable.getValue(inLeft.getValue(), inRight.getValue());

				network.expectInputFromAll();
				network.sendToAll(myShare);
				return EvaluationStatus.HAS_MORE_ROUNDS;
			case 1:
				List<TinyTablesElement> shares = network.receiveFromAll();
				boolean open = TinyTablesElement.open(shares);
				this.out.setValue(new TinyTablesElement(open));
				return EvaluationStatus.IS_DONE;
			default:
				throw new MPCException("Cannot evaluate rounds larger than 0");
		}
	}

}
