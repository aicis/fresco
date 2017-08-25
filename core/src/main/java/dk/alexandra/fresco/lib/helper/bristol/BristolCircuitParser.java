/*******************************************************************************
 * Copyright (c) 2015, 2016 FRESCO (http://github.com/aicis/fresco).
 *
 * This file is part of the FRESCO project.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * FRESCO uses SCAPI - http://crypto.biu.ac.il/SCAPI, Crypto++, Miracl, NTL, and Bouncy Castle.
 * Please see these projects for any further licensing issues.
 *******************************************************************************/
package dk.alexandra.fresco.lib.helper.bristol;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary;
import dk.alexandra.fresco.framework.value.SBool;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Parses textual circuit representation.
 *
 * The circuit is expected to be in "Bristol" format, see
 * https://www.cs.bris.ac.uk/Research/CryptographySecurity/MPC/ for a specification of this.
 *
 * Reading is done in a streamed fashion.
 */
public class BristolCircuitParser implements
    dk.alexandra.fresco.framework.builder.ComputationBuilder<List<SBool>, ProtocolBuilderBinary> {

  private Stream<String> lines;
  private Iterator<String> linesIter;

  // TODO: It is not memory efficient to keep all wires in a map like this.
  // Given that this circuit is fixed, it should, somehow, be possible to
  // garbage collect intermediate results early, if they are not used further.
  private Map<Integer, Computation<SBool>> wires;

  private List<Computation<SBool>> in1;
  private List<Computation<SBool>> in2;

  // Some meta data.
  private int no_gates;
  private int no_wires;
  private int no_input1;
  private int no_input2;
  private int no_output;

  String dangling = null;

  public BristolCircuitParser(Stream<String> lines, List<Computation<SBool>> in1,
      List<Computation<SBool>> in2) {
    this.in1 = in1;
    this.in2 = in2;

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

    this.wires = new HashMap<Integer, Computation<SBool>>(no_wires);
    initWires();
  }

  private void initWires() {
    for (int i = 0; i < this.no_input1; i++) {
      this.wires.put(i, this.in1.get(i));
    }
    for (int i = 0; i < this.no_input2; i++) {
      this.wires.put(i + this.no_input1, this.in2.get(i));
    }
    for (int i = 0; i < this.no_output; i++) {
      this.wires.put(this.no_wires - this.no_output + i, null);
    }

  }

  public void close() {
    this.lines.close();
  }


  /**
   * Convert one line of text file to the corresponding basic boolean gate.
   *
   * Returns null if any input of circuit is not currently present in wires map.
   */
  private void parseLine(String line, ProtocolBuilderBinary builder) throws IOException {
    // System.out.println("Parsing line: \"" + line + "\"");
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

    // System.out.println("TYPE: " + type);
    // System.out.println("IN: " + Arrays.toString(in));
    // System.out.println("OUT: " + Arrays.toString(out));

    switch (type) {
      case "XOR":
        if (in.length != 2 || out.length != 1) {
          throw new IOException("Wrong circuit format for XOR");
        }
        Computation<SBool> leftInWireXor = wires.get(in[0]);
        Computation<SBool> rightInWireXor = wires.get(in[1]);
        Computation<SBool> outWireXor = wires.get(out[0]);

        // If some input wire is not ready we have reached a gate that depends on
        // output that is not yet ready, aka first gate of next batch.
        if (leftInWireXor == null) {
          throw new MPCException("xor: LEFT input wire " + in[0] + " was null");
        }
        if (rightInWireXor == null) {
          throw new MPCException("xor: RIGHT input wire " + in[1] + " was null");
        }

        // if (!leftInWireXor.isReady()) {
        // System.out.println("XOR: LEFT in wire " + in[0] + " is not ready");
        // return null;
        // }
        // if (!rightInWireXor.isReady()) {
        // System.out.println("XOR: RIGHT in wire " + in[0] + " is not ready");
        // return null;
        // }
        outWireXor = builder.binary().xor(leftInWireXor, rightInWireXor);
        this.wires.put(out[0], outWireXor);
        return;
      case "AND":
        if (in.length != 2 || out.length != 1) {
          throw new IOException("Wrong circuit format for AND");
        }
        Computation<SBool> leftInWireAnd = wires.get(in[0]);
        Computation<SBool> rightInWireAnd = wires.get(in[1]);
        Computation<SBool> outWireAnd = wires.get(out[0]);

        if (leftInWireAnd == null) {
          throw new MPCException("and LEFT input " + in[0] + " was not set");
        }
        if (rightInWireAnd == null) {
          throw new MPCException("and RIGHT input " + in[1] + " was not set");
        }

        // if (!leftInWireAnd.isReady()) {
        // System.out.println("and LEFT input " + in[0] + " was not ready");
        // return null;
        // }
        // if (!rightInWireAnd.isReady()) {
        // System.out.println("and RIGHT input " + in[1] + " was not ready");
        // return null;
        // }
        outWireAnd = builder.binary().and(leftInWireAnd, rightInWireAnd);
        this.wires.put(out[0], outWireAnd);
        return;
      case "INV":
        if (in.length != 1 || out.length != 1) {
          throw new IOException("Wrong circuit format for INV");
        }
        Computation<SBool> inWireNot = wires.get(in[0]);
        Computation<SBool> outWireNot = wires.get(out[0]);

        if (inWireNot == null) {
          throw new MPCException("NOT input " + in[0] + " was not set");
        }

        outWireNot = builder.binary().not(inWireNot);
        this.wires.put(out[0], outWireNot);

        return;
      default:
        throw new MPCException("Unknown gate type: " + type);
    }
  }

  private static final class IterationState implements Computation<IterationState> {

    private final Iterator<String> it;

    private IterationState(Iterator<String> it) {
      this.it = it;
    }

    @Override
    public IterationState out() {
      return this;
    }
  }

  /**
   * Fills res with next protocols, starting from pos. Returns next empty pos of array.
   */
  public Computation<List<SBool>> build(ProtocolBuilderBinary builder) {

    return builder.seq(seq -> {
      return new IterationState(this.linesIter);
    }).whileLoop((state) -> state.it.hasNext(), (state, seq) -> {
      String line = state.it.next();
      if (line.equals("")) {
        // empty line
        return state;
      }
      try {
        parseLine(line, seq);
      } catch (IOException e) {
        throw new MPCException("Could not parse the line '" + line + "'", e);
      }
      return state;
    }).seq((state, seq) -> {
      List<SBool> output = new ArrayList<>();
      for (int i = 0; i < this.no_output; i++) {
        output.add(this.wires.get(this.no_wires - this.no_output + i).out());
      }
      return () -> output;
    });
  }

  /**
   * @return Total no of gates in circuit.
   */
  public int getNoOfGates() {
    return this.no_gates;
  }

  public int getNoOfWires() {
    return this.no_wires;
  }

  public static BristolCircuitParser readCircuitDescription(String path,
      List<Computation<SBool>> in1, List<Computation<SBool>> in2) {
    ClassLoader classLoader = BristolCircuitParser.class.getClassLoader();
    InputStream is = classLoader.getResourceAsStream(path);
    if (is == null) {
      throw new MPCException("Couldn't find bristol circuit descritpion at " + path);
    }
    Stream<String> stream = new BufferedReader(new InputStreamReader(is)).lines();
    return new BristolCircuitParser(stream, in1, in2);
  }

}
