package dk.alexandra.fresco.framework.builder.numeric;

import java.math.BigInteger;

public final class FieldDefinitionBigInteger implements FieldDefinition {

  private final ModulusBigInteger modulus;

  public FieldDefinitionBigInteger(ModulusBigInteger modulus) {
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
  public FieldElement deserialize(byte[] bytes) {
    return deserialize(bytes, 0, bytes.length);
  }

  @Override
  public FieldElement deserialize(byte[] bytes, int offset, int length) {
    if (bytes.length > length) {
      byte[] dest = new byte[length];
      System.arraycopy(bytes, offset, dest, 0, length);
      return new FieldElementBigInteger(dest, modulus);
    } else {
      return new FieldElementBigInteger(bytes, modulus);
    }
  }

  @Override
  public byte[] serialize(FieldElement fieldElement) {
    byte[] bytes = new byte[fieldElement.convertToBigInteger().toByteArray().length];
    serialize(fieldElement, bytes, 0, bytes.length);
    return bytes;
  }

  @Override
  public void serialize(FieldElement fieldElement, byte[] bytes, int offset, int length) {
    fieldElement.toByteArray(bytes, offset, length);
  }

  @Override
  public FieldElement createElement(int value) {
    return new FieldElementBigInteger(value, modulus);
  }

  @Override
  public FieldElement createElement(String value) {
    return new FieldElementBigInteger(value, modulus);
  }

  @Override
  public FieldElement createElement(BigInteger value) {
    return new FieldElementBigInteger(value, modulus);
  }
}
