package dk.alexandra.fresco.suite.marlin.datatypes;

import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.suite.marlin.util.BigUIntSerializer;
import java.security.SecureRandom;

public class GenericCompositeUIntFactory implements CompositeUIntFactory<GenericCompositeUInt> {

  private final SecureRandom random = new SecureRandom();

  @Override
  public GenericCompositeUInt createFromBytes(byte[] bytes) {
    return new GenericCompositeUInt(bytes, 128);
  }

  @Override
  public GenericCompositeUInt createFromLow(GenericCompositeUInt value) {
    return new GenericCompositeUInt(value, 128);
  }

  @Override
  public GenericCompositeUInt createRandom() {
    byte[] bytes = new byte[16];
    this.random.nextBytes(bytes);
    return createFromBytes(bytes);
  }

  @Override
  public ByteSerializer<GenericCompositeUInt> createSerializer() {
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
