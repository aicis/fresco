package dk.alexandra.fresco.suite.marlin.datatypes;

import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.suite.marlin.util.BigUIntSerializer;
import java.security.SecureRandom;

public class CompUInt128Factory implements CompUIntFactory<UInt64, UInt64, CompUInt128> {

  private final SecureRandom random = new SecureRandom();

  @Override
  public CompUInt128 createFromBytes(byte[] bytes) {
    return new CompUInt128(bytes);
  }

  @Override
  public CompUInt128 createFromHigh(UInt64 value) {
    return new CompUInt128(value);
  }

  @Override
  public CompUInt128 createFromLow(UInt64 value) {
    return new CompUInt128(value);
  }

  @Override
  public CompUInt128 createRandom() {
    byte[] bytes = new byte[16];
    this.random.nextBytes(bytes);
    return createFromBytes(bytes);
  }

  @Override
  public ByteSerializer<CompUInt128> createSerializer() {
    return new BigUIntSerializer<>(this);
  }

  @Override
  public int getCompositeBitLength() {
    return 128;
  }

  @Override
  public int getLowBitLength() {
    return 64;
  }

  @Override
  public int getHighBitLength() {
    return 64;
  }

}
