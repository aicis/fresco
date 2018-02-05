package dk.alexandra.fresco.suite.marlin.datatypes;

import java.security.SecureRandom;

public class MutableUInt128Factory implements BigUIntFactory<MutableUInt128> {

  private final SecureRandom random = new SecureRandom();

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

}
