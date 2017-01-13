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
package dk.alexandra.fresco.suite.tinytables.prepro.protocols;

import java.util.List;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.network.SCENetwork;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.Value;
import dk.alexandra.fresco.lib.field.bool.AndProtocol;
import dk.alexandra.fresco.suite.tinytables.prepro.TinyTablesPreproProtocolSuite;
import dk.alexandra.fresco.suite.tinytables.prepro.datatypes.TinyTablesPreproSBool;
import dk.alexandra.fresco.suite.tinytables.prepro.datatypes.TinyTablesTriple;
import dk.alexandra.fresco.suite.tinytables.storage.TinyTable;

/**
 * <p>
 * This class represents an AND protocol in the preprocessing phase of the
 * TinyTables protocol.
 * </p>
 * 
 * <p>
 * Here, each of the two players picks random shares for the mask of the output
 * wire, <i>r<sub>O</sub></i>. Each player also has to calculate a so called
 * <i>TinyTable</i> for this protocol, which are 2x2 matrices such that the
 * <i>(c,d)</i>'th entries from the two tables is an additive secret sharing of
 * <i>(r<sub>u</sub> + c)(r<sub>v</sub> + d) + r<sub>o</sub></i>.
 * <p>
 * 
 * @author Jonas Lindstrøm (jonas.lindstrom@alexandra.dk)
 *
 */
public class TinyTablesPreproANDProtocol extends TinyTablesPreproProtocol implements AndProtocol {

	private TinyTablesPreproSBool inLeft, inRight, out;
	private TinyTablesTriple triple;

	public TinyTablesPreproANDProtocol(int id, TinyTablesPreproSBool inLeft,
			TinyTablesPreproSBool inRight, TinyTablesPreproSBool out) {
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

		TinyTablesPreproProtocolSuite ps = TinyTablesPreproProtocolSuite.getInstance(resourcePool
				.getMyId());

		switch (round) {
			case 0:

				/*
				 * Take the next multiplication (Beaver) triple from storage.
				 * This is a secret sharing of values a,b,c such that a&b = c.
				 */
				// TODO: How to make this work in parallel?
				this.triple = ps.getTinyTablesTripleProvider().getNextTriple();

				/*
				 * Calculate a share of e = right + a and d = left + b, and open
				 * it to the other player.
				 */
				boolean epsilon = inRight.getShare() ^ triple.getA();
				boolean delta = inLeft.getShare() ^ triple.getB();

				network.sendToAll(new boolean[] { epsilon, delta });
				network.expectInputFromAll();

				return EvaluationStatus.HAS_MORE_ROUNDS;

			case 1:
				List<boolean[]> allShares = network.receiveFromAll();

				/*
				 * Calculate e and d from the shares.
				 */
				boolean e = false;
				boolean d = false;
				for (boolean[] share : allShares) {
					e ^= share[0];
					d ^= share[1];
				}

				/*
				 * Calculate a share of the product of the values of the input
				 * wires.
				 */
				boolean product = triple.getC() ^ e & triple.getB() ^ d & triple.getA();
				if (resourcePool.getMyId() == 1) {
					/*
					 * The players should also share the value e&d, which is
					 * known to both players. We do this by letting player 1
					 * define his share as e&d and player 2 has share 0.
					 */
					product ^= e & d;
				}

				boolean rO = resourcePool.getSecureRandom().nextBoolean();
				out.setShare(rO);

				/*
				 * Calculate this players TinyTable. Entry i,j is a secret
				 * sharing of (left + i)(right + j) + r_O.
				 */
				boolean[] entries = new boolean[4];
				entries[0] = product ^ rO;
				entries[1] = entries[0] ^ inLeft.getShare();
				entries[2] = entries[0] ^ inRight.getShare();
				entries[3] = entries[0] ^ inLeft.getShare() ^ inRight.getShare();
				if (resourcePool.getMyId() == 1) {
					entries[3] ^= true;
				}

				ps.getStorage().storeTinyTable(id, new TinyTable(entries));
				return EvaluationStatus.IS_DONE;

			default:
				throw new MPCException("Cannot evaluate more than one round");
		}
	}
}
