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
 */
public class SumSIntList implements
    BiFunction<List<Computation<SInt>>, SequentialProtocolBuilder, Computation<SInt>> {

  private final DelayedComputation<SInt> result = new DelayedComputation<>();

  /**
   * Creates a new SumSIntList.
   */
  public SumSIntList() {
  }

  private void doIteration(SequentialProtocolBuilder iterationBuilder,
      Computation<List<Computation<SInt>>> inputList) {
    List<Computation<SInt>> currentInput = inputList.out();
    if (currentInput.size() > 1) {
      Computation<List<Computation<SInt>>> iteration
          = iterationBuilder.createParallelSub(new Iteration(currentInput));
      iterationBuilder.createIteration((builder) -> doIteration(builder, iteration));
    } else {
      result.setComputation(currentInput.get(0));
    }
  }

  @Override
  public Computation<SInt> apply(
      List<Computation<SInt>> input,
      SequentialProtocolBuilder iterationBuilder) {
    doIteration(iterationBuilder, () -> input);
    return result;
  }

  private static class Iteration implements
      Function<ParallelProtocolBuilder, Computation<List<Computation<SInt>>>> {

    private final List<Computation<SInt>> input;

    Iteration(List<Computation<SInt>> input) {
      this.input = input;
    }

    @Override
    public Computation<List<Computation<SInt>>> apply(ParallelProtocolBuilder parallel) {
      List<Computation<SInt>> out = new ArrayList<>();
      NumericBuilder numericBuilder = parallel.numeric();
      Computation<SInt> left = null;
      for (Computation<SInt> input : input) {
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
