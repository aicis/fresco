package dk.alexandra.fresco.suite.spdz2k.datatypes;

import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.suite.spdz2k.util.UIntSerializer;
import java.math.BigInteger;
import java.security.SecureRandom;

public class CompUInt64Factory implements CompUIntFactory<CompUInt64> {

  private static final CompUInt64 ZERO = new CompUInt64(0);
  private static final CompUInt64 ONE = new CompUInt64(1);
  private static final CompUInt64 TWO = new CompUInt64(2);
  private final SecureRandom random = new SecureRandom();

  @Override
  public CompUInt64 createFromBytes(byte[] bytes) {
    return new CompUInt64(bytes);
  }

  @Override
  public CompUInt64 createRandom() {
    byte[] bytes = new byte[8];
    this.random.nextBytes(bytes);
    return createFromBytes(bytes);
  }

  @Override
  public CompUInt64 fromBit(int bit) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ByteSerializer<CompUInt64> createSerializer() {
    return new UIntSerializer<>(this);
  }

  @Override
  public int getLowBitLength() {
    return 32;
  }

  @Override
  public int getHighBitLength() {
    return 32;
  }

  @Override
  public CompUInt64 createFromBigInteger(BigInteger value) {
    return value == null ? null : new CompUInt64(value);
  }

  @Override
  public CompUInt64 zero() {
    return ZERO;
  }

  @Override
  public CompUInt64 one() {
    return ONE;
  }

  @Override
  public CompUInt64 two() {
    return TWO;
  }

}
