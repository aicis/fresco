package dk.alexandra.fresco.framework.builder.numeric.field;

import dk.alexandra.fresco.framework.util.StrictBitVector;
import java.math.BigInteger;
import java.util.List;

public final class BigIntegerFieldDefinition implements FieldDefinition {

  private final BigIntegerModulus modulus;
  private final int modulusLength;
  private final BigInteger modulusHalf;

  public BigIntegerFieldDefinition(BigIntegerModulus modulus) {
    FieldUtils.ensureDivisible(modulus.getBigInteger());
    this.modulus = modulus;
    this.modulusHalf = modulus.getBigInteger().shiftRight(1);
    this.modulusLength = modulus.getBigInteger().toByteArray().length;
  }

  @Override
  public BigInteger convertRepresentation(FieldElement value) {
    return FieldUtils.convertRepresentation(
        BigIntegerFieldElement.extractValue(value), getModulus(), modulusHalf);
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
    return FieldUtils.deserializeList(bytes, modulusLength, this::deserialize);
  }

  @Override
  public byte[] serialize(FieldElement fieldElement) {
    return ((BigIntegerFieldElement) fieldElement).toByteArray();
  }

  @Override
  public byte[] serialize(List<FieldElement> fieldElements) {
    return FieldUtils.serialize(modulusLength, fieldElements, this::serialize);
  }

  @Override
  public StrictBitVector convertToBitVector(FieldElement fieldElement) {
    return FieldUtils.convertToBitVector(getModulus(), serialize(fieldElement));
  }
}
