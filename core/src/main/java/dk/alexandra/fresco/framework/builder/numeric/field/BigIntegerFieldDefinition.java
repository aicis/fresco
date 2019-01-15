package dk.alexandra.fresco.framework.builder.numeric.field;

import dk.alexandra.fresco.framework.util.StrictBitVector;
import java.math.BigInteger;
import java.util.List;

/**
 * Defines a field based on a modulus as a normal BigInteger.
 */
public final class BigIntegerFieldDefinition implements FieldDefinition {

  private final BigIntegerModulus modulus;
  private final BigInteger modulusHalf;
  private final FieldUtils utils;
  private final int modulusBitLength;

  /**
   * Construct a new field definition for a specified modulus.
   *
   * @param modulus the modulus
   */
  public BigIntegerFieldDefinition(BigInteger modulus) {
    this.modulus = new BigIntegerModulus(modulus);
    this.modulusHalf = this.modulus.getBigInteger().shiftRight(1);
    this.modulusBitLength = this.modulus.getBigInteger().bitLength();
    this.utils = new FieldUtils(modulusBitLength, this::createElement,
        BigIntegerFieldElement::extractValue);
  }

  /**
   * Construct a new field definition for a specified modulus.
   *
   * @param modulus the modulus as a string.
   */
  public BigIntegerFieldDefinition(String modulus) {
    this(new BigInteger(modulus));
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
