package dk.alexandra.fresco.demo.mimcaggregation;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.NumericBuilder;
import dk.alexandra.fresco.framework.builder.ProtocolBuilderNumeric.SequentialProtocolBuilder;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class OutputStep implements
    Application<List<List<BigInteger>>, SequentialProtocolBuilder> {

  private List<List<SInt>> secretSharedRows;

  public OutputStep(List<List<SInt>> secretSharedRows) {
    this.secretSharedRows = secretSharedRows;
  }

  @Override
  public Computation<List<List<BigInteger>>> prepareApplication(
      SequentialProtocolBuilder producer) {
    return producer.par(par -> {
      NumericBuilder numeric = par.numeric();
      List<List<Computation<BigInteger>>> computations =
          mapMatrixLists(numeric::open, secretSharedRows);
      return () -> computations;
    }).seq((computations, seq) ->
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
