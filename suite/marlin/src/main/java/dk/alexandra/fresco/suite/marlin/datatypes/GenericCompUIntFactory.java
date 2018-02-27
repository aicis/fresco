package dk.alexandra.fresco.suite.marlin.datatypes;

import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.suite.marlin.util.BigUIntSerializer;
import java.security.SecureRandom;

public class GenericCompUIntFactory implements
    CompUIntFactory<GenericCompUInt> {

  private final SecureRandom random = new SecureRandom();
  private final int highBitLength;
  private final int lowBitLength;

  public GenericCompUIntFactory(int highBitLength, int lowBitLength) {
    this.highBitLength = highBitLength;
    this.lowBitLength = lowBitLength;
  }

  @Override
  public GenericCompUInt createFromBytes(byte[] bytes) {
    return new GenericCompUInt(bytes, 128);
  }

  @Override
  public GenericCompUInt createRandom() {
    byte[] bytes = new byte[16];
    this.random.nextBytes(bytes);
    return createFromBytes(bytes);
  }

  @Override
  public ByteSerializer<GenericCompUInt> createSerializer() {
    return new BigUIntSerializer<>(this);
  }

  @Override
  public int getLowBitLength() {
    return lowBitLength;
  }

  @Override
  public int getHighBitLength() {
    return highBitLength;
  }

}
