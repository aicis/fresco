package dk.alexandra.fresco.framework.builder.numeric.field;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public final class BigIntegerFieldDefinition implements FieldDefinition {

  private final BigIntegerModulus modulus;
  private final int modulusLength;
  private final BigInteger modulusHalf;

  public BigIntegerFieldDefinition(BigIntegerModulus modulus) {
    this.modulus = modulus;
    this.modulusHalf = modulus.getBigInteger().shiftRight(1);
    this.modulusLength = modulus.getBigInteger().toByteArray().length;
  }

  @Override
  public BigInteger convertRepresentation(FieldElement value) {
    BigInteger actual = BigIntegerFieldElement.extractValue(value);
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
    return BigIntegerFieldElement.create(value, modulus);
  }

  @Override
  public FieldElement createElement(String value) {
    return BigIntegerFieldElement.create(value, modulus);
  }

  @Override
  public FieldElement createElement(BigInteger value) {
    return BigIntegerFieldElement.create(value, modulus);
  }

  @Override
  public FieldElement deserialize(byte[] bytes) {
    return BigIntegerFieldElement.create(bytes, modulus);
  }

  @Override
  public List<FieldElement> deserializeList(byte[] bytes) {
    ArrayList<FieldElement> elements = new ArrayList<>();
    for (int i = 0; i < bytes.length; i += modulusLength) {
      byte[] copy = new byte[modulusLength];
      System.arraycopy(bytes, i, copy, 0, modulusLength);
      elements.add(BigIntegerFieldElement.create(copy, modulus));
    }
    return elements;
  }

  @Override
  public byte[] serialize(FieldElement fieldElement) {
    return ((BigIntegerFieldElement) fieldElement).toByteArray();
  }

  @Override
  public byte[] serialize(List<FieldElement> fieldElements) {
    byte[] bytes = new byte[modulusLength * fieldElements.size()];
    for (int i = 0; i < fieldElements.size(); i++) {
      BigIntegerFieldElement fieldElement = (BigIntegerFieldElement) fieldElements.get(i);
      fieldElement.toByteArray(bytes, i * modulusLength, modulusLength);
    }
    return bytes;
  }
}
