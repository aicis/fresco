package dk.alexandra.fresco.lib.math.integer;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.RecursiveComputation;
import dk.alexandra.fresco.framework.builder.NumericBuilder;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder.ParallelProtocolBuilder;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder.SequentialProtocolBuilder;
import dk.alexandra.fresco.framework.value.SInt;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Protocol producer for summing a list of SInts
 *
 * @param <SIntT> the type of SInts to add - and later output
 */
public class AddSIntList<SIntT extends SInt> extends RecursiveComputation<SIntT>
    implements Consumer<SequentialProtocolBuilder<SIntT>> {

  private Computation<List<Computation<SIntT>>> inputList;

  /**
   * Creates a new AddSIntList.
   *
   * @param input the input to sum
   */
  public AddSIntList(List<Computation<SIntT>> input) {
    super();
    this.inputList = () -> input;
  }

  private AddSIntList(Computation<List<Computation<SIntT>>> input,
      AddSIntList<SIntT> previousComputation) {
    super(previousComputation);
    this.inputList = input;
  }

  @Override
  public void accept(SequentialProtocolBuilder<SIntT> iterationBuilder) {
    List<Computation<SIntT>> currentInput = inputList.out();
    if (currentInput.size() > 1) {
      Iteration<SIntT> iteration = iterationBuilder
          .createParallelSubFactory(new Iteration<>(currentInput));
      iterationBuilder.createSequentialSubFactory(new AddSIntList<>(iteration, this));
    } else {
      setResult(currentInput.get(0).out());
    }
  }


  private static class Iteration<SIntT extends SInt> implements
      Consumer<ParallelProtocolBuilder<SIntT>>,
      Computation<List<Computation<SIntT>>> {

    private List<Computation<SIntT>> out;
    private final List<Computation<SIntT>> input;

    Iteration(List<Computation<SIntT>> input) {
      this.input = input;
    }

    @Override
    public void accept(ParallelProtocolBuilder<SIntT> parallel) {
      out = new ArrayList<>();
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
    }

    @Override
    public List<Computation<SIntT>> out() {
      return out;
    }
  }
}
