package dk.alexandra.fresco.lib.compare.bool;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary;
import dk.alexandra.fresco.framework.value.SBool;
import java.util.List;

/**
 * Represents a comparison protocol between two bitstrings. Concretely, the protocol computes the
 * 'greater than' relation of strings A and B, i.e., it computes C := A > B.
 * 
 */
public class BinaryGreaterThan
    implements dk.alexandra.fresco.framework.builder.Computation<SBool, ProtocolBuilderBinary> {

  private List<DRes<SBool>> inA;
  private List<DRes<SBool>> inB;
  private int length;

  /**
   * Construct a protocol to compare strings A and B. The bitstrings A and B are assumed to be even
   * length and to be ordered from most- to least significant bit.
   * 
   * @param inA input string A
   * @param inB input string B
   */
  public BinaryGreaterThan(List<DRes<SBool>> inA, List<DRes<SBool>> inB) {
    if (inA.size() == inB.size()) {
      this.inA = inA;
      this.inB = inB;
      this.length = inA.size();
    } else {
      throw new IllegalArgumentException("Comparison failed: bitsize differs");
    }
  }

  @Override
  public DRes<SBool> buildComputation(ProtocolBuilderBinary builder) {
    return builder.seq(seq -> {
      int round = 0;
      DRes<SBool> xor = seq.binary().xor(inA.get(length - 1), inB.get(length - 1));
      round++;
      DRes<SBool> outC = seq.binary().and(inA.get(length - 1), xor);
      round++;
      return new IterationState(round, outC);
    }).whileLoop(
        (state) -> state.round <= length, 
        (seq, state) -> {
          int i = length - state.round;
          DRes<SBool> xor = seq.binary().xor(inA.get(i), inB.get(i));
          DRes<SBool> tmp = seq.binary().xor(inA.get(i), state.value);
          tmp = seq.binary().and(tmp, xor);
          DRes<SBool> outC = seq.binary().xor(state.value, tmp);
          return new IterationState(state.round + 1, outC);
        }).seq((state, seq) -> {
          return seq.value;
        });
  }

  private static final class IterationState implements DRes<IterationState> {

    private int round;
    private final DRes<SBool> value;

    private IterationState(int round, DRes<SBool> value) {
      this.round = round;
      this.value = value;
    }

    @Override
    public IterationState out() {
      return this;
    }
  }

}
