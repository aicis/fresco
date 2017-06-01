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
import dk.alexandra.fresco.lib.field.bool.XorProtocol;
import dk.alexandra.fresco.suite.tinytables.online.datatypes.TinyTablesSBool;
import dk.alexandra.fresco.suite.tinytables.prepro.protocols.TinyTablesPreproXORProtocol;

/**
 * <p>
 * This class represents an XOR protocol in the online phase of the TinyTables
 * protocol.
 * </p>
 * 
 * <p>
 * During preprocessing (see {@link TinyTablesPreproXORProtocol}), each of the
 * players let their additive share of the mask of the output wire,
 * <i>r<sub>O</sub></i>, be equal to the sum of their shares of the input masks
 * <i>r<sub>u</sub></i> and <i>r<sub>v</sub></i>, so <i>r<sub>O</sub> =
 * r<sub>u</sub> + r<sub>v</sub></i>.
 * </p>
 * 
 * <p>
 * Now, in the online phase, each player knows the masked input values
 * <i>e<sub>u</sub> = b<sub>u</sub> + r<sub>u</sub></i> and <i>e<sub>v</sub> =
 * b<sub>v</sub> + r<sub>v</sub></i>, and let the output value be equal to
 * </p>
 * <p>
 * <i>e<sub>u</sub> + e<sub>v</sub> = b<sub>u</sub> + r<sub>u</sub> +
 * b<sub>v</sub> + r<sub>v</sub> = b<sub>u</sub> + b<sub>v</sub> +
 * r<sub>O</sub></i>
 * </p>
 * <p>
 * as desired.
 * </p>
 * 
 * @author Jonas Lindstrøm (jonas.lindstrom@alexandra.dk)
 *
 */
public class TinyTablesXORProtocol extends TinyTablesProtocol implements XorProtocol {

	private TinyTablesSBool inLeft, inRight, out;

	public TinyTablesXORProtocol(int id, TinyTablesSBool inLeft, TinyTablesSBool inRight,
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
		if (round == 0) {
			// Free XOR
			this.out.setValue(inLeft.getValue().add(inRight.getValue()));
			return EvaluationStatus.IS_DONE;
		} else {
			throw new MPCException("Cannot evaluate XOR in round > 0");
		}
	}

}
