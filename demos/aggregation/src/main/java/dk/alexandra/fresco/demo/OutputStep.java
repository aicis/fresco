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

public class OutputStep implements
    Application<List<List<BigInteger>>, ProtocolBuilderNumeric> {

  private List<List<SInt>> secretSharedRows;

  public OutputStep(List<List<SInt>> secretSharedRows) {
    this.secretSharedRows = secretSharedRows;
  }

  @Override
  public DRes<List<List<BigInteger>>> buildComputation(
      ProtocolBuilderNumeric producer) {
    return producer.par(par -> {
      Numeric numeric = par.numeric();
      List<List<DRes<BigInteger>>> computations =
          mapMatrixLists(numeric::open, secretSharedRows);
      return () -> computations;
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
