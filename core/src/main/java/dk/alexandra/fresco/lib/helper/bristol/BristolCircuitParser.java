package dk.alexandra.fresco.lib.helper.bristol;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary;
import dk.alexandra.fresco.framework.value.SBool;
import java.io.BufferedReader;
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
    dk.alexandra.fresco.framework.builder.Computation<List<SBool>, ProtocolBuilderBinary> {

  private Stream<String> lines;
  private Iterator<String> linesIter;

  // TODO: It is not memory efficient to keep all wires in a map like this.
  // Given that this circuit is fixed, it should, somehow, be possible to
  // garbage collect intermediate results early, if they are not used further.
  private Map<Integer, DRes<SBool>> wires;

  private List<DRes<SBool>> in1;
  private List<DRes<SBool>> in2;

  // Some meta data.
  private int no_wires;
  private int no_input1;
  private int no_input2;
  private int no_output;

  String dangling = null;

  public BristolCircuitParser(Stream<String> lines, List<DRes<SBool>> in1,
      List<DRes<SBool>> in2) {
    this.in1 = in1;
    this.in2 = in2;

    this.lines = lines;
    this.linesIter = lines.iterator();

    // Read first line; this is meta data.
    String[] meta = linesIter.next().split(" \\s*");
    this.no_wires = Integer.parseInt(meta[1]);
    meta = linesIter.next().split(" \\s*");
    this.no_input1 = Integer.parseInt(meta[0]);
    this.no_input2 = Integer.parseInt(meta[1]);
    this.no_output = Integer.parseInt(meta[2]);
    linesIter.next(); // 3rd line is always empty line.

    this.wires = new HashMap<>(no_wires);
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


  /**
   * Convert one line of text file to the corresponding basic boolean gate.
   *
   * Returns null if any input of circuit is not currently present in wires map.
   */
  private void parseLine(String line, ProtocolBuilderBinary builder) {
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

    if ("XOR".equals(type)) {
      if (in.length != 2 || out.length != 1) {
        throw new IllegalArgumentException("Wrong circuit format for XOR");
      }
      DRes<SBool> leftInWireXor = wires.get(in[0]);
      DRes<SBool> rightInWireXor = wires.get(in[1]);
      DRes<SBool> outWireXor = wires.get(out[0]);

      // If some input wire is not ready we have reached a gate that depends on
      // output that is not yet ready, aka first gate of next batch.
      if (leftInWireXor == null) {
        throw new IllegalArgumentException("xor: LEFT input wire " + in[0] + " was null");
      }
      if (rightInWireXor == null) {
        throw new IllegalArgumentException("xor: RIGHT input wire " + in[1] + " was null");
      }

      outWireXor = builder.binary().xor(leftInWireXor, rightInWireXor);
      this.wires.put(out[0], outWireXor);

    } else if ("AND".equals(type)) {
      if (in.length != 2 || out.length != 1) {
        throw new IllegalArgumentException("Wrong circuit format for AND");
      }
      DRes<SBool> leftInWireAnd = wires.get(in[0]);
      DRes<SBool> rightInWireAnd = wires.get(in[1]);
      DRes<SBool> outWireAnd = wires.get(out[0]);

      if (leftInWireAnd == null) {
        throw new IllegalArgumentException("and LEFT input " + in[0] + " was not set");
      }
      if (rightInWireAnd == null) {
        throw new IllegalArgumentException("and RIGHT input " + in[1] + " was not set");
      }

      outWireAnd = builder.binary().and(leftInWireAnd, rightInWireAnd);
      this.wires.put(out[0], outWireAnd);
    } else if ("INV".equals(type)) {
      if (in.length != 1 || out.length != 1) {
        throw new IllegalArgumentException("Wrong circuit format for INV");
      }
      DRes<SBool> inWireNot = wires.get(in[0]);
      DRes<SBool> outWireNot = wires.get(out[0]);
      if (inWireNot == null) {
        throw new IllegalArgumentException("NOT input " + in[0] + " was not set");
      }
      outWireNot = builder.binary().not(inWireNot);
      this.wires.put(out[0], outWireNot);
    } else {
      throw new IllegalArgumentException("Unknown gate type: " + type);
    }
  }

  private static final class IterationState implements DRes<IterationState> {

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
  public DRes<List<SBool>> buildComputation(ProtocolBuilderBinary builder) {

    return builder.seq(seq ->
        new IterationState(this.linesIter)
    ).whileLoop((state) -> state.it.hasNext(), (seq, state) -> {
      String line = state.it.next();
      if (line.equals("")) {
        // empty line
        return state;
      }
      parseLine(line, seq);
      return state;
    }).seq((seq, state) -> {
      List<SBool> output = new ArrayList<>();
      for (int i = 0; i < this.no_output; i++) {
        output.add(this.wires.get(this.no_wires - this.no_output + i).out());
      }
      this.lines.close();
      return () -> output;
    });
  }

  public static BristolCircuitParser readCircuitDescription(String path,
      List<DRes<SBool>> in1, List<DRes<SBool>> in2) {
    ClassLoader classLoader = BristolCircuitParser.class.getClassLoader();
    InputStream is = classLoader.getResourceAsStream(path);
    if (is == null) {
      throw new IllegalArgumentException("Couldn't find bristol circuit descritpion at " + path);
    }
    Stream<String> stream = new BufferedReader(new InputStreamReader(is)).lines();
    return new BristolCircuitParser(stream, in1, in2);
  }

}
