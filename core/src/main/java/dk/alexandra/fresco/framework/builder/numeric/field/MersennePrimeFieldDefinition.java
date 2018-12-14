package dk.alexandra.fresco.framework.builder.numeric.field;

import dk.alexandra.fresco.framework.util.StrictBitVector;
import java.math.BigInteger;
import java.util.List;

public final class MersennePrimeFieldDefinition implements FieldDefinition {

  private final MersennePrimeModulus modulus;
  private final BigInteger modulusHalf;
  private final int modulusLength;
  private int modulusBitLength;

  public MersennePrimeFieldDefinition(MersennePrimeModulus modulus) {
    this.modulus = modulus;
    this.modulusHalf = modulus.getBigInteger().shiftRight(1);
    this.modulusLength = this.modulus.getBigInteger().toByteArray().length;
    this.modulusBitLength = modulus.getBigInteger().bitLength();
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
    return modulus.getBigInteger();
  }

  @Override
  public int getBitLength() {
    return modulusBitLength;
  }

  @Override
  public FieldElement deserialize(byte[] bytes) {
    return MersennePrimeFieldElement.create(bytes, modulus);
  }

  @Override
  public List<FieldElement> deserializeList(byte[] bytes) {
    return FieldUtils.deserializeList(bytes, modulusLength, this::deserialize);
  }

  @Override
  public byte[] serialize(FieldElement fieldElement) {
    return ((MersennePrimeFieldElement) fieldElement).toByteArray();
  }

  @Override
  public byte[] serialize(List<FieldElement> fieldElements) {
    return FieldUtils.serialize(modulusLength, fieldElements, this::serialize);
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
    return FieldUtils.convertToBitVector(getBitLength(), serialize(fieldElement));
  }
}
