package dk.alexandra.fresco.suite.spdz2k.datatypes;

import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.suite.spdz2k.util.UIntSerializer;
import java.math.BigInteger;
import java.security.SecureRandom;

public class CompUInt128Factory implements CompUIntFactory<CompUInt128> {

  private static final CompUInt128 ZERO = new CompUInt128(new byte[16]);
  private final SecureRandom random = new SecureRandom();

  @Override
  public CompUInt128 createFromBytes(byte[] bytes) {
    return new CompUInt128(bytes);
  }

  @Override
  public CompUInt128 createRandom() {
    byte[] bytes = new byte[16];
    this.random.nextBytes(bytes);
    return createFromBytes(bytes);
  }

  @Override
  public ByteSerializer<CompUInt128> createSerializer() {
    return new UIntSerializer<>(this);
  }

  @Override
  public int getLowBitLength() {
    return 64;
  }

  @Override
  public int getHighBitLength() {
    return 64;
  }

  @Override
  public CompUInt128 createFromBigInteger(BigInteger value) {
    return value == null ? null : new CompUInt128(value.toByteArray(), true);
  }

  @Override
  public CompUInt128 zero() {
    return ZERO;
  }

}
