package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.network.serializers.BigIntegerSerializer;
import dk.alexandra.fresco.framework.network.serializers.BigIntegerWithFixedLengthSerializer;
import dk.alexandra.fresco.framework.sce.resources.ResourcePoolImpl;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.suite.spdz.storage.SpdzStorage;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SpdzResourcePoolImpl extends ResourcePoolImpl implements SpdzResourcePool {

  private MessageDigest messageDigest;
  private int modulusSize;
  private BigInteger modulus;
  private BigInteger modulusHalf;
  private SpdzStorage store;

  public SpdzResourcePoolImpl(int myId, int noOfPlayers, Drbg drbg,
      SpdzStorage store) throws NoSuchAlgorithmException {
    super(myId, noOfPlayers, drbg);

    this.store = store;

    messageDigest = MessageDigest.getInstance("SHA-256");

    try {
      this.store.getSSK();
    } catch (MPCException e) {
      throw new MPCException("No preprocessed data found for SPDZ - aborting.", e);
    }

    // Initialize various fields global to the computation.
    this.modulus = store.getSupplier().getModulus();
    this.modulusHalf = this.modulus.divide(BigInteger.valueOf(2));
    this.modulusSize = this.modulus.toByteArray().length;

  }

  @Override
  public BigInteger getModulus() {
    return modulus;
  }

  @Override
  public BigIntegerSerializer getSerializer() {
    return new BigIntegerWithFixedLengthSerializer(modulusSize);
  }

  @Override
  public SpdzStorage getStore() {
    return store;
  }

  @Override
  public MessageDigest getMessageDigest() {
    return messageDigest;
  }

  @Override
  public BigInteger convertRepresentation(BigInteger b) {
    BigInteger actual = b.mod(modulus);
    if (actual.compareTo(modulusHalf) > 0) {
      actual = actual.subtract(modulus);
    }
    return actual;
  }
}
