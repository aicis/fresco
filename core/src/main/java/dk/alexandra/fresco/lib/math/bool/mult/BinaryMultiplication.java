package dk.alexandra.fresco.lib.math.bool.mult;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary;
import dk.alexandra.fresco.framework.value.SBool;
import java.util.ArrayList;
import java.util.List;

/**
 * This class implements a Binary Multiplication protocol by doing the school method.
 * This means that we connect O(n^2) 1-Bit-FullAdders in order to get the result.
 * As one would imagine, this is not the most efficient method, but it works as a basic case.
 *
 */
public class BinaryMultiplication implements
    Computation<List<DRes<SBool>>, ProtocolBuilderBinary> {

  private List<DRes<SBool>> lefts;
  private List<DRes<SBool>> rights;

  public BinaryMultiplication(List<DRes<SBool>> lefts,
      List<DRes<SBool>> rights) {
    this.lefts = lefts;
    this.rights = rights;
  }

  @Override
  public DRes<List<DRes<SBool>>> buildComputation(ProtocolBuilderBinary builder) {
    return builder.seq(seq -> {
      int idx = this.lefts.size() - 1;
      List<DRes<SBool>> res = new ArrayList<>();
      for (DRes<SBool> right : rights) {
        res.add(seq.binary().and(lefts.get(idx), right));
      }
      IterationState is = new IterationState(idx, () -> res);
      return is;
    }).whileLoop(
        (state) -> state.round >= 1,
        (seq, state) -> {
          int idx = state.round - 1;

          List<DRes<SBool>> res = new ArrayList<>();
          for (DRes<SBool> right : rights) {
            res.add(seq.binary().and(lefts.get(idx), right));
          }
          for (int i = state.round; i < this.lefts.size(); i++) {
            res.add(seq.binary().known(false));
          }
          List<DRes<SBool>> tmp = state.value.out();
          while (tmp.size() < res.size()) {
            tmp.add(0, seq.binary().known(false));
          }
          IterationState is = new IterationState(idx,
              seq.advancedBinary().fullAdder(state.value.out(), res, seq.binary().known(false)));
          return is;
        }
    ).seq((seq, state) -> state.value
    );
  }

  private static final class IterationState implements DRes<IterationState> {

    private int round;
    private final DRes<List<DRes<SBool>>> value;

    private IterationState(int round,
        DRes<List<DRes<SBool>>> value) {
      this.round = round;
      this.value = value;
    }

    @Override
    public IterationState out() {
      return this;
    }
  }

}
