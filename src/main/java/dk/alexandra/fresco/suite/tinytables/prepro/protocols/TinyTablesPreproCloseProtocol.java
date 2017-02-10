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

import dk.alexandra.fresco.framework.network.SCENetwork;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.OBool;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.framework.value.Value;
import dk.alexandra.fresco.lib.field.bool.CloseBoolProtocol;
import dk.alexandra.fresco.suite.tinytables.datatypes.TinyTablesElement;
import dk.alexandra.fresco.suite.tinytables.prepro.TinyTablesPreproProtocolSuite;
import dk.alexandra.fresco.suite.tinytables.prepro.datatypes.TinyTablesPreproOBool;
import dk.alexandra.fresco.suite.tinytables.prepro.datatypes.TinyTablesPreproSBool;

/**
 * <p>
 * This class represents a close protocol in the preprocessing phase of the
 * TinyTables protocol.
 * </p>
 * 
 * <p>
 * Here the one player, the inputter, knows the input value <i>b</i>, and he
 * picks a random mask <i>r</i> and sends <i>e = b + r</i> to the other player,
 * who simply assigns <code>false</code> to his share of the mask.
 * </p>
 * 
 * @author Jonas Lindstrøm (jonas.lindstrom@alexandra.dk)
 *
 */
public class TinyTablesPreproCloseProtocol extends TinyTablesPreproProtocol implements CloseBoolProtocol {

	private int inputter;
	private TinyTablesPreproOBool in;
	private TinyTablesPreproSBool out;
	
	public TinyTablesPreproCloseProtocol(int id, int inputter, OBool in, SBool out) {
		this.id = id;
		this.inputter = inputter;
		this.in = (TinyTablesPreproOBool) in;
		this.out = (TinyTablesPreproSBool) out;
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
		TinyTablesPreproProtocolSuite ps = TinyTablesPreproProtocolSuite.getInstance(resourcePool.getMyId()); 
		
		if (resourcePool.getMyId() == inputter) {
			/*
			 * The masking parameter r is additively shared among the players.
			 * If you are the inputter, you are responsible for picking a random
			 * share.
			 */
			TinyTablesElement r = new TinyTablesElement(resourcePool.getSecureRandom().nextBoolean());
			out.setValue(r);

			// We store the share for the online phase
			ps.getStorage().storeMaskShare(id, r);
			
		} else {
			/*
			 * All other players set a trivial (false) share.
			 */
			out.setValue(new TinyTablesElement(false));
		}
		return EvaluationStatus.IS_DONE;
	}

}
