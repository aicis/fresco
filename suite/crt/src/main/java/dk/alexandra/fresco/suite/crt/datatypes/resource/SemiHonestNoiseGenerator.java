package dk.alexandra.fresco.suite.crt.datatypes.resource;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.NumericResourcePool;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.crt.CRTNumericContext;
import dk.alexandra.fresco.suite.crt.datatypes.CRTSInt;

import java.util.ArrayList;
import java.util.List;

public class SemiHonestNoiseGenerator<ResourcePoolL extends NumericResourcePool, ResourcePoolR extends NumericResourcePool>
        extends CRTNoiseGenerator<ResourcePoolL, ResourcePoolR> {

  private final int batchSize;

  public SemiHonestNoiseGenerator(int batchSize) {
    this.batchSize = batchSize;
  }

  @Override
  public DRes<List<CRTSInt>> buildComputation(ProtocolBuilderNumeric builder,
                                              CRTNumericContext<ResourcePoolL, ResourcePoolR> context) {
    return builder.par(par -> {
      Numeric left = context.leftNumeric(par);
      List<CRTSInt> list = new ArrayList<>(batchSize);
      for (int i = 0; i < batchSize; i++) {
        DRes<SInt> r = left.randomElement();
        CRTSInt noisePair = new CRTSInt(r, r);
        list.add(noisePair);
      }
      return DRes.of(list);
    });
  }

}
