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
package dk.alexandra.fresco.lib.helper.builder.tree;

import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.lib.helper.AbstractRoundBasedProtocol;
import dk.alexandra.fresco.lib.helper.ParallelProtocolProducer;

/**
 * Represents a generic tree structured circuit used to apply an operation to
 * all elements in a list.
 * 
 * An example could be a circuit computing the AND or XOR of all bits in an
 * list or array. This can be done in a generic tree fashion in log(size of
 * list) depth given simply the length of the list. 
 * 
 * The concrete operation to be done on elements is specified by a
 * TreeCircuitNodeGenerator. The TreeCircuitNodeGenerator generates a a circuit
 * corresponding to the operation on two elements given the indicies of these
 * elements in the underlying list.
 * 
 * This class does not explicitly hold the data it simply generates the
 * appropriate circuit based on correct indexing to the
 * TreeCircuitNodeGenerator. Usually the data will be held by the
 * TreeCircuitNodeGenerator.
 * 
 * 
 * @author psn
 * 
 */
public class TreeCircuit extends AbstractRoundBasedProtocol {

	private int step = 1;
	private int length;
	private TreeCircuitNodeGenerator tcn;

	/**
	 * A log depth tree circuit based on a given TreeCircuitNodeGenerator to
	 * generate the nodes.
	 * 
	 * @param tcn
	 *            a node generator specifying the operation to be done.
	 */
	public TreeCircuit(TreeCircuitNodeGenerator tcn) {
		this.length = tcn.getLength();
		this.tcn = tcn;
	}

	@Override
	public ProtocolProducer nextProtocolProducer() {
		if (step < length) {
			ParallelProtocolProducer par = new ParallelProtocolProducer();
			int i = 0;
			while (i + step < length) {
				par.append(tcn.getNode(i, i + step));
				i += 2 * step;
			}
			step *= 2;
			return par;
		} else {
			return null;
		}
	}
}
