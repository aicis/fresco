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

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.network.SCENetwork;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.Value;
import dk.alexandra.fresco.lib.field.bool.AndProtocol;
import dk.alexandra.fresco.suite.tinytables.datatypes.TinyTable;
import dk.alexandra.fresco.suite.tinytables.datatypes.TinyTablesElement;
import dk.alexandra.fresco.suite.tinytables.prepro.TinyTablesPreproProtocolSuite;
import dk.alexandra.fresco.suite.tinytables.prepro.datatypes.TinyTablesPreproSBool;

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

	public TinyTablesPreproANDProtocol(int id, TinyTablesPreproSBool inLeft,
			TinyTablesPreproSBool inRight, TinyTablesPreproSBool out) {
		super();
		this.id = id;
		this.inLeft = inLeft;
		this.inRight = inRight;
		this.out = out;		
	}

	public TinyTablesPreproSBool getInLeft() {
		return inLeft;
	}

	public TinyTablesPreproSBool getInRight() {
		return inRight;
	}

	public TinyTablesPreproSBool getOut() {
		return out;
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
				 * Here we only pick the mask of the output wire. The TinyTable
				 * is calculated after all AND gates has been preprocessed.
				 */
				boolean rO = resourcePool.getSecureRandom().nextBoolean();
				out.setValue(new TinyTablesElement(rO));

				/*
				 * We need to finish the processing of this gate after all
				 * preprocessing is done (see calculateTinyTable). To do this,
				 * we keep a reference to all AND gates.
				 */
				ps.addANDGate(this);

				return EvaluationStatus.IS_DONE;

			default:
				throw new MPCException("Cannot evaluate more than one round");
		}
	}

	/**
	 * Calculate the TinyTable for this gate.
	 * 
	 * @param playerId
	 *            The ID of this player.
	 * @param product
	 *            A share of the product of input values for this gate.
	 * @return
	 */
	public TinyTable calculateTinyTable(int playerId, TinyTablesElement product) {
		
		TinyTablesElement[] entries = new TinyTablesElement[4];
		entries[0] = product.add(this.out.getValue());
		entries[1] = entries[0].add(inLeft.getValue());
		entries[2] = entries[0].add(inRight.getValue());
		entries[3] = entries[0].add(inLeft.getValue()).add(inRight.getValue()).not(playerId);
		return new TinyTable(entries);
	}
	
}
