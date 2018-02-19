package dk.alexandra.fresco.suite.marlin.datatypes;

import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.suite.marlin.util.BigUIntSerializer;
import java.security.SecureRandom;

public class UIntFactory implements BigUIntFactory<UInt> {

  private final SecureRandom random = new SecureRandom();

  @Override
  public UInt createFromBytes(byte[] bytes) {
    return new UInt(bytes, 128);
  }

  @Override
  public UInt createFromLow(UInt value) {
    return new UInt(value, 128);
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
