package dk.alexandra.fresco.lib.common.collections.io;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.ComputationParallel;
import dk.alexandra.fresco.lib.common.collections.Collections;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.collections.DefaultCollections;
import dk.alexandra.fresco.lib.common.util.RowPairD;
import java.math.BigInteger;

public class OpenRowPair
    implements ComputationParallel<RowPairD<BigInteger, BigInteger>, ProtocolBuilderNumeric> {

  private final DRes<RowPairD<SInt, SInt>> closedPair;

  public OpenRowPair(DRes<RowPairD<SInt, SInt>> closedPair) {
    super();
    this.closedPair = closedPair;
  }

  @Override
  public DRes<RowPairD<BigInteger, BigInteger>> buildComputation(ProtocolBuilderNumeric builder) {
    RowPairD<SInt, SInt> closedPairOut = closedPair.out();
    Collections collections = new DefaultCollections(builder);
    RowPairD<BigInteger, BigInteger> openPair =
        new RowPairD<>(collections.openList(closedPairOut.getFirst()),
            collections.openList(closedPairOut.getSecond()));
    return () -> openPair;
  }

}
