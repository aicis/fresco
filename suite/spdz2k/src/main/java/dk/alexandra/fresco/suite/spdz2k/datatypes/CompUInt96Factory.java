package dk.alexandra.fresco.suite.spdz2k.datatypes;

import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.suite.spdz2k.util.UIntSerializer;
import java.security.SecureRandom;

public class CompUInt96Factory implements CompUIntFactory<CompUInt96> {

  private final SecureRandom random = new SecureRandom();

  @Override
  public CompUInt96 createFromBytes(byte[] bytes) {
    return new CompUInt96(bytes);
  }

  @Override
  public CompUInt96 createRandom() {
    byte[] bytes = new byte[12];
    this.random.nextBytes(bytes);
    return createFromBytes(bytes);
  }

  @Override
  public ByteSerializer<CompUInt96> createSerializer() {
    return new UIntSerializer<>(this);
  }

  @Override
  public int getLowBitLength() {
    return 32;
  }

  @Override
  public int getHighBitLength() {
    return 64;
  }

}
