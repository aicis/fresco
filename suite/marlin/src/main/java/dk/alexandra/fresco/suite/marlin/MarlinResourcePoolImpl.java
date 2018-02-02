package dk.alexandra.fresco.suite.marlin;

import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.sce.resources.ResourcePoolImpl;
import dk.alexandra.fresco.framework.util.Drbg;
import java.math.BigInteger;

public class MarlinResourcePoolImpl extends ResourcePoolImpl implements MarlinResourcePool {

  private final int operationalBitLength;
  private final int effectiveBitLength;
  private final BigInteger modulus;

  /**
   * Creates new {@link MarlinResourcePool}.
   */
  private MarlinResourcePoolImpl(int myId, int noOfPlayers, Drbg drbg, int operationalBitLength,
      int effectiveBitLength) {
    super(myId, noOfPlayers, drbg);
    if (operationalBitLength != 128) {
      throw new IllegalArgumentException(
          "Current implementation only supports 128 operational bit length");
    }
    if (effectiveBitLength != 64) {
      throw new IllegalArgumentException(
          "Current implementation only supports 128 element bit length");
    }
    this.operationalBitLength = operationalBitLength;
    this.effectiveBitLength = effectiveBitLength;
    this.modulus = BigInteger.ONE.shiftLeft(operationalBitLength);
  }

  /**
   * Creates new {@link MarlinResourcePool}.
   */
  public MarlinResourcePoolImpl(int myId, int noOfPlayers, Drbg drbg) {
    this(myId, noOfPlayers, drbg, 128, 64);
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
  public BigInteger getModulus() {
    return modulus;
  }

  @Override
  public ByteSerializer<BigInteger> getSerializer() {
    throw new UnsupportedOperationException("This suite does not support serializing big integers");
  }

}
