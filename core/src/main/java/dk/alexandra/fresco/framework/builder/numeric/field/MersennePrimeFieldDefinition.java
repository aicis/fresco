package dk.alexandra.fresco.framework.builder.numeric.field;

import dk.alexandra.fresco.framework.util.StrictBitVector;
import java.math.BigInteger;
import java.util.List;

public final class MersennePrimeFieldDefinition implements FieldDefinition {

  private final MersennePrimeModulus modulus;
  private final BigInteger modulusHalf;
  private final int modulusLength;
  private final int modulusBitLength;

  public MersennePrimeFieldDefinition(int bitLength, int constant) {
    this.modulus = new MersennePrimeModulus(bitLength, constant);
    this.modulusHalf = modulus.getPrime().shiftRight(1);
    this.modulusBitLength = bitLength;
    this.modulusLength = FieldUtils.bytesNeededForBits(modulusBitLength);
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
  public BigInteger getModulus() {
    return modulus.getPrime();
  }

  @Override
  public int getBitLength() {
    return modulusBitLength;
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
    return FieldUtils.serialize(
        modulusLength, MersennePrimeFieldElement.extractValue(fieldElement));
  }

  @Override
  public byte[] serialize(List<FieldElement> fieldElements) {
    return FieldUtils.serializeList(
        modulusLength, fieldElements, MersennePrimeFieldElement::extractValue);
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
  public StrictBitVector convertToBitVector(FieldElement fieldElement) {
    return FieldUtils.convertToBitVector(
        modulusLength, MersennePrimeFieldElement.extractValue(fieldElement));
  }
}
