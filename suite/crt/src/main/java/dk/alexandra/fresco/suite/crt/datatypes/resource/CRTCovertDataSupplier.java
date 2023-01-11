package dk.alexandra.fresco.suite.crt.datatypes.resource;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.NumericResourcePool;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.builder.numeric.field.FieldDefinition;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.suite.crt.datatypes.CRTSInt;

import java.util.ArrayDeque;

public class CRTCovertDataSupplier<ResourcePoolL extends NumericResourcePool,
    ResourcePoolR extends NumericResourcePool>
        implements CRTDataSupplier {

  private final ArrayDeque<CRTSInt> noisePairs = new ArrayDeque<>();


  private final CovertNoiseGenerator<ResourcePoolL, ResourcePoolR> noiseGenerator;
  private final CRTResourcePool<ResourcePoolL, ResourcePoolR> resourcePool;

  public CRTCovertDataSupplier(CRTResourcePool<ResourcePoolL,
      ResourcePoolR> resourcePool) {
    this(resourcePool, 8, 2, 40);
  }


  public CRTCovertDataSupplier(CRTResourcePool<ResourcePoolL,
          ResourcePoolR> resourcePool, int batchSize, int deterrenceFactor, int securityParam) {
    this.resourcePool = resourcePool;
    this.noiseGenerator = new CovertNoiseGenerator<>(batchSize, deterrenceFactor, securityParam);
  }

  @Override
  public DRes<CRTSInt> getCorrelatedNoise(ProtocolBuilderNumeric builder) {
    if (noisePairs.isEmpty()) {
      return builder.seq(noiseGenerator).seq((seq, noise) -> {
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
    return new Pair<>(resourcePool.getSubResourcePools().getFirst().getFieldDefinition(),
            resourcePool.getSubResourcePools().getSecond().getFieldDefinition()
    );
  }
}
