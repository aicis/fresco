package dk.alexandra.fresco.framework.builder.numeric;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public final class FieldDefinitionMersennePrime implements FieldDefinition {

  private final ModulusMersennePrime modulus;
  private final int modulusLength;
  private final BigInteger modulusHalf;

  public FieldDefinitionMersennePrime(ModulusMersennePrime modulus) {
    this.modulus = modulus;
    this.modulusHalf = modulus.getBigInteger().shiftRight(1);
    this.modulusLength = this.modulus.getBigInteger().toByteArray().length;
  }

  @Override
  public BigInteger convertRepresentation(FieldElement value) {
    BigInteger actual = FieldElementMersennePrime.extractValue(value);
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
    return FieldElementMersennePrime.create(bytes, modulus);
  }

  @Override
  public List<FieldElement> deserializeList(byte[] bytes) {
    ArrayList<FieldElement> elements = new ArrayList<>();
    for (int i = 0; i < bytes.length; i += modulusLength) {
      byte[] copy = new byte[modulusLength];
      System.arraycopy(bytes, i * modulusLength, copy, 0, modulusLength);
      elements.add(FieldElementMersennePrime.create(copy, modulus));
    }
    return elements;
  }

  @Override
  public byte[] serialize(FieldElement fieldElement) {
    return ((FieldElementMersennePrime) fieldElement).toByteArray();
  }

  @Override
  public byte[] serialize(List<FieldElement> fieldElements) {
    byte[] bytes = new byte[modulusLength * fieldElements.size()];
    for (int i = 0; i < fieldElements.size(); i++) {
      FieldElementMersennePrime fieldElement = (FieldElementMersennePrime) fieldElements.get(i);
      fieldElement.toByteArray(bytes, i * modulusLength, modulusLength);
    }
    return bytes;
  }

  @Override
  public FieldElement createElement(int value) {
    return FieldElementMersennePrime.create(value, modulus);
  }

  @Override
  public FieldElement createElement(String value) {
    return FieldElementMersennePrime.create(value, modulus);
  }

  @Override
  public FieldElement createElement(BigInteger value) {
    return FieldElementMersennePrime.create(value, modulus);
  }
}
