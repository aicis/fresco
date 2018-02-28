package dk.alexandra.fresco.suite.marlin.datatypes;

import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.suite.marlin.util.UIntSerializer;
import java.security.SecureRandom;

public class CompUInt128Factory implements CompUIntFactory<CompUInt128> {

  private final SecureRandom random = new SecureRandom();

  @Override
  public CompUInt128 createFromBytes(byte[] bytes) {
    return new CompUInt128(bytes);
  }

  @Override
  public CompUInt128 createRandom() {
    byte[] bytes = new byte[16];
    this.random.nextBytes(bytes);
    return createFromBytes(bytes);
  }

  @Override
  public ByteSerializer<CompUInt128> createSerializer() {
    return new UIntSerializer<>(this);
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
