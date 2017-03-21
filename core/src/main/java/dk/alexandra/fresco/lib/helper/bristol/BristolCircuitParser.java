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
package dk.alexandra.fresco.lib.helper.bristol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.Protocol;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.lib.field.bool.BasicLogicFactory;

/**
 * Parses textual circuit representation.
 * 
 * The circuit is expected to be in "Bristol" format, see
 * https://www.cs.bris.ac.uk/Research/CryptographySecurity/MPC/ for a
 * specification of this.
 * 
 * Reading is done in a streamed fashion.
 * 
 */
public class BristolCircuitParser {

	private Stream<String> lines;
	private Iterator<String> linesIter;
	private BasicLogicFactory boolFactory;

	// TODO: It is not memory efficient to keep all wires in a map like this.
	// Given that this circuit is fixed, it should, somehow, be possible to
	// garbage collect intermediate results early, if they are not used further.
	private Map<Integer, SBool> wires;

	private SBool[] in1;
	private SBool[] in2;
	private SBool[] out;

	// Some meta data.
	private int no_gates;
	private int no_wires;
	private int no_input1;
	private int no_input2;
	private int no_output;

	String dangling = null;

	public BristolCircuitParser(Stream<String> lines, BasicLogicFactory boolFactory, SBool[] in1, SBool[] in2, SBool[] out) {
		this.in1 = in1;
		this.in2 = in2;
		this.out = out;
		this.boolFactory = boolFactory;

		this.lines = lines;
		this.linesIter = lines.iterator();

		// Read first line; this is meta data.
		String[] meta = linesIter.next().split(" \\s*");
		this.no_gates = Integer.parseInt(meta[0]);
		this.no_wires = Integer.parseInt(meta[1]);
		meta = linesIter.next().split(" \\s*");
		// System.out.println("Read: " + Arrays.toString(meta));
		this.no_input1 = Integer.parseInt(meta[0]);
		this.no_input2 = Integer.parseInt(meta[1]);
		this.no_output = Integer.parseInt(meta[2]);
		linesIter.next(); // 3rd line is always empty line.

		this.wires = new HashMap<Integer, SBool>(no_wires);
		initWires();
	}

	private void initWires() {
		for (int i = 0; i < this.no_input1; i++) {
			this.wires.put(i, this.in1[i]);
		}
		for (int i = 0; i < this.no_input2; i++) {
			this.wires.put(i + this.no_input1, this.in2[i]);
		}
		for (int i = 0; i < this.no_output; i++) {
			this.wires.put(this.no_wires - this.no_output + i, this.out[i]);
		}
		
	}

