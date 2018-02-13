package dk.alexandra.fresco.suite.marlin.resource;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.sce.resources.Broadcast;
import dk.alexandra.fresco.framework.sce.resources.ResourcePoolImpl;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.suite.marlin.datatypes.BigUInt;
import dk.alexandra.fresco.suite.marlin.datatypes.BigUIntFactory;
import dk.alexandra.fresco.suite.marlin.resource.storage.MarlinDataSupplier;
import dk.alexandra.fresco.suite.marlin.resource.storage.MarlinOpenedValueStore;
import java.math.BigInteger;

public class MarlinResourcePoolImpl<T extends BigUInt<T>> extends ResourcePoolImpl implements
    MarlinResourcePool {

  private final int operationalBitLength;
  private final int effectiveBitLength;
  private final BigInteger modulus;
  private final MarlinOpenedValueStore<T> storage;
  private final MarlinDataSupplier<T> supplier;
  private final BigUIntFactory<T> factory;
  private final ByteSerializer<T> rawSerializer;
  private Broadcast broadcast;

  /**
   * Creates new {@link MarlinResourcePool}.
   */
  private MarlinResourcePoolImpl(int myId, int noOfPlayers, Drbg drbg, int operationalBitLength,
      int effectiveBitLength, MarlinOpenedValueStore<T> storage, MarlinDataSupplier<T> supplier,
      BigUIntFactory<T> factory) {
    super(myId, noOfPlayers, drbg);
    if (operationalBitLength != 128) {
      throw new IllegalArgumentException(
          "Current implementation only supports 128 operational bit length");
    }
    if (effectiveBitLength != 64) {
      throw new IllegalArgumentException(
          "Current implementation only supports 64 effective bit length");
    }
    this.operationalBitLength = operationalBitLength;
    this.effectiveBitLength = effectiveBitLength;
    this.modulus = BigInteger.ONE.shiftLeft(operationalBitLength);
    this.storage = storage;
    this.supplier = supplier;
    this.factory = factory;
    this.rawSerializer = factory.createSerializer();
    this.broadcast = null;
  }

  /**
   * Creates new {@link MarlinResourcePool}.
   */
  public MarlinResourcePoolImpl(int myId, int noOfPlayers, Drbg drbg,
      MarlinOpenedValueStore<T> storage, MarlinDataSupplier<T> supplier,
      BigUIntFactory<T> factory) {
    this(myId, noOfPlayers, drbg, 128, 64, storage, supplier, factory);
  }

  @Override
  public int getOperationalBitLength() {
    return operationalBitLength;
  }

  @Override
  public int getEffectiveBitLength() {
    return effectiveBitLength;
  }

  @Override
  public MarlinOpenedValueStore getOpenedValueStore() {
    return storage;
  }

  @Override
  public MarlinDataSupplier getDataSupplier() {
    return supplier;
  }

  @Override
  public BigUIntFactory getFactory() {
    return factory;
  }

  @Override
  public ByteSerializer getRawSerializer() {
    return rawSerializer;
  }

  @Override
  public Broadcast createBroadcast(Network network) {
    // TODO come up with way to cache this
    return new Broadcast(network);
  }

  @Override
  public BigInteger getModulus() {
    return modulus;
  }

  @Override
  public ByteSerializer<BigInteger> getSerializer() {
    throw new UnsupportedOperationException("This suite does not support serializing big integers");
  }

}
