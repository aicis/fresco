package dk.alexandra.fresco.lib.math.integer;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.ComputationBuilder;
import dk.alexandra.fresco.framework.builder.NumericBuilder;
import dk.alexandra.fresco.framework.builder.ProtocolBuilderNumeric.SequentialProtocolBuilder;
import dk.alexandra.fresco.framework.value.SInt;
import java.util.ArrayList;
import java.util.List;

/**
 * Protocol producer for summing a list of SInts
 */
public class SumSIntList implements ComputationBuilder<SInt> {

  private final List<Computation<SInt>> input;

  /**
   * Creates a new SumSIntList.
   *
   * @param list the list to sum
   */
  public SumSIntList(
      List<Computation<SInt>> list) {
    input = list;
  }

  @Override
  public Computation<SInt> build(
      SequentialProtocolBuilder iterationBuilder) {
    return iterationBuilder.seq(seq ->
        () -> input
    ).whileLoop(
        computations -> computations.size() > 1,
        (inputs, seq) -> seq.createParallelSub(parallel -> {
          List<Computation<SInt>> out = new ArrayList<>();
          NumericBuilder numericBuilder = parallel.numeric();
          Computation<SInt> left = null;
          for (Computation<SInt> input1 : inputs) {
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
    ).seq((currentInput, builder) ->
        currentInput.get(0)
    );
  }
}
