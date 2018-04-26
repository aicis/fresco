package dk.alexandra.fresco.lib.collections.io;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.ComputationParallel;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

public class OpenList<T extends DRes<SInt>>
    implements ComputationParallel<List<DRes<BigInteger>>, ProtocolBuilderNumeric> {

  private final DRes<List<T>> closedList;

  public OpenList(DRes<List<T>> closedList) {
    super();
    this.closedList = closedList;
  }

  @Override
  public DRes<List<DRes<BigInteger>>> buildComputation(ProtocolBuilderNumeric builder) {
    Numeric nb = builder.numeric();
    // for each input value, call input
    List<DRes<BigInteger>> openList =
        closedList.out().stream().map(nb::open).collect(Collectors.toList());
    return () -> openList;
  }

}
