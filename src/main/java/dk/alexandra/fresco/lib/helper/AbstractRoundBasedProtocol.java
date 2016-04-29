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
package dk.alexandra.fresco.lib.helper;

import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.Protocol;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.Value;

/**
 * A skeleton for a generic round based circuit. I.e., a circuit that consisting
 * of a number of sub-circuits that must be evaluated one after an other. This
 * can be a little more lightweight than using the SequentialGateProducer as
 * makes it easier to write a circuit where the sub-circuits are generated only
 * after the previous sub-circuits have been evaluated.
 * 
 * @author psn
 * 
 */
public abstract class AbstractRoundBasedProtocol implements Protocol {

	private boolean done = false;
	private ProtocolProducer gp = null;
	private Value[] inputs = null;
	private Value[] outputs = null;

	@Override
	public int getNextProtocols(NativeProtocol[] gates, int pos) {
		if (gp == null || !gp.hasNextProtocols()) {
			gp = nextProtocolProducer();
			if (gp == null) {
				done = true;
				return pos;
			}
		}
		if (gp.hasNextProtocols()) {
			pos = gp.getNextProtocols(gates, pos);
		}
		return pos;
	}

	/**
	 * Gives the sub-circuit for the next round. All gates of the preceding
	 * round are assumed to be evaluated before the next round is evaluated.
	 * 
	 * @return a GateProducer for the next round.
	 */
	public abstract ProtocolProducer nextProtocolProducer();

	@Override
	public boolean hasNextProtocols() {
		return !done;
	}

	@Override
	public Value[] getInputValues() {
		return inputs;
	}

	@Override
	public Value[] getOutputValues() {
		return outputs;
	}

	/**
	 * Sets the input values of this circuit.
	 * 
	 * @param inputs
	 *            the input values of the circuit.
	 */
	protected void setInputValues(Value[] inputs) {
		this.inputs = inputs;
	}

	/**
	 * Sets the output values of this circuit.
	 * 
	 * @param outputs
	 *            the output values of the circuit.
	 */
	protected void setOutputValues(Value[] outputs) {
		this.outputs = outputs;
	}

}
