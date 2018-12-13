package dk.alexandra.fresco.framework.builder.numeric.field;

import java.math.BigInteger;
import java.util.ArrayList;
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
    BigInteger actual = MersennePrimeFieldElement.extractValue(value);
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
  public FieldElement deserialize(byte[] bytes) {
    return MersennePrimeFieldElement.create(bytes, modulus);
  }

  @Override
  public List<FieldElement> deserializeList(byte[] bytes) {
    ArrayList<FieldElement> elements = new ArrayList<>();
    for (int i = 0; i < bytes.length; i += modulusLength) {
      byte[] copy = new byte[modulusLength];
      System.arraycopy(bytes, i * modulusLength, copy, 0, modulusLength);
      elements.add(MersennePrimeFieldElement.create(copy, modulus));
    }
    return elements;
  }

  @Override
  public byte[] serialize(FieldElement fieldElement) {
    return ((MersennePrimeFieldElement) fieldElement).toByteArray();
  }

  @Override
  public byte[] serialize(List<FieldElement> fieldElements) {
    byte[] bytes = new byte[modulusLength * fieldElements.size()];
    for (int i = 0; i < fieldElements.size(); i++) {
      MersennePrimeFieldElement fieldElement = (MersennePrimeFieldElement) fieldElements.get(i);
      fieldElement.toByteArray(bytes, i * modulusLength, modulusLength);
    }
    return bytes;
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
}
