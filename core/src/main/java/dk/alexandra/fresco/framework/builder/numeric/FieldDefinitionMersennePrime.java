package dk.alexandra.fresco.framework.builder.numeric;

import java.math.BigInteger;

public final class FieldDefinitionMersennePrime implements FieldDefinition {

  private ModulusMersennePrime modulus;

  public FieldDefinitionMersennePrime(ModulusMersennePrime modulus) {
    this.modulus = modulus;
  }

  @Override
  public BigInteger getModulus() {
    return modulus.getBigInteger();
  }

  @Override
  public BigInteger getModulusHalved() {
    return modulus.getBigIntegerHalved();
  }

  @Override
  public FieldElement deserialize(byte[] bytes, int offset, int length) {
    if (bytes.length > length) {
      byte[] dest = new byte[length];
      System.arraycopy(bytes, offset, dest, 0, length);
      return new FieldElementMersennePrime(dest, modulus);
    } else {
      return new FieldElementMersennePrime(bytes, modulus);
    }
  }

  @Override
  public void serialize(FieldElement fieldElement, byte[] bytes, int offset, int length) {
    fieldElement.toByteArray(bytes, offset, length);
  }

  @Override
  public FieldElement createElement(int value) {
    return new FieldElementMersennePrime(value, modulus);
  }

  @Override
  public FieldElement createElement(String value) {
    return new FieldElementMersennePrime(value, modulus);
  }

  @Override
  public FieldElement createElement(BigInteger value) {
    return new FieldElementMersennePrime(value, modulus);
  }
}