	public void close() {
		this.lines.close();
	}

	
	/**
	 * Convert one line of text file to the corresponding basic boolean gate.
	 * 
	 * Returns null if any input of circuit is not currently present in wires
	 * map.
	 * 
	 */
	private Protocol parseLine(String line) throws IOException {
		//System.out.println("Parsing line: \"" + line + "\"");
		String[] tokens = line.split(" \\s*");
		int no_input = Integer.parseInt(tokens[0]);
		int no_output = Integer.parseInt(tokens[1]);
		int[] in = new int[no_input];
		int[] out = new int[no_output];
		for (int i = 0; i < no_input; i++) {
			in[i] = Integer.parseInt(tokens[2 + i]);
		}
		for (int i = 0; i < no_output; i++) {
			out[i] = Integer.parseInt(tokens[2 + no_input + i]);
		}
		String type = tokens[2 + no_input + no_output];
		
		//System.out.println("TYPE: " + type);
		//System.out.println("IN: " + Arrays.toString(in));
		//System.out.println("OUT: " + Arrays.toString(out));
		//System.out.println();
		
		// TODO: Currently, we use isReady() to determine when to stop batch.
		// This ONLY works if the invariant is that getNextProtocols() is only
		// called when ALL previous gates are done. I.e. it does not allow 
		// to call getNextProtocols() twice at once. Is this the invariant
		// that we want? It probably means that the Bristol circuits will not
		// work with an asynchronous evaluator strategy that calls getNextProtocols()
		// many times before evaluating any gates.
		

		switch (type) {
		case "XOR":
			if (in.length != 2 || out.length != 1)
				throw new IOException("Wrong circuit format for XOR");
			SBool leftInWireXor = wires.get(in[0]);
			SBool rightInWireXor = wires.get(in[1]);
			SBool outWireXor = wires.get(out[0]);

			// If some input wire is not ready we have reached a gate that depends on
			// output that is not yet ready, aka first gate of next batch.
			if (leftInWireXor == null ) {
				throw new MPCException("xor: LEFT input wire " + in[0] + " was null");
			}
			if (rightInWireXor == null ) {
				throw new MPCException("xor: RIGHT input wire " + in[1] + " was null");
			}
			
			if (!leftInWireXor.isReady()) {
				//System.out.println("XOR: LEFT in wire " + in[0] + " is not ready");
				return null;
			}
			if (!rightInWireXor.isReady()) {
				//System.out.println("XOR: RIGHT in wire " + in[0] + " is not ready");
				return null;
			}

			if (outWireXor == null) {
				// A new intermediate wire.
				outWireXor = this.boolFactory.getSBool();
				this.wires.put(out[0], outWireXor);
			}

			
			return this.boolFactory.getXorProtocol(leftInWireXor, rightInWireXor, outWireXor);
		case "AND":
			if (in.length != 2 || out.length != 1)
				throw new IOException("Wrong circuit format for AND");
			SBool leftInWireAnd = wires.get(in[0]);
			SBool rightInWireAnd = wires.get(in[1]);
			SBool outWireAnd = wires.get(out[0]);
			
			if (leftInWireAnd == null ) {
				throw new MPCException("and LEFT input " + in[0] + " was not set");
			}
			if (rightInWireAnd == null ) {
				throw new MPCException("and RIGHT input " + in[1] + " was not set");
			}
			
			if (!leftInWireAnd.isReady()) {
				//System.out.println("and LEFT input " + in[0] + " was not ready");
				return null;
			}
			if (!rightInWireAnd.isReady()) {
				//System.out.println("and RIGHT input " + in[1] + " was not ready");
				return null;
			}
			
			
			if (outWireAnd == null) {
				// A new intermediate wire.
				outWireAnd = this.boolFactory.getSBool();
				this.wires.put(out[0], outWireAnd);
			}
			
			
			return this.boolFactory.getAndProtocol(leftInWireAnd, rightInWireAnd, outWireAnd);
		case "INV":
			if (in.length != 1 || out.length != 1)
				throw new IOException("Wrong circuit format for INV");
			SBool inWireNot = wires.get(in[0]);
			SBool outWireNot = wires.get(out[0]);

			if (inWireNot == null) {
				throw new MPCException("NOT input " + in[0] + " was not set");
			}
			
			if (!inWireNot.isReady()) {
				//System.out.println("NOT input " + in[0] + " was not ready");
				return null;
			}

			if (outWireNot == null) {
				// A new intermediate wire.
				outWireNot = this.boolFactory.getSBool();
				this.wires.put(out[0], outWireNot);
			}
			
			return this.boolFactory.getNotProtocol(inWireNot, outWireNot);
		default:
			throw new MPCException("Unknown gate type: " + type);
		}
	}

	/**
	 * Fills res with next protocols, starting from pos. Returns next empty pos
	 * of array.
	 * 
	 */
	public int getNext(Protocol[] res, int pos) {
		
		// Start with the dangling line from previous call, if its there.
		if (this.dangling != null && pos < res.length) {
			//System.out.println("Processing dangling gate");
			try {
				Protocol c = parseLine(this.dangling);
				if (c == null) {
					//System.out.println("CircuitParser returned 0 (still dangling..) ");
					return pos; // Dangling still not ready.
				} else {
					res[pos] = c;
					pos++;
					this.dangling = null;
				}
			} catch (IOException e) {
				throw new MPCException("Error while reading circuit", e);
			}
		}
		while (pos < res.length) {
			try {
				String line = this.linesIter.next();
				if (line.startsWith("*") || line.matches("\\s*")) {
					// Lines starting with "*" are meta data stating that a new
					// layer is starting here. We don't use that information,
					// and just skip it. Lines with only whitespace is also skipped.
					continue;
				}
				Protocol c = parseLine(line);
				if (c == null) {
					//System.out.println("We have reached a gate of next layer");
					// We have reached a gate of next layer.
					this.dangling = line;
					//System.out.println("CircuitParser reached end of batch at " + pos + ", line: " + line);
					break;
				} else {
					res[pos] = c;
					pos++;
				}
			} catch (NoSuchElementException e) { // End of circuit reached.
				//System.out.println("CircuitParser reached end of file ");
				this.close();
				break;
			} catch (IOException e) {
				throw new MPCException("Error while reading circuit", e);
			}
		}
		return pos;
	}

	/**
	 * @return Total no of gates in circuit.
	 * 
	 */
	public int getNoOfGates() {
		return this.no_gates;
	}

	public int getNoOfWires() {
		return this.no_wires;
	}
	
	public static BristolCircuitParser readCircuitDescription(BasicLogicFactory blf, String path, SBool[] in1, SBool[] in2, SBool[] out) {
		ClassLoader classLoader = BristolCircuitParser.class.getClassLoader();
		InputStream is = classLoader.getResourceAsStream(path);
		if (is == null)
			throw new MPCException("Couldn't find bristol circuit descritpion at " + path);
		Stream<String> stream = new BufferedReader(new InputStreamReader(is)).lines();
		return new BristolCircuitParser(stream, blf, in1, in2, out);
	}
	

}
