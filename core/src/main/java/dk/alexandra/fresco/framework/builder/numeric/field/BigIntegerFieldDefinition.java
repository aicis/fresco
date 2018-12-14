package dk.alexandra.fresco.framework.builder.numeric.field;

import dk.alexandra.fresco.framework.util.StrictBitVector;
import java.math.BigInteger;
import java.util.List;

public final class BigIntegerFieldDefinition implements FieldDefinition {

  private final BigIntegerModulus modulus;
  private final BigInteger modulusHalf;
  private final int modulusLength;
  private int modulusBitLength;

  public BigIntegerFieldDefinition(BigIntegerModulus modulus) {
    this.modulus = modulus;
    this.modulusHalf = modulus.getBigInteger().shiftRight(1);
    this.modulusBitLength = modulus.getBigInteger().bitLength();
    this.modulusLength = modulusBitLength / 8;
  }

  @Override
  public BigInteger convertToUnsigned(FieldElement value) {
    return BigIntegerFieldElement.extractValue(value);
  }

  @Override
  public BigInteger convertToSigned(BigInteger signed) {
    return FieldUtils.convertRepresentation(signed, getModulus(), modulusHalf);
  }

  @Override
  public BigInteger getModulus() {
    return modulus.getBigInteger();
  }

  @Override
  public int getBitLength() {
    return modulusBitLength;
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
    return FieldUtils.deserialize(bytes, modulusLength, this::createElement);
  }

  @Override
  public List<FieldElement> deserializeList(byte[] bytes) {
    return FieldUtils.deserializeList(bytes, modulusLength, this::createElement);
  }

  @Override
  public byte[] serialize(FieldElement fieldElement) {
    return FieldUtils.serialize(modulusLength, BigIntegerFieldElement.extractValue(fieldElement));
  }

  @Override
  public byte[] serialize(List<FieldElement> fieldElements) {
    return FieldUtils
        .serializeList(modulusLength, fieldElements, BigIntegerFieldElement::extractValue);
  }

  @Override
  public StrictBitVector convertToBitVector(FieldElement fieldElement) {
    return FieldUtils.convertToBitVector(
        getBitLength(), BigIntegerFieldElement.extractValue(fieldElement));
  }
}
