package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.builder.numeric.FieldDefinition;
import dk.alexandra.fresco.framework.builder.numeric.FieldElement;
import dk.alexandra.fresco.framework.network.serializers.BigIntegerWithFixedLengthSerializer;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.sce.resources.ResourcePoolImpl;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.ExceptionConverter;
import dk.alexandra.fresco.framework.util.OpenedValueStore;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.storage.SpdzDataSupplier;
import java.math.BigInteger;
import java.security.MessageDigest;

public class SpdzResourcePoolImpl extends ResourcePoolImpl implements SpdzResourcePool {

  private final MessageDigest messageDigest;
  private final int modulusSize;
  private final BigInteger modulus;
  private final BigInteger modulusHalf;
  private final OpenedValueStore<SpdzSInt, FieldElement> openedValueStore;
  private final SpdzDataSupplier dataSupplier;
  private Drbg drbg;

  /**
   * Construct a ResourcePool implementation suitable for the spdz protocol suite.
   *
   * @param myId The id of the party
   * @param noOfPlayers The amount of parties
   * @param openedValueStore Store for maintaining opened values for later mac check
   * @param dataSupplier Pre-processing material supplier
   */
  public SpdzResourcePoolImpl(int myId, int noOfPlayers,
      OpenedValueStore<SpdzSInt, FieldElement> openedValueStore, SpdzDataSupplier dataSupplier,
      Drbg drbg) {
    super(myId, noOfPlayers);
    this.dataSupplier = dataSupplier;
    this.openedValueStore = openedValueStore;
    this.messageDigest = ExceptionConverter.safe(
        () -> MessageDigest.getInstance("SHA-256"),
        "Configuration error, SHA-256 is needed for Spdz");
    // Initialize various fields global to the computation.
    this.modulus = dataSupplier.getModulus();
    this.modulusHalf = dataSupplier.getFieldDefinition().getModulusHalved();
    this.modulusSize = this.modulus.toByteArray().length;
    this.drbg = drbg;
  }

  @Override
  public FieldDefinition getFieldDefinition() {
    return dataSupplier.getFieldDefinition();
  }

  @Override
  public ByteSerializer<FieldElement> getSerializer() {
    // TODO Define by the user of this class
    return new BigIntegerWithFixedLengthSerializer(modulusSize, getFieldDefinition());
  }

  @Override
  public OpenedValueStore<SpdzSInt, FieldElement> getOpenedValueStore() {
    return openedValueStore;
  }

  @Override
  public SpdzDataSupplier getDataSupplier() {
    return dataSupplier;
  }

  @Override
  public MessageDigest getMessageDigest() {
    return messageDigest;
  }

  @Override
  public Drbg getRandomGenerator() {
    if (drbg == null) {
      throw new IllegalStateException("Joint drbg must be initialized before use");
    }
    return drbg;
  }
}
