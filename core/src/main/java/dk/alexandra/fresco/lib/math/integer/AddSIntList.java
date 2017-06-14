package dk.alexandra.fresco.lib.math.integer;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder.SequentialProtocolBuilder;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Protocol producer for summing a list of SInts
 *
 * @param <SIntT> the type of SInts to add - and later output
 */
public class AddSIntList<SIntT extends SInt>
    implements Consumer<SequentialProtocolBuilder<SIntT>>, Computation<SIntT> {

  private List<Computation<SIntT>> currentInputList;
  private ResultSInt<SIntT> resultSInt;

  /**
   * Creates a new AddSIntList.
   *
   * @param input the input to sum
   */
  public AddSIntList(List<Computation<SIntT>> input) {
    this(input, new ResultSInt<>());
  }

  private AddSIntList(List<Computation<SIntT>> input, ResultSInt<SIntT> resultSInt) {
    this.currentInputList = input;
    this.resultSInt = resultSInt;
  }

  @Override
  public void accept(SequentialProtocolBuilder<SIntT> iterationBuilder) {
    if (currentInputList.size() > 1) {
      List<Computation<SIntT>> out = new ArrayList<>();
      iterationBuilder.createParallelSubFactory(parallel -> doIterationInParallel(parallel, out));
      iterationBuilder.createSequentialSubFactory(new AddSIntList<>(out, resultSInt));
    } else {
      resultSInt.sint = currentInputList.get(0).out();
    }
  }

  private void doIterationInParallel(ProtocolBuilder<SIntT> parallel,
      List<Computation<SIntT>> out) {
    BasicNumericFactory<SIntT> appendingFactory = parallel.createAppendingBasicNumericFactory();
    Computation<SIntT> left = null;
    for (Computation<SIntT> input : currentInputList) {
      if (left == null) {
        left = input;
      } else {
        out.add((Computation) appendingFactory.add(left.out(), input.out()));
        left = null;
      }
    }
    if (left != null) {
      out.add(left);
    }
    currentInputList = out;
  }

  @Override
  public SIntT out() {
    return resultSInt.sint;
  }

  private static class ResultSInt<SIntT extends SInt> {

    private SIntT sint;
  }
}
