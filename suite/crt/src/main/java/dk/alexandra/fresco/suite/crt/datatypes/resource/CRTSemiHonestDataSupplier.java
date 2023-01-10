package dk.alexandra.fresco.suite.crt.datatypes.resource;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.NumericResourcePool;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.builder.numeric.field.FieldDefinition;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.suite.crt.datatypes.CRTSInt;

import java.util.ArrayDeque;

public class CRTSemiHonestDataSupplier<ResourcePoolL extends NumericResourcePool,
    ResourcePoolR extends NumericResourcePool>
        implements CRTDataSupplier {

  private final ArrayDeque<CRTSInt> noisePairs = new ArrayDeque<>();
  private final FieldDefinition fp;
  private final FieldDefinition fq;


  public CRTSemiHonestDataSupplier(CRTResourcePool<ResourcePoolL,
      ResourcePoolR> resourcePool) {
    this.fp = resourcePool.getFieldDefinitions().getFirst();
    this.fq = resourcePool.getFieldDefinitions().getFirst();
  }

  @Override
  public DRes<CRTSInt> getCorrelatedNoise(ProtocolBuilderNumeric builder) {
    if (noisePairs.isEmpty()) {
      return builder.seq(new SemiHonestNoiseGenerator<ResourcePoolL, ResourcePoolR>(10)).seq((seq, noise) -> {
        noisePairs.addAll(noise);
        CRTSInt out = noisePairs.pop();
        return DRes.of(out);
      });
    } else {
      return DRes.of(noisePairs.pop());
    }
  }

  @Override
  public CRTSInt getRandomBit() {
    return null;
  }

  @Override
  public Pair<FieldDefinition, FieldDefinition> getFieldDefinitions() {
    return new Pair<>(fp, fq);
  }
}
