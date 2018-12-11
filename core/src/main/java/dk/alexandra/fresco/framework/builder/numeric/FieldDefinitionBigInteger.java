package dk.alexandra.fresco.framework.builder.numeric;

import java.math.BigInteger;

public final class FieldDefinitionBigInteger implements FieldDefinition {

  private ModulusBigInteger modulus;

  public FieldDefinitionBigInteger(ModulusBigInteger modulus) {
    this.modulus = modulus;
  }

  @Override
  public BigInteger getModulus() {
    return modulus.getBigInteger();
  }

  @Override
  public FieldElement deserialize(byte[] bytes, int offset, int length) {
    return new FieldElementBigInteger(bytes, modulus);
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
