package dk.alexandra.fresco.framework.builder.numeric.field;

import dk.alexandra.fresco.framework.util.StrictBitVector;
import java.math.BigInteger;
import java.util.List;

public final class MersennePrimeFieldDefinition implements FieldDefinition {

  private final MersennePrimeModulus modulus;
  private final BigInteger modulusHalf;
  private final int modulusBitLength;
  private final FieldUtils utils;

  /**
   * Construct a new field definition for a pseudo mersenne prime.
   *
   * @param bitLength the bitlength of the prime
   * @param constant the constant subtracted from 2^bitlength
   */
  public MersennePrimeFieldDefinition(int bitLength, int constant) {
    this.modulus = new MersennePrimeModulus(bitLength, constant);
    this.modulusHalf = modulus.getPrime().shiftRight(1);
    this.modulusBitLength = bitLength;
    this.utils = new FieldUtils(modulusBitLength, this::createElement,
        MersennePrimeFieldElement::extractValue);
  }

  @Override
  public FieldElement createElement(int value) {
    return MersennePrimeFieldElement.create(value, modulus);
  }

  @Override
  public FieldElement createElement(String value) {
    return MersennePrimeFieldElement.create(value, modulus);
  }

  @Override
  public FieldElement createElement(BigInteger value) {
    return MersennePrimeFieldElement.create(value, modulus);
  }

  @Override
  public BigInteger getModulus() {
    return modulus.getPrime();
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
    return MersennePrimeFieldElement.extractValue(value);
  }

  @Override
  public BigInteger convertToSigned(BigInteger signed) {
    return FieldUtils.convertRepresentation(signed, getModulus(), modulusHalf);
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
