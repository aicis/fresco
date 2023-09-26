package dk.alexandra.fresco.suite.crt.datatypes.resource;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.NumericResourcePool;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.builder.numeric.field.FieldDefinition;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.suite.crt.datatypes.CRTCombinedPad;
import dk.alexandra.fresco.suite.crt.datatypes.CRTSInt;

import java.util.ArrayDeque;

public abstract class CRTDataSupplier<L extends NumericResourcePool, R extends NumericResourcePool> {
  public static final int DEFAULT_BATCH_SIZE = 8;
  public static final int DEFAULT_DETERRENCE_FACTOR = 2;
  public static final int DEFAULT_STATSECURITY = 60;

  private final ArrayDeque<CRTCombinedPad> noisePairs = new ArrayDeque<>();
  private final NoiseGenerator<L, R> noiseGenerator;


  protected CRTDataSupplier(NoiseGenerator<L, R> noiseGenerator) {
    this.noiseGenerator = noiseGenerator;
  }

  /**
   * Supplies the next correlated noise
   *
   * @return r
   */
  public DRes<CRTCombinedPad> getCorrelatedNoise(ProtocolBuilderNumeric builder) {
    if (noisePairs.isEmpty()) {
      return builder.seq(noiseGenerator).seq((seq, noise) -> {
        noisePairs.addAll(noise);
        CRTCombinedPad out = noisePairs.pop();
        return DRes.of(out);
      });
    } else {
      return DRes.of(noisePairs.pop());
    }
  }
  /**
   * Supply the next random bit
   * todo
   * @return b
   */
  public CRTSInt getRandomBit() {
    throw new IllegalArgumentException("Not implemented yet");
  }

  public Pair<FieldDefinition, FieldDefinition> getFieldDefinitions() {
    throw new IllegalArgumentException("Not implemented yet");
  }
}
