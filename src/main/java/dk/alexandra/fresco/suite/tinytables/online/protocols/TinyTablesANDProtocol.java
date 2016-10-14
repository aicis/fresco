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
import dk.alexandra.fresco.suite.tinytables.online.TinyTablesProtocolSuite;
import dk.alexandra.fresco.suite.tinytables.online.datatypes.TinyTablesSBool;

/**
 * <p>
 * This class represents an AND protocol in the TinyTables protocol's online
 * phase.
 * </p>
 * <p>
 * Here it is assumed that each of the two players have computed a TinyTable for
 * the protocol. Player 1 has picked random values for his:
 * </p>
 * <table>
 * <tr>
 * <td><i>t<sub>00</sub></i></td>
 * <td><i>t<sub>01</sub></i></td>
 * </tr>
 * <tr>
 * <td><i>t<sub>10</sub></i></td>
 * <td><i>t<sub>11</sub></i></td>
 * </tr>
 * </table>
 * <p>
 * and player 2 has computer a TinyTable which looks like this
 * </p>
 * <table>
 * <tr>
 * <td><i>t<sub>00</sub>+r<sub>O</sub>+r<sub>u</sub>r<sub>v</sub></i></td>
 * <td><i>t<sub>01</sub>+r<sub>O</sub>+r<sub>u</sub>(r<sub>v</sub>+1)</i></td>
 * </tr>
 * <tr>
 * <td><i>t<sub>10</sub>+r<sub>O</sub>+(r<sub>u</sub>+1)r<sub>v</sub></i></td>
 * <td><i>t<sub>11</sub>+r<sub>O</sub>+(r<sub>u</sub>+1)(r<sub>v</sub>+1)</i></td>
 * </tr>
 * </table>
 * <p>
 * Now, both players know the encrypted inputs of the input wires wires
 * <i>e<sub>u</sub> = b<sub>u</sub>+r<sub>u</sub></i> and <i>e<sub>v</sub> =
 * b<sub>v</sub>+r<sub>v</sub></i> where <i>b<sub>u</sub></i> and
 * <i>b<sub>v</sub></i> are the clear text bits, and each now looks up entry
 * <i>(e<sub>u</sub>, e<sub>v</sub>)</i> in his TinyTable and shares this with
 * the other player. Both players now add their share with the other players
 * share to get
 * </p>
 * <p>
 * <i>t<sub>e<sub>u</sub>e<sub>v</sub></sub> +
 * t<sub>e<sub>u</sub>e<sub>v</sub></sub> + r<sub>O</sub> +
 * (r<sub>u</sub>+e<sub>u</sub>)(r<sub>v</sub>+e<sub>v</sub>) = r<sub>O</sub> +
 * b<sub>u</sub>b<sub>v</sub> = e<sub>O</sub></i>,
 * </p>
 * <p>
 * which is the masked value of the output wire.
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
				boolean myShare = ps.getStorage().lookupTinyTable(id, inLeft.getValue(),
						inRight.getValue());

				network.expectInputFromAll();
				network.sendToAll(myShare);
				return EvaluationStatus.HAS_MORE_ROUNDS;
			case 1:
				List<Boolean> shares = network.receiveFromAll();
				boolean res = false;
				for (boolean share : shares) {
					res = res ^ share;
				}
				this.out.setValue(res);
				return EvaluationStatus.IS_DONE;
			default:
				throw new MPCException("Cannot evaluate rounds larger than 0");
		}
	}

}
