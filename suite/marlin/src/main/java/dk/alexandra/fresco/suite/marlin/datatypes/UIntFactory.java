package dk.alexandra.fresco.suite.marlin.datatypes;

import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.suite.marlin.util.BigUIntSerializer;
import java.security.SecureRandom;

public class UIntFactory implements BigUIntFactory<UInt> {

  private final SecureRandom random = new SecureRandom();

  @Override
  public UInt createFromLong(long value) {
    return new UInt(value, 128);
  }

  @Override
  public UInt createFromBytes(byte[] bytes) {
    return new UInt(bytes, 128);
  }

  @Override
  public UInt createRandom() {
    byte[] bytes = new byte[16];
    this.random.nextBytes(bytes);
    return createFromBytes(bytes);
  }

  @Override
  public ByteSerializer<UInt> createSerializer() {
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
