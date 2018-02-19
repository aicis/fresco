package dk.alexandra.fresco.suite.marlin.datatypes;

import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.suite.marlin.util.BigUIntSerializer;
import java.security.SecureRandom;

public class UInt128Factory implements BigUIntFactory<UInt128> {

  private final SecureRandom random = new SecureRandom();

  @Override
  public UInt128 createFromBytes(byte[] bytes) {
    return new UInt128(bytes);
  }

  @Override
  public UInt128 createFromLow(UInt128 value) {
    return null;
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
