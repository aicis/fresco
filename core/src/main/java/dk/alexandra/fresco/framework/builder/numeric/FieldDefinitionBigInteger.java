package dk.alexandra.fresco.framework.builder.numeric;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public final class FieldDefinitionBigInteger implements FieldDefinition {

  private final ModulusBigInteger modulus;
  private final int modulusLength;
  private final BigInteger modulusHalf;

  public FieldDefinitionBigInteger(ModulusBigInteger modulus) {
    this.modulus = modulus;
    this.modulusHalf = modulus.getBigInteger().shiftRight(1);
    this.modulusLength = modulus.getBigInteger().toByteArray().length;
  }

  @Override
  public BigInteger convertRepresentation(FieldElement value) {
    BigInteger actual = value.convertToBigInteger();
    if (actual.compareTo(modulusHalf) > 0) {
      return actual.subtract(getModulus());
    } else {
      return actual;
    }
  }

  @Override
  public BigInteger getModulus() {
    return modulus.getBigInteger();
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

  @Override
  public FieldElement deserialize(byte[] bytes) {
    return new FieldElementBigInteger(bytes, modulus);
  }

  @Override
  public List<FieldElement> deserializeList(byte[] bytes) {
    ArrayList<FieldElement> elements = new ArrayList<>();
    for (int i = 0; i < bytes.length; i += modulusLength) {
      byte[] copy = new byte[modulusLength];
      System.arraycopy(bytes, i * modulusLength, copy, 0, modulusLength);
      elements.add(new FieldElementBigInteger(copy, modulus));
    }
    return elements;
  }

  @Override
  public byte[] serialize(FieldElement fieldElement) {
    return ((FieldElementBigInteger) fieldElement).toByteArray();
  }

  @Override
  public byte[] serialize(List<FieldElement> fieldElements) {
    byte[] bytes = new byte[modulusLength * fieldElements.size()];
    for (int i = 0; i < fieldElements.size(); i++) {
      FieldElementBigInteger fieldElement = (FieldElementBigInteger) fieldElements.get(i);
      fieldElement.toByteArray(bytes, i * modulusLength, modulusLength);
    }
    return bytes;
  }
}
