package dk.alexandra.fresco.framework.builder.numeric.field;

import dk.alexandra.fresco.framework.util.StrictBitVector;
import java.math.BigInteger;
import java.util.List;

public final class BigIntegerFieldDefinition implements FieldDefinition {

  private final BigIntegerModulus modulus;
  private final BigInteger modulusHalf;
  private final FieldUtils utils;
  private final int modulusBitLength;

  /**
   * Construct a new field definition for a specified modulus.
   *
   * @param value the modulus
   */
  public BigIntegerFieldDefinition(BigInteger value) {
    this.modulus = new BigIntegerModulus(value);
    this.modulusHalf = modulus.getBigInteger().shiftRight(1);
    this.modulusBitLength = modulus.getBigInteger().bitLength();
    this.utils = new FieldUtils(modulusBitLength, this::createElement,
        BigIntegerFieldElement::extractValue);
  }

  public BigIntegerFieldDefinition(int value) {
    this(BigInteger.valueOf(value));
  }

  public BigIntegerFieldDefinition(String value) {
    this(new BigInteger(value));
  }

  @Override
  public FieldElement createElement(long value) {
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
  public BigInteger getModulus() {
    return modulus.getBigInteger();
  }

  @Override
  public int getBitLength() {
    return modulusBitLength;
  }

  @Override
  public StrictBitVector convertToBitVector(FieldElement fieldElement) {
    return utils.convertToBitVector(fieldElement);
  }

  @Override
  public BigInteger convertToUnsigned(FieldElement value) {
    return BigIntegerFieldElement.extractValue(value);
  }

  @Override
  public BigInteger convertToSigned(BigInteger asUnsigned) {
    return FieldUtils.convertRepresentation(asUnsigned, getModulus(), modulusHalf);
  }

  @Override
  public byte[] serialize(FieldElement fieldElement) {
    return utils.serialize(fieldElement);
  }

  @Override
  public byte[] serialize(List<FieldElement> fieldElements) {
    return utils.serializeList(fieldElements);
  }

  @Override
  public FieldElement deserialize(byte[] bytes) {
    return utils.deserialize(bytes);
  }

  @Override
  public List<FieldElement> deserializeList(byte[] bytes) {
    return utils.deserializeList(bytes);
  }
}
