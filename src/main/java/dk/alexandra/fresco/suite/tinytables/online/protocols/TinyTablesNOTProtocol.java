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
import dk.alexandra.fresco.framework.value.Value;
import dk.alexandra.fresco.lib.field.bool.NotProtocol;
import dk.alexandra.fresco.suite.tinytables.online.datatypes.TinyTablesSBool;
import dk.alexandra.fresco.suite.tinytables.prepro.protocols.TinyTablesPreproNOTProtocol;

/**
 * <p>
 * This class represents a NOT protocol in the online phase of the TinyTables
 * protocol.
 * </p>
 * <p>
 * Here both players know the masked value of the input wire, <i>e<sub>u</sub> =
 * b<sub>u</sub> + r<sub>u</sub></i> where <i>b<sub>u</sub></i> is the unmasked
 * value and <i>r<sub>u</sub></i> is the mask. During the preprocessing phase
 * (see {@link TinyTablesPreproNOTProtocol}, both players have let their share
 * of the mask of the output wire be equal to their share of the input wire, so
 * <i>r<sub>O</sub> = r<sub>u</sub></i>. Now, both players set value of the
 * output wire to be <i>e<sub>O</sub> = !e<sub>u</sub> = !b<sub>u</sub> +
 * r<sub>O</sub></i>.</p>
 * 
 * @author Jonas Lindstrøm (jonas.lindstrom@alexandra.dk)
 *
 */
public class TinyTablesNOTProtocol extends TinyTablesProtocol implements NotProtocol{

	private TinyTablesSBool in, out;
	
	public TinyTablesNOTProtocol(int id, TinyTablesSBool in, TinyTablesSBool out) {
		this.id = id;
		this.in = in;
		this.out = out;
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
		if (round == 0) {
			this.out.setValue(in.getValue().flip());
			return EvaluationStatus.IS_DONE;
		} else {
			throw new MPCException("Cannot evaluate NOT in round > 0");
		}
	}

}
