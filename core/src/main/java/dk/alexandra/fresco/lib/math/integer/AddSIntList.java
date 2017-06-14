package dk.alexandra.fresco.lib.math.integer;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.RecursiveComputation;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder.ParallelProtocolBuilder;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder.SequentialProtocolBuilder;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Protocol producer for summing a list of SInts
 *
 * @param <SIntT> the type of SInts to add - and later output
 */
public class AddSIntList<SIntT extends SInt> extends RecursiveComputation<SIntT>
    implements Consumer<SequentialProtocolBuilder<SIntT>> {

  private Computation<List<SIntT>> inputList;

  /**
   * Creates a new AddSIntList.
   *
   * @param input the input to sum
   */
  public AddSIntList(Computation<List<SIntT>> input) {
    super();
    this.inputList = input;
  }

  private AddSIntList(Computation<List<SIntT>> input, AddSIntList<SIntT> previousComputation) {
    super(previousComputation);
    this.inputList = input;
  }

  @Override
  public void accept(SequentialProtocolBuilder<SIntT> iterationBuilder) {
    List<SIntT> currentInput = inputList.out();
    if (currentInput.size() > 1) {
      Iteration<SIntT> iteration = iterationBuilder
          .createParallelSubFactory(new Iteration<>(currentInput));
      iterationBuilder.createSequentialSubFactory(new AddSIntList<>(iteration, this));
    } else {
      setResult(currentInput.get(0));
    }
  }


  private static class Iteration<SIntT extends SInt> implements
      Consumer<ParallelProtocolBuilder<SIntT>>,
      Computation<List<SIntT>> {

    private List<Computation<SIntT>> out;
    private final List<SIntT> input;

    Iteration(List<SIntT> input) {
      this.input = input;
    }

    @Override
    public void accept(ParallelProtocolBuilder<SIntT> parallel) {
      out = new ArrayList<>();
      BasicNumericFactory<SIntT> appendingFactory = parallel.createAppendingBasicNumericFactory();
      SIntT left = null;
      for (SIntT input : input) {
        if (left == null) {
          left = input;
        } else {
          out.add((Computation) appendingFactory.add(left, input));
          left = null;
        }
      }
      if (left != null) {
        SIntT finalLeft = left;
        out.add(() -> finalLeft);
      }
    }

    @Override
    public List<SIntT> out() {
      return out
          .stream()
          .map(Computation::out)
          .collect(Collectors.toList());
    }
  }
}
