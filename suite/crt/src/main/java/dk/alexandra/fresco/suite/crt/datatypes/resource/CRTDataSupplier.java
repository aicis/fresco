package dk.alexandra.fresco.suite.crt.datatypes.resource;

import dk.alexandra.fresco.framework.builder.numeric.field.FieldDefinition;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.suite.crt.datatypes.CRTSInt;

public interface CRTDataSupplier {

  /**
   * Supplies the next correlated noise
   *
   * @return r
   */
  CRTSInt getCorrelatedNoise();

  /**
   * Supply the next random bit
   *
   * @return b
   */
  CRTSInt getRandomBit();

  Pair<FieldDefinition, FieldDefinition> getFieldDefinitions();
}
