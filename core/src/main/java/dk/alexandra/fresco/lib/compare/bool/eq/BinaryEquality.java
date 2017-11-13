package dk.alexandra.fresco.lib.compare.bool.eq;


import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.binary.AdvancedBinary;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary;
import dk.alexandra.fresco.framework.value.SBool;
import java.util.ArrayList;
import java.util.List;

/**
 * Does a simple compare like this: out = (a1 XNOR b1) AND (a2 XNOR b2) AND (a3 XNOR b3) AND ...
 *
 * The XNORs are done in parallel and the ANDs are done by a log-depth tree structured protocol.
 *
 */
public class BinaryEquality implements Computation<SBool, ProtocolBuilderBinary> {


  private List<DRes<SBool>> inLeft;
  private List<DRes<SBool>> inRight;
  private final int length;

  public BinaryEquality(List<DRes<SBool>> inLeft,
      List<DRes<SBool>> inRight) {
    this.inLeft = inLeft;
    this.inRight = inRight;
    if (inLeft.size() != inRight.size()) {
      throw new IllegalArgumentException("Binary strings must be of equal length");
    }
    this.length = inLeft.size();
  }

  @Override
  public DRes<SBool> buildComputation(ProtocolBuilderBinary builder) {
    return builder.par(par -> {
      AdvancedBinary bb = par.advancedBinary();
      List<DRes<SBool>> xnors = new ArrayList<>();
      for (int i = 0; i < length; i++) {
        xnors.add(bb.xnor(inLeft.get(i), inRight.get(i)));
      }

      IterationState is = new IterationState(xnors.size(), () -> xnors);
      return is;
    }).whileLoop(
        (state) -> state.round > 1, 
        (seq, state) -> {
          List<DRes<SBool>> input = state.value.out();
          int size = input.size() % 2 + input.size() / 2;

          IterationState is = new IterationState(size, seq.par(par -> {
            List<DRes<SBool>> ands = new ArrayList<>();
            int idx = 0;
            while (idx < input.size() - 1) {
              ands.add(par.binary().and(input.get(idx), input.get(idx + 1)));
              idx += 2;
            }
            if (idx < input.size()) {
              ands.add(input.get(idx));
            }
            return () -> ands;
          }));
          return is;
        }).seq((seq, state) -> {
          return state.value.out().get(0);
        });
  }

  private static final class IterationState implements DRes<IterationState> {

    private int round;
    private final DRes<List<DRes<SBool>>> value;

    private IterationState(int round, DRes<List<DRes<SBool>>> value) {
      this.round = round;
      this.value = value;
    }

    @Override
    public IterationState out() {
      return this;
    }
  }

}
