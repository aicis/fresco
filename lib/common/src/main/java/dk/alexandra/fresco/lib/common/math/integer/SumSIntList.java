package dk.alexandra.fresco.lib.common.math.integer;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Builder for a {@link Computation}s that sum lists of {@link SInts} secret-shared values.
 *
 * <p>Empty lists are allowed, and will always produce a value of {@code 0}.
 */
public class SumSIntList implements Computation<SInt, ProtocolBuilderNumeric> {

  private final List<DRes<SInt>> input;

  /**
   * Creates a new {@link SumSIntList}.
   *
   * @param list the list to sum. Not nullable.
   */
  public SumSIntList(List<DRes<SInt>> list) {
    input = Objects.requireNonNull(list);
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric iterationBuilder) {
    // Fast case if there is nothing to sum.
    if (input.isEmpty()) {
      return iterationBuilder.seq(seq -> seq.numeric().known(0));
    }

    // Slow case when there are elements to sum.
    return iterationBuilder
        .seq(seq -> () -> input)
        .whileLoop(
            (inputs) -> inputs.size() > 1,
            (seq, inputs) ->
                seq.par(
                    parallel -> {
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
                    }))
        .seq((builder, currentInput) -> currentInput.get(0));
  }
}
