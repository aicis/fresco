package dk.alexandra.fresco.suite.crt.datatypes.resource;

import dk.alexandra.fresco.framework.builder.numeric.NumericResourcePool;
import dk.alexandra.fresco.framework.builder.numeric.field.FieldDefinition;
import dk.alexandra.fresco.framework.util.Pair;

public interface CRTResourcePool<ResourcePoolA extends NumericResourcePool, ResourcePoolB extends NumericResourcePool>
    extends NumericResourcePool {

  Pair<ResourcePoolA, ResourcePoolB> getSubResourcePools();

  /**
   * The field definitions used for this instance of RNS.
   *
   * @return a pair of field definitions
   */
  Pair<FieldDefinition, FieldDefinition> getFieldDefinitions();

  CRTDataSupplier getDataSupplier();

}
