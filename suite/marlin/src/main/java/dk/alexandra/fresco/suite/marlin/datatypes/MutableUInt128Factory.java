package dk.alexandra.fresco.suite.marlin.datatypes;

import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.suite.marlin.util.BigUIntSerializer;
import java.security.SecureRandom;

public class MutableUInt128Factory implements BigUIntFactory<MutableUInt128> {

  private final SecureRandom random = new SecureRandom();

  @Override
  public MutableUInt128 createFromLong(long value) {
    return new MutableUInt128(value);
  }

  @Override
  public MutableUInt128 createFromBytes(byte[] bytes) {
    return new MutableUInt128(bytes);
  }

  @Override
  public MutableUInt128 createRandom() {
    byte[] bytes = new byte[16];
    this.random.nextBytes(bytes);
    return createFromBytes(bytes);
  }

  @Override
  public ByteSerializer<MutableUInt128> createSerializer() {
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
