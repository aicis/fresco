package dk.alexandra.fresco.suite.dummy.arithmetic;

import dk.alexandra.fresco.framework.builder.numeric.FieldDefinition;
import dk.alexandra.fresco.framework.builder.numeric.FieldElement;
import dk.alexandra.fresco.framework.network.serializers.BigIntegerWithFixedLengthSerializer;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.sce.resources.ResourcePoolImpl;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;

/**
 * Implements the resource pool needed for the Dummy Arithmetic suite.
 */
public class DummyArithmeticResourcePoolImpl extends ResourcePoolImpl
    implements DummyArithmeticResourcePool {

  private final int modulusSize;
  private final FieldDefinition fieldDefinition;

  /**
   * Constructs a new {@link ResourcePool} for the Dummy Arithmetic suite.
   *
   * @param myId id of this party
   * @param noOfPlayers number of parties in the participating
   * @param fieldDefinition definition of the mathematical field
   */
  public DummyArithmeticResourcePoolImpl(int myId, int noOfPlayers,
      FieldDefinition fieldDefinition) {
    super(myId, noOfPlayers);
    this.fieldDefinition = fieldDefinition;
    this.modulusSize = fieldDefinition.getModulus().bytesLength();
  }

  @Override
  public BigInteger getModulus() {
    return fieldDefinition.getModulus().getBigInteger();
  }

  @Override
  public FieldDefinition getFieldDefinition() {
    return fieldDefinition;
  }

  @Override
  public ByteSerializer<FieldElement> getSerializer() {
    return new BigIntegerWithFixedLengthSerializer(modulusSize, getFieldDefinition());
  }

  @Override
  public SInt createSInt(FieldElement add) {
    return new DummyArithmeticSInt(add);
  }
}
