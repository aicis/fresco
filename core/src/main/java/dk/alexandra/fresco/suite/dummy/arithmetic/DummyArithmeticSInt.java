package dk.alexandra.fresco.suite.dummy.arithmetic;

import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;
import java.util.Objects;

/**
 * Implements {@link SInt} for the Dummy Arithmetic suite.
 *
 * <p>
 * As the Dummy Arithmetic suite does not do actual MPC, but does all work locally, this is just a
 * wrapper around the {@link BigInteger} class.
 * </p>
 */
public class DummyArithmeticSInt implements SInt {

  private final BigInteger value;

  /**
   * Constructs an SInt with a given value.
   *
   * @param value the given value
   */
  public DummyArithmeticSInt(BigInteger value) {
    this.value = Objects.requireNonNull(value);
  }

  /**
   * Constructs an SInt with a given value.
   *
   * @param value the given value
   */
  public DummyArithmeticSInt(int value) {
    this(BigInteger.valueOf(value));
  }

  /**
   * Gets the value of this SInt.
   *
   * @return the value
   */
  public BigInteger getValue() {
    return value;
  }

  @Override
  public String toString() {
    return "DummyArithmeticSInt [value=" + value + "]";
  }

  @Override
  public SInt out() {
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    DummyArithmeticSInt that = (DummyArithmeticSInt) o;

    return value.equals(that.value);
  }

  @Override
  public int hashCode() {
    return value.hashCode();
  }
}
