package dk.alexandra.fresco.lib.compare.gt;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;
import java.util.List;

public class CarryBits implements Computation<List<DRes<SInt>>, ProtocolBuilderNumeric> {

  private final List<DRes<SInt>> valuesA;
  private final List<DRes<BigInteger>> valuesB;
  private final int k;

  public CarryBits(List<DRes<SInt>> valuesA, List<DRes<BigInteger>> valuesB) {
    this.valuesA = valuesA;
    this.valuesB = valuesB;
    this.k = valuesA.size();
  }

  @Override
  public DRes<List<DRes<SInt>>> buildComputation(ProtocolBuilderNumeric builder) {
    return null;
  }

}
