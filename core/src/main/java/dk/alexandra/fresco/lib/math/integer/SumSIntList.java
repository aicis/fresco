package dk.alexandra.fresco.lib.math.integer;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.DelayedComputation;
import dk.alexandra.fresco.framework.builder.NumericBuilder;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder.ParallelProtocolBuilder;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder.SequentialProtocolBuilder;
import dk.alexandra.fresco.framework.value.SInt;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Protocol producer for summing a list of SInts
 *
 * @param <SIntT> the type of SInts to add - and later output
 */
public class SumSIntList<SIntT extends SInt>
    implements
    BiFunction<List<Computation<SIntT>>, SequentialProtocolBuilder<SIntT>, Computation<SIntT>> {

  private final DelayedComputation<SIntT> result = new DelayedComputation<>();

  /**
   * Creates a new SumSIntList.
   *
   * @param input the input to sum
   */
  public SumSIntList() {
  }

  private void doIteration(SequentialProtocolBuilder<SIntT> iterationBuilder,
      Computation<List<Computation<SIntT>>> inputList) {
    List<Computation<SIntT>> currentInput = inputList.out();
    if (currentInput.size() > 1) {
      Computation<List<Computation<SIntT>>> iteration
          = iterationBuilder.createParallelSubFactoryReturning(new Iteration<>(currentInput));
      iterationBuilder.createSequentialSubFactory((builder) -> doIteration(builder, iteration));
    } else {
      result.setComputation(currentInput.get(0));
    }
  }

  @Override
  public Computation<SIntT> apply(
      List<Computation<SIntT>> input,
      SequentialProtocolBuilder<SIntT> iterationBuilder) {
    doIteration(iterationBuilder, () -> input);
    return result;
  }

  private static class Iteration<SIntT extends SInt> implements
      Function<ParallelProtocolBuilder<SIntT>, Computation<List<Computation<SIntT>>>> {

    private final List<Computation<SIntT>> input;

    Iteration(List<Computation<SIntT>> input) {
      this.input = input;
    }

    @Override
    public Computation<List<Computation<SIntT>>> apply(ParallelProtocolBuilder<SIntT> parallel) {
      List<Computation<SIntT>> out = new ArrayList<>();
      NumericBuilder<SIntT> numericBuilder = parallel.createNumericBuilder();
      Computation<SIntT> left = null;
      for (Computation<SIntT> input : input) {
        if (left == null) {
          left = input;
        } else {
          out.add(numericBuilder.add(left, input));
          left = null;
        }
      }
      if (left != null) {
        out.add(left);
      }
      return () -> out;
    }
  }
}
