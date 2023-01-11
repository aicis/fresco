package dk.alexandra.fresco.suite.crt.datatypes.resource;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.NumericResourcePool;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.builder.numeric.field.FieldDefinition;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.suite.crt.datatypes.CRTSInt;

import java.util.ArrayDeque;

public abstract class CRTDataSupplier<L extends NumericResourcePool, R extends NumericResourcePool,
        T extends CRTNoiseGenerator<L,R>> {

  private final ArrayDeque<CRTSInt> noisePairs = new ArrayDeque<>();
  private final T noiseGenerator;
  private final CRTResourcePool<L, R> resourcePool;


  protected CRTDataSupplier(T noiseGenerator, CRTResourcePool<L,R> resourcePool) {
    this.noiseGenerator = noiseGenerator;
    this.resourcePool = resourcePool;
  }

  /**
   * Supplies the next correlated noise
   *
   * @return r
   */
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

  /**
   * Supply the next random bit
   *
   * @return b
   */
  public CRTSInt getRandomBit() {
    return null;
  }

  public Pair<FieldDefinition, FieldDefinition> getFieldDefinitions() {
    return new Pair<>(resourcePool.getSubResourcePools().getFirst().getFieldDefinition(),
            resourcePool.getSubResourcePools().getSecond().getFieldDefinition()
    );
  }

}
