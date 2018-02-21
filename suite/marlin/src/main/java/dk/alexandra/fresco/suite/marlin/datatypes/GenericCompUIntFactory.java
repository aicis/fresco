package dk.alexandra.fresco.suite.marlin.datatypes;

import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.suite.marlin.util.BigUIntSerializer;
import java.security.SecureRandom;

public class GenericCompUIntFactory implements
    CompUIntFactory<GenericUInt, GenericUInt, GenericUInt> {

  private final SecureRandom random = new SecureRandom();

  @Override
  public GenericUInt createFromBytes(byte[] bytes) {
    return new GenericUInt(bytes, 128);
  }

  @Override
  public GenericUInt createFromHigh(GenericUInt value) {
    return new GenericUInt(value, 128);
  }

  @Override
  public GenericUInt createRandom() {
    byte[] bytes = new byte[16];
    this.random.nextBytes(bytes);
    return createFromBytes(bytes);
  }

  @Override
  public ByteSerializer<GenericUInt> createSerializer() {
    return new BigUIntSerializer<>(this);
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
