package dk.alexandra.fresco.suite.crt.datatypes.resource;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.NumericResourcePool;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.suite.crt.CRTNumericContext;
import dk.alexandra.fresco.suite.crt.datatypes.CRTSInt;
import dk.alexandra.fresco.suite.crt.protocols.framework.CRTComputation;

import java.util.List;

public abstract class NoiseGenerator<ResourcePoolL extends NumericResourcePool, ResourcePoolR extends NumericResourcePool>
        extends CRTComputation<List<CRTSInt>, ResourcePoolL, ResourcePoolR> {

  @Override
  abstract public DRes<List<CRTSInt>> buildComputation(ProtocolBuilderNumeric builder,
                                              CRTNumericContext<ResourcePoolL, ResourcePoolR> context);
}
