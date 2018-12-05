package dk.alexandra.fresco.suite.dummy.arithmetic;

import dk.alexandra.fresco.framework.builder.numeric.BigInt;
import dk.alexandra.fresco.framework.builder.numeric.BigIntegerI;
import dk.alexandra.fresco.framework.network.serializers.BigIntegerWithFixedLengthSerializer;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.sce.resources.ResourcePoolImpl;
import dk.alexandra.fresco.framework.util.ModulusFinder;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;
import java.util.function.Function;

/**
 * Implements the resource pool needed for the Dummy Arithmetic suite.
 */
public class DummyArithmeticResourcePoolImpl extends ResourcePoolImpl
    implements DummyArithmeticResourcePool {

  private final BigInteger modulus;
  private final int modulusSize;
  private final Function<byte[], BigIntegerI> bigIntegerSupplier;

  /**
   * Constructs a new {@link ResourcePool} for the Dummy Arithmetic suite.
   *
   * @param myId id of this party
   * @param noOfPlayers number of parties in the participating
   */
  public DummyArithmeticResourcePoolImpl(int myId, int noOfPlayers) {
    this(myId, noOfPlayers, ModulusFinder.findSuitableModulus(128),
        (bytes) -> BigInt.fromBytes(bytes, ModulusFinder.findSuitableModulus(128)));
  }

  /**
   * Constructs a new {@link ResourcePool} for the Dummy Arithmetic suite.
   *
   * @param myId id of this party
   * @param noOfPlayers number of parties in the participating
   * @param modulus the modulus
   * @param bigIntegerSupplier supplies wrapped big ints
   */
  public DummyArithmeticResourcePoolImpl(int myId, int noOfPlayers, BigInteger modulus,
      Function<byte[], BigIntegerI> bigIntegerSupplier) {
    super(myId, noOfPlayers);
    this.bigIntegerSupplier = bigIntegerSupplier;
    this.modulus = modulus;
    this.modulusSize = modulus.toByteArray().length;
  }

  @Override
  public BigInteger getModulus() {
    return this.modulus;
  }

  @Override
  public ByteSerializer<BigIntegerI> getSerializer() {
    return new BigIntegerWithFixedLengthSerializer(modulusSize, bigIntegerSupplier);
  }

  @Override
  public SInt createSInt(BigIntegerI add) {
    return new DummyArithmeticSInt(add);
  }
}
