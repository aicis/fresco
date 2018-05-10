package dk.alexandra.fresco.suite.spdz2k;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.Conversion;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.spdz2k.protocols.natives.Spdz2kArithmeticToBooleanProtocol;
import dk.alexandra.fresco.suite.spdz2k.protocols.natives.Spdz2kBooleanToArithmeticProtocol;
import java.util.ArrayList;
import java.util.List;

/**
 * Spdz2k optimized protocols for converting between arithmetic and boolean representations.
 */
public class Spdz2kConversion implements Conversion {

  private final ProtocolBuilderNumeric builder;

  public Spdz2kConversion(ProtocolBuilderNumeric builder) {
    this.builder = builder;
  }

  @Override
  public DRes<SInt> toBoolean(DRes<SInt> arithmeticValue) {
    return builder.append(new Spdz2kArithmeticToBooleanProtocol<>(arithmeticValue));
  }

  @Override
  public DRes<SInt> toArithmetic(DRes<SInt> booleanValue) {
    return builder.append(new Spdz2kBooleanToArithmeticProtocol<>(booleanValue));
  }

  @Override
  public DRes<List<DRes<SInt>>> toBooleanBatch(DRes<List<DRes<SInt>>> arithmeticBatch) {
    return builder.par(par -> {
      List<DRes<SInt>> inner = arithmeticBatch.out();
      List<DRes<SInt>> converted = new ArrayList<>(inner.size());
      for (DRes<SInt> anInner : inner) {
        converted.add(par.conversion().toBoolean(anInner));
      }
      return () -> converted;
    });
  }

  @Override
  public DRes<List<DRes<SInt>>> toArithmeticBatch(DRes<List<DRes<SInt>>> booleanBatch) {
    return builder.par(par -> {
      List<DRes<SInt>> inner = booleanBatch.out();
      List<DRes<SInt>> converted = new ArrayList<>(inner.size());
      for (DRes<SInt> anInner : inner) {
        converted.add(par.conversion().toArithmetic(anInner));
      }
      return () -> converted;
    });
  }

}
