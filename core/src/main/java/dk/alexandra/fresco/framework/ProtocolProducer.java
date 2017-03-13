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
package dk.alexandra.fresco.framework;


/**
 * Invariants:
 * 
 * INVARIANT1: Once a call to getNextprotocolss() does not return any new protocolss or,
 * equivalently, hasMoreprotocolss() returns false, these methods will continue to
 * return no new protocolss, or false.
 * 
 * INVARIANT2: All protocolss returned in a slice must only depend on protocolss in
 * previous slices.
 * 
 * 
 * TODO: Should INVARIANT2 be replaced by another invariant, stating that all
 * protocolss must only depend on protocolss before itselv, possibly in the same slice?
 * Some protocolsevaluator-protocol suite constellations may benefit from this??
 * 
 * 
 */
public interface ProtocolProducer {

	/**
	 * Attempt to fill the given protocols array with ready protocolss.
	 * 
	 * If no protocolss are ready, the result of the method equals the given n.
	 * 
	 * 
	 * TODO: Does no next protocolss mean that evaluation has finished, or just that
	 * evaluator should try again later?
	 * 
	 * 
	 * @param protocols
	 *            Array to be filled with ready protocolss.
	 * @param pos
	 *            Pointer to the first free slot in the array.
	 * @return Pointer to the next free slot in the array.
	 * 
	 */
	int getNextProtocols(NativeProtocol[] protocols, int pos);

	/**
	 * Returns true if there is at least one protocols left in the protocol that has
	 * not already been evaluated.
	 * 
	 */
	boolean hasNextProtocols();
}
