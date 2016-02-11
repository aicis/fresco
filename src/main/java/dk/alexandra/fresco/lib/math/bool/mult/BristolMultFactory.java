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
package dk.alexandra.fresco.lib.math.bool.mult;

import dk.alexandra.fresco.framework.ProtocolFactory;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.lib.field.bool.BasicLogicFactory;
import dk.alexandra.fresco.lib.helper.bristol.BristolCircuit;
import dk.alexandra.fresco.lib.helper.bristol.BristolCircuitParser;

/**
 * Factory for various mulitplication operations.
 * 
 * The factory is based on a factory for AND, XOR, NOT gates. It reads a full
 * description of a boolean circuit from a static file.
 * 
 * It is based on a static circuit description produced by Nigel Smart and
 * others, see https://www.cs.bris.ac.uk/Research/CryptographySecurity/MPC/. The
 * circuit has been optimized such that it contains only a small number of
 * non-linear gates (AND gates) at the cost of more linear gates (XOR, NOT)
 * gates.
 * 
 * There is a certain amount of streaming here: The circuit description is
 * loaded into memory in a streamed manner. When getNextProtocols() is invoked,
 * new Xor and And protocols and SBools are constructed in a lazy manner.
 * However, nothing is garbage collected, so at the end of the getNextGates, the
 * full circuit is present in memory.
 * 
 * OBS: A new read of the file is done at every call to the factory. Might be
 * more efficient to load file once and generate protocols based on that.
 * 
 * IMPORTANT: The circuit here ignores padding and only allows a single-block
 * input. Each circuit implements the compression function of the respective
 * hash function, where the input chaining values are fixed to the IV values of
 * the respective hash function. The input of the circuit is a full-size message
 * block, the output consists of chaining values after invocation of the
 * compression function (formatted in the same way as the digest value would be
 * in the respective hash function). This means that non-standard test vectors
 * are used, see https://www.cs.bris.ac.uk/Research/CryptographySecurity/MPC/.
 * 
 */
public class BristolMultFactory implements ProtocolFactory {

	private BasicLogicFactory blf;

	public BristolMultFactory(BasicLogicFactory blf) {
		this.blf = blf;
	}

	
	/**
	 * A circuit for 32x32 bit multiplication.
	 * 
	 * The complete circuit consists of 5926 AND gates, 1069 XOR gates, and 5379
	 * NOT gates.
	 * 
	 * @param in1,
	 *            in2 Arrays of 32 SBools for the two inputs to multiply.
	 * @param out
	 *            Array of 64 SBools for the resulting product.
	 * @return A protocol that computes 32x32 bit multiplication.
	 * 
	 */
	public BristolCircuit getMult32x32Circuit(SBool[] in1, SBool[] in2, SBool[] out) {
		if (null == in1 || in1.length != 32)
			throw new IllegalArgumentException("'in1' must be array of 32 SBools");
		if (null == in2 || in2.length != 32)
			throw new IllegalArgumentException("'in1' must be array of 32 SBools");
		if (null == out || out.length != 64)
			throw new IllegalArgumentException("'out' must be array of 64 SBools");
		BristolCircuitParser parser = BristolCircuitParser.readCircuitDescription(this.blf, "circuits/mult_32x32.txt", in1, in2, out);
		return new BristolCircuit(parser, in1, in2, out);
	}
	

	
}
