package dk.alexandra.fresco.demo;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
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
  public DRes<List<List<SInt>>> buildComputation(ProtocolBuilderNumeric producer) {
    return producer.par(par -> {
      Numeric numeric = par.numeric();
      Function<BigInteger, DRes<SInt>> known = value -> numeric.input(value, pid);
      List<List<DRes<SInt>>> collect = mapMatrixLists(known, inputRows);
      return () -> collect;
    }).seq((seq, computations) ->
        () -> mapMatrixLists(DRes::out, computations)
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
