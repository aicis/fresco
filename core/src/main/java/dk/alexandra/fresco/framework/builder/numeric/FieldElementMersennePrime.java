package dk.alexandra.fresco.framework.builder.numeric;

import java.math.BigInteger;
import java.util.Objects;
import java.util.function.BiConsumer;

public class FieldElementMersennePrime implements FieldElement {

  private MersennePrimeInteger value;
  private Modulus modulus;

  public FieldElementMersennePrime(MersennePrimeInteger value, Modulus modulus) {
    this.value = value;
    this.modulus = modulus;
  }

  public FieldElementMersennePrime(int i, Modulus modulus) {
    this(new MersennePrimeInteger(i), modulus);
  }

  public FieldElementMersennePrime(String toString, Modulus modulus) {
    this(new MersennePrimeInteger(toString), modulus);
  }

  public FieldElementMersennePrime(byte[] bytes, Modulus modulus) {
    this(MersennePrimeInteger.fromBytes(bytes), modulus);
    this.value.mod((MersennePrimeInteger) modulus.get());
  }

  public FieldElementMersennePrime(BigInteger value, Modulus modulus) {
    this(value.toString(), modulus);
  }

  @Override
  public FieldElement add(FieldElement operand) {
    return safe(this::add, operand);
  }

  private void add(MersennePrimeInteger left, MersennePrimeInteger right) {
    left.add(right);
  }

  @Override
  public FieldElement subtract(FieldElement operand) {
    return safe(this::sub, operand);
  }

  private void sub(MersennePrimeInteger left, MersennePrimeInteger right) {
    left.sub(right);
  }

  @Override
  public FieldElement multiply(FieldElement operand) {
    return safe(this::mul, operand);
  }

  private void mul(MersennePrimeInteger left, MersennePrimeInteger right) {
    left.mul(right);
  }

  private FieldElementMersennePrime safe(
      BiConsumer<MersennePrimeInteger, MersennePrimeInteger> operation,
      FieldElement operand) {
    MersennePrimeInteger copy = value.copy();
    operation.accept(copy, (MersennePrimeInteger) operand);
    copy.mod((MersennePrimeInteger) modulus.get());
    return new FieldElementMersennePrime(copy, modulus);
  }

  @Override
  public BigInteger convertToBigInteger() {
    return new BigInteger(value.toString());
  }

  @Override
  public void toByteArray(byte[] bytes, int offset, int byteLength) {
    value.toByteArray(bytes, offset, byteLength);
  }

  @Override
  public int compareTo(FieldElement o) {
    return value.compareTo(((FieldElementMersennePrime) o).value);
  }

  @Override
  public String toString() {
    return "FieldElementMersennePrime{" +
        "value=" + value +
        ", modulus =" + modulus +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    FieldElementMersennePrime fieldElementMersennePrime = (FieldElementMersennePrime) o;
    return Objects.equals(modulus, fieldElementMersennePrime.modulus) &&
        Objects.equals(value, fieldElementMersennePrime.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(modulus, value);
  }
}
