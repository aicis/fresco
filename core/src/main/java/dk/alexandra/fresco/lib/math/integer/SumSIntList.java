package dk.alexandra.fresco.lib.math.integer;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import java.util.ArrayList;
import java.util.List;

/**
 * ComputationBuilder for summing a list of SInts
 */
public class SumSIntList implements Computation<SInt, ProtocolBuilderNumeric> {

  private final List<DRes<SInt>> input;

  /**
   * Creates a new SumSIntList.
   *
   * @param list the list to sum
   */
  public SumSIntList(List<DRes<SInt>> list) {
    input = list;
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric iterationBuilder) {
    return iterationBuilder.seq(seq ->
        () -> input
    ).whileLoop(
        (inputs) -> inputs.size() > 1,
        (seq, inputs) -> seq.par(parallel -> {
          List<DRes<SInt>> out = new ArrayList<>();
          Numeric numericBuilder = parallel.numeric();
          DRes<SInt> left = null;
          for (DRes<SInt> input1 : inputs) {
            if (left == null) {
              left = input1;
            } else {
              out.add(numericBuilder.add(left, input1));
              left = null;
            }
          }
          if (left != null) {
            out.add(left);
          }
          return () -> out;
        })
    ).seq((builder, currentInput) -> {
      return currentInput.get(0);
    });
  }
}
