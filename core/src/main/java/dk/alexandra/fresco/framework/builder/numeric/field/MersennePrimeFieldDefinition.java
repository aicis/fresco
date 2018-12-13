package dk.alexandra.fresco.framework.builder.numeric.field;

import dk.alexandra.fresco.framework.util.StrictBitVector;
import java.math.BigInteger;
import java.util.List;

public final class MersennePrimeFieldDefinition implements FieldDefinition {

  private final MersennePrimeModulus modulus;
  private final int modulusLength;
  private final BigInteger modulusHalf;

  public MersennePrimeFieldDefinition(MersennePrimeModulus modulus) {
    this.modulus = modulus;
    this.modulusHalf = modulus.getBigInteger().shiftRight(1);
    this.modulusLength = this.modulus.getBigInteger().toByteArray().length;
  }

  @Override
  public BigInteger convertRepresentation(FieldElement value) {
    return FieldUtils.convertRepresentation(value, getModulus(), modulusHalf,
        MersennePrimeFieldElement::extractValue);
  }

  @Override
  public BigInteger getModulus() {
    return modulus.getBigInteger();
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
    return FieldUtils.convertToBitVector(getModulus(), serialize(fieldElement));
  }
}
