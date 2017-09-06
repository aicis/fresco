package dk.alexandra.fresco.demo;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.numeric.NumericBuilder;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class InputStep implements
    Application<List<List<SInt>>, ProtocolBuilderNumeric> {

  private final List<List<BigInteger>> inputRows;
  private final int pid;

  public InputStep(List<List<BigInteger>> inputRows, int pid) {
    super();
    this.inputRows = inputRows;
    this.pid = pid;
  }

  @Override
  public Computation<List<List<SInt>>> buildComputation(ProtocolBuilderNumeric producer) {
    return producer.par(par -> {
      NumericBuilder numeric = par.numeric();
      Function<BigInteger, Computation<SInt>> known = value -> numeric.input(value, pid);
      List<List<Computation<SInt>>> collect = mapMatrixLists(known, inputRows);
      return () -> collect;
    }).seq((seq, computations) ->
        () -> mapMatrixLists(Computation::out, computations)
    );
  }

  private <R, T> List<List<R>> mapMatrixLists(
      Function<T, R> mapper,
      List<List<T>> inputRows) {
    return inputRows.stream().map(
        row -> row.stream().map(mapper).collect(Collectors.toList())
    ).collect(Collectors.toList());
  }
}
