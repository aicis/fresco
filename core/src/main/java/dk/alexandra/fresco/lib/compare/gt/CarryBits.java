package dk.alexandra.fresco.lib.compare.gt;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.SIntPair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.math.integer.binary.ArithmeticXorKnownRight;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class CarryBits implements Computation<List<DRes<SInt>>, ProtocolBuilderNumeric> {

  private final DRes<List<DRes<SInt>>> bitsA;
  private final DRes<List<DRes<BigInteger>>> bitsB;

  public CarryBits(DRes<List<DRes<SInt>>> bitsA, DRes<List<DRes<BigInteger>>> bitsB) {
    this.bitsA = bitsA;
    this.bitsB = bitsB;
  }

  @Override
  public DRes<List<DRes<SInt>>> buildComputation(ProtocolBuilderNumeric builder) {
    // these could also be done in parallel
    DRes<List<DRes<SInt>>> xoredDef = builder.par(new ArithmeticXorKnownRight(bitsA, bitsB));
    DRes<List<DRes<SInt>>> andedDef = builder.par(new ArithmeticXorKnownRight(bitsA, bitsB));
    DRes<List<SIntPair>> pairs = () -> {
      List<DRes<SInt>> xored = xoredDef.out();
      List<DRes<SInt>> anded = andedDef.out();
      List<SIntPair> innerPairs = new ArrayList<>(xored.size());
      for (int i = 0; i < xored.size(); i++) {
        innerPairs.add(new SIntPair(xored.get(i), anded.get(i)));
      }
      return innerPairs;
    };
    return null;
  }

}
