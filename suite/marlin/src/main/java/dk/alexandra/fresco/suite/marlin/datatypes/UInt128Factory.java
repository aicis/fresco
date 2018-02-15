package dk.alexandra.fresco.suite.marlin.datatypes;

import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.suite.marlin.util.BigUIntSerializer;
import java.security.SecureRandom;

public class UInt128Factory implements BigUIntFactory<UInt128> {

  private final SecureRandom random = new SecureRandom();

  @Override
  public UInt128 createFromLong(long value) {
    return new UInt128(value);
  }

  @Override
  public UInt128 createFromBytes(byte[] bytes) {
    return new UInt128(bytes);
  }

  @Override
  public UInt128 createRandom() {
    byte[] bytes = new byte[16];
    this.random.nextBytes(bytes);
    return createFromBytes(bytes);
  }

  @Override
  public ByteSerializer<UInt128> createSerializer() {
    return new BigUIntSerializer<>(this);
  }

  @Override
  public int getOperationalBitLength() {
    return 128;
  }

  @Override
  public int getEffectiveBitLength() {
    return 64;
  }

}
