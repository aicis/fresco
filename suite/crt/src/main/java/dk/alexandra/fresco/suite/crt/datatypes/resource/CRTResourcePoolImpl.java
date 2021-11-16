package dk.alexandra.fresco.suite.crt.datatypes.resource;

import dk.alexandra.fresco.framework.builder.numeric.NumericResourcePool;
import dk.alexandra.fresco.framework.builder.numeric.field.FieldDefinition;
import dk.alexandra.fresco.framework.sce.resources.ResourcePoolImpl;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.suite.crt.CRTRingDefinition;

public class CRTResourcePoolImpl<ResourcePoolA extends NumericResourcePool, ResourcePoolB extends NumericResourcePool> extends
    ResourcePoolImpl implements
    CRTResourcePool<ResourcePoolA, ResourcePoolB> {

  private final CRTDataSupplier dataSupplier;
  private final FieldDefinition definition;
  private final Pair<ResourcePoolA, ResourcePoolB> resourcePools;

  public CRTResourcePoolImpl(int myId, int noOfPlayers, CRTDataSupplier dataSupplier,
      ResourcePoolA resourcePoolLeft, ResourcePoolB resourcePoolRight) {
    super(myId, noOfPlayers);
    this.dataSupplier = dataSupplier;
    this.definition = new CRTRingDefinition(
        dataSupplier.getFieldDefinitions().getFirst().getModulus(),
        dataSupplier.getFieldDefinitions().getSecond().getModulus());
    this.resourcePools = new Pair<>(resourcePoolLeft, resourcePoolRight);
  }

  @Override
  public Pair<ResourcePoolA, ResourcePoolB> getSubResourcePools() {
    return resourcePools;
  }

  @Override
  public Pair<FieldDefinition, FieldDefinition> getFieldDefinitions() {
    return dataSupplier.getFieldDefinitions();
  }

  @Override
  public CRTDataSupplier getDataSupplier() {
    return dataSupplier;
  }

  @Override
  public FieldDefinition getFieldDefinition() {
    return definition;
  }
}
