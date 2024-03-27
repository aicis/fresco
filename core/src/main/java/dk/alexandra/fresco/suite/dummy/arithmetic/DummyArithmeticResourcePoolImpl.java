package dk.alexandra.fresco.suite.dummy.arithmetic;

import dk.alexandra.fresco.framework.builder.numeric.field.FieldDefinition;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.sce.resources.ResourcePoolImpl;
import dk.alexandra.fresco.framework.util.ValidationUtils;

/**
 * Implements the resource pool needed for the Dummy Arithmetic suite.
 */
public class DummyArithmeticResourcePoolImpl extends ResourcePoolImpl
    implements DummyArithmeticResourcePool {

  private final FieldDefinition fieldDefinition;

  /**
   * Constructs a new {@link ResourcePool} for the Dummy Arithmetic suite.
   *
   * @param myId id of this party
   * @param noOfPlayers number of parties in the participating
   * @param fieldDefinition definition of the finite field in which arithmetic takes place
   */
  public DummyArithmeticResourcePoolImpl(int myId, int noOfPlayers,
      FieldDefinition fieldDefinition) {
    super(myId, noOfPlayers);
    ValidationUtils.assertValidId(myId, noOfPlayers);
    this.fieldDefinition = fieldDefinition;
  }

  @Override
  public FieldDefinition getFieldDefinition() {
    return fieldDefinition;
  }
}
