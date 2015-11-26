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
package dk.alexandra.fresco.lib.collections;

import dk.alexandra.fresco.framework.ProtocolFactory;
import dk.alexandra.fresco.framework.value.Value;

public interface LookUpCircuitFactory<T extends Value> extends ProtocolFactory {

	/**
	 * Gets a circuit that allows to look up the value of a key in a list of
	 * key/value pairs. If the key is not present in the key list the output
	 * value should be unchanged. This feature can be used to check if the key
	 * is present.
	 * 
	 * 
	 * @param lookUpKey
	 *            the key to look up a value for
	 * @param keys
	 *            the list of keys
	 * @param values
	 *            the list of values. Note the order should match that of the
	 *            keys.
	 * @param outputValue
	 *            a value to put the output value in. I.e., this will be
	 *            overwritten if the look up key is present in the key set.
	 * @return the circuit
	 */
	public LookUpProtocol<T> getLookUpCircuit(T lookUpKey, T[] keys, T[] values,
			T outputValue);
	
	/**
	 * Gets a circuit that allows to look up the array of values associated with a key in a list of
	 * key/value array pairs. If the key is not present in the key list the output
	 * values should be unchanged. This feature can be used to check if the key
	 * is present.
	 * 
	 * 
	 * @param lookUpKey
	 *            the key to look up a value for
	 * @param keys
	 *            the list of keys
	 * @param values
	 *            the list of value arrays. Note the order should match that of the
	 *            keys.
	 * @param outputValue
	 *            a value array to put the output value in. I.e., this will be
	 *            overwritten if the look up key is present in the key set.
	 * @return the circuit
	 */
	
	public LookUpProtocol<T> getLookUpCircuit(T lookUpKey, T[] keys, T[][] values,
			T[] outputValue);
}
