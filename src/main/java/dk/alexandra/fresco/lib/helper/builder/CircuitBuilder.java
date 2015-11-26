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
package dk.alexandra.fresco.lib.helper.builder;

import dk.alexandra.fresco.framework.ProtocolProducer;

/**
 * CircuitBuilders provide a more natural interface than working with raw circuits
 * when writing secure computation applications. The goal is to have a more "code-like" 
 * way of specifying the circuit. 
 * 
 * To build a circuit the application programmer 
 * calls methods on a CircuitBuilder corresponding to the operations the final circuit 
 * should apply to given SInts. Behind the scenes the CircuitBuilder constructs the corresponding
 * circuit and returns the corresponding GateProducer when needed. 
 * 
 * I.e. CircuitBuilders hides away the raw circuit and (to some extend) its structure, and lets
 * the application programmer work solely with Values.
 * 
 * The CircuitBuilder interface provides general methods for controlling the structure of
 * the circuit, i.e. to specify whether the computation can be done in parallel or must be 
 * done sequentially. To switch between parallel and sequential circuit building one sets declares 
 * a "scope" for a sequence of instructions.
 * 
 * TODO: Eventually CircuitBuilders could maybe also be used to analyze and optimize the circuits
 * that they have been building.  
 * 
 * @author psn
 *
 */
public interface CircuitBuilder {

	/**
	 * Starts a parallel scope. I.e. Specifies that the following instructions 
	 * given to this CircuitBuilder can be done in parallel.
	 * Note: One should be careful and remember to close scopes
	 */
	public abstract void beginParScope();

	/**
	 * Starts a sequential scope. I.e. Specifies that the following instructions 
	 * given to this CircuitBuilder must not be done in sequence.
	 * Note: One should be careful and remember to close scopes
	 */
	public abstract void beginSeqScope();

	/**
	 * Ends the current scope returning to the previous scope.
	 * Note: One should be careful and remember to close scopes and not close to many scopes
	 */
	public abstract void endCurScope();
	
	/**
	 * Adds a GateProducer to the circuit being build. This to allow adding circuits that cannot be created with 
	 * this CircuitBuilder.
	 * @param gp a GateProducer  
	 */
	public abstract void addGateProducer(ProtocolProducer gp);

	/**
	 * Gets the GateProducer corresponding the entire circuit build by this CircuitBuilder.
	 * @return a GateProducer corresponding to the circuit build.
	 */
	public abstract ProtocolProducer getCircuit();

	/**
	 * Resets the current builder - meaning this can be treated as a new instance again.
	 */
	public abstract void reset();
}