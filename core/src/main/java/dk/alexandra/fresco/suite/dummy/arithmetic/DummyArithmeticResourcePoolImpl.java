package dk.alexandra.fresco.suite.dummy.arithmetic;

import dk.alexandra.fresco.framework.builder.numeric.FieldInteger;
import dk.alexandra.fresco.framework.builder.numeric.FieldElement;
import dk.alexandra.fresco.framework.builder.numeric.Modulus;
import dk.alexandra.fresco.framework.network.serializers.BigIntegerWithFixedLengthSerializer;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.sce.resources.ResourcePoolImpl;
import dk.alexandra.fresco.framework.util.ModulusFinder;
import dk.alexandra.fresco.framework.value.SInt;
import java.util.function.Function;

/**
 * Implements the resource pool needed for the Dummy Arithmetic suite.
 */
public class DummyArithmeticResourcePoolImpl extends ResourcePoolImpl
    implements DummyArithmeticResourcePool {

  private final Modulus modulus;
  private final int modulusSize;
  private final Function<byte[], FieldElement> bigIntegerSupplier;

  /**
   * Constructs a new {@link ResourcePool} for the Dummy Arithmetic suite.
   *
   * @param myId id of this party
   * @param noOfPlayers number of parties in the participating
   */
  public DummyArithmeticResourcePoolImpl(int myId, int noOfPlayers) {
    this(myId, noOfPlayers, ModulusFinder.findSuitableModulus(128),
        (bytes) -> FieldInteger.fromBytes(bytes, ModulusFinder.findSuitableModulus(128)));
  }

  /**
   * Constructs a new {@link ResourcePool} for the Dummy Arithmetic suite.
   *
   * @param myId id of this party
   * @param noOfPlayers number of parties in the participating
   * @param modulus the modulus
   * @param bigIntegerSupplier supplies wrapped big ints
   */
  public DummyArithmeticResourcePoolImpl(int myId, int noOfPlayers, Modulus modulus,
      Function<byte[], FieldElement> bigIntegerSupplier) {
    super(myId, noOfPlayers);
    this.bigIntegerSupplier = bigIntegerSupplier;
    this.modulus = modulus;
    this.modulusSize = modulus.getBigInteger().toByteArray().length;
  }

  @Override
  public Modulus getModulus() {
    return this.modulus;
  }

  @Override
  public ByteSerializer<FieldElement> getSerializer() {
    return new BigIntegerWithFixedLengthSerializer(modulusSize, bigIntegerSupplier);
  }

  @Override
  public SInt createSInt(FieldElement add) {
    return new DummyArithmeticSInt(add);
  }
}
