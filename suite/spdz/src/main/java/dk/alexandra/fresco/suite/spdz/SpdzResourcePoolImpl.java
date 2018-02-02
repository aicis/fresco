package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.network.serializers.BigIntegerWithFixedLengthSerializer;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.sce.resources.ResourcePoolImpl;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.ExceptionConverter;
import dk.alexandra.fresco.suite.spdz.storage.SpdzStorage;
import java.math.BigInteger;
import java.security.MessageDigest;

public class SpdzResourcePoolImpl extends ResourcePoolImpl implements SpdzResourcePool {

  private MessageDigest messageDigest;
  private int modulusSize;
  private BigInteger modulus;
  private BigInteger modulusHalf;
  private SpdzStorage storage;

  /**
   * Construct a ResourcePool implementation suitable for the spdz protocol suite.
   * @param myId The id of the party
   * @param noOfPlayers The amount of parties
   * @param drbg The randomness to use
   * @param storage The storage to use
   */
  public SpdzResourcePoolImpl(int myId, int noOfPlayers, Drbg drbg, SpdzStorage storage) {
    super(myId, noOfPlayers, drbg);

    this.storage = storage;

    messageDigest = ExceptionConverter.safe(
        () -> MessageDigest.getInstance("SHA-256"),
        "Configuration error, SHA-256 is needed for Spdz");

    // To make sure we are properly initialized, may throw runtime exceptions if not
    storage.getSecretSharedKey();

    // Initialize various fields global to the computation.
    this.modulus = storage.getSupplier().getModulus();
    this.modulusHalf = this.modulus.divide(BigInteger.valueOf(2));
    this.modulusSize = this.modulus.toByteArray().length;

  }

  @Override
  public BigInteger getModulus() {
    return modulus;
  }

  @Override
  public ByteSerializer<BigInteger> getSerializer() {
    return new BigIntegerWithFixedLengthSerializer(modulusSize);
  }

  @Override
  public SpdzStorage getStore() {
    return storage;
  }

  @Override
  public MessageDigest getMessageDigest() {
    return messageDigest;
  }

  @Override
  public BigInteger convertRepresentation(BigInteger bigInteger) {
    BigInteger actual = bigInteger.mod(modulus);
    if (actual.compareTo(modulusHalf) > 0) {
      actual = actual.subtract(modulus);
    }
    return actual;
  }
}
