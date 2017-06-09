/*******************************************************************************
 * Copyright (c) 2015, 2016 FRESCO (http://github.com/aicis/fresco).
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
 * ProtocolBuilders provide a more natural interface than working with raw Protocols
 * when writing secure computation applications. The goal is to have a more "code-like" 
 * way of specifying the NativeProtocol.
 *
 * To build a NativeProtocol the application programmer
 * calls methods on a ProtocolBuilder corresponding to the operations the final NativeProtocol
 * should apply to given SInts. Behind the scenes the ProtocolBuilder constructs the corresponding
 * NativeProtocol and returns the corresponding ProtocolProducer when needed.
 *
 * I.e. ProtocolBuilders hides away the raw NativeProtocol and (to some extend) its structure, and lets
 * the application programmer work solely with Values.
 * 
 * The ProtocolBuilder interface provides general methods for controlling the structure of
 * the NativeProtocol, i.e. to specify whether the computation can be done in parallel or must be
 * done sequentially. To switch between parallel and sequential NativeProtocol building one sets declares
 * a "scope" for a sequence of instructions.
 * 
 * TODO: Eventually ProtocolBuilders could maybe also be used to analyze and optimize the Protocols
 * that they have been building.  
 * 
 * @author psn
 *
 */
public interface ProtocolBuilder {

	/**
	 * Starts a parallel scope. I.e. Specifies that the following instructions 
	 * given to this ProtocolBuilder can be done in parallel.
	 * Note: One should be careful and remember to close scopes
	 */
	void beginParScope();

	/**
	 * Starts a sequential scope. I.e. Specifies that the following instructions 
	 * given to this ProtocolBuilder must not be done in sequence.
	 * Note: One should be careful and remember to close scopes
	 */
	void beginSeqScope();

	/**
	 * Ends the current scope returning to the previous scope.
	 * Note: One should be careful and remember to close scopes and not close to many scopes
	 */
	void endCurScope();
	
	/**
   * Adds a ProtocolProducer to the NativeProtocol being build. This to allow adding Protocols that cannot be created with
   * this ProtocolBuilder.
   * @param pp a ProtocolProducer
	 */
	void addProtocolProducer(ProtocolProducer pp);

	/**
   * Gets the ProtocolProducer corresponding the entire NativeProtocol build by this ProtocolBuilder.
   * @return a ProtocolProducer corresponding to the NativeProtocol build.
   */
  ProtocolProducer getProtocol();

	/**
	 * Resets the current builder - meaning this can be treated as a new instance again.
	 */
	void reset();
}