package dk.alexandra.fresco.lib.math.integer;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.ComputationBuilder;
import dk.alexandra.fresco.framework.builder.numeric.NumericBuilder;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import java.util.ArrayList;
import java.util.List;

/**
 * ComputationBuilder for multiplying a list of SInts
 */
public class ProductSIntList implements ComputationBuilder<SInt, ProtocolBuilderNumeric> {

  private final List<Computation<SInt>> input;

  /**
   * Creates a new ProductSIntList.
   *
   * @param list the list to sum
   */
  public ProductSIntList(List<Computation<SInt>> list) {
    input = list;
  }

  @Override
  public Computation<SInt> build(ProtocolBuilderNumeric iterationBuilder) {
    return iterationBuilder.seq(seq ->
        () -> input
    ).whileLoop(
        (inputs) -> inputs.size() > 1,
        (inputs, seq) -> seq.createParallelSub(parallel -> {
          List<Computation<SInt>> out = new ArrayList<>();
          NumericBuilder numericBuilder = parallel.numeric();
          Computation<SInt> left = null;
          for (Computation<SInt> input1 : inputs) {
            if (left == null) {
              left = input1;
            } else {
              out.add(numericBuilder.mult(left, input1));
              left = null;
            }
          }
          if (left != null) {
            out.add(left);
          }
          return () -> out;
        })
    ).seq((currentInput, builder) -> currentInput.get(0));
  }
}
