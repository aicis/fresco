package dk.alexandra.fresco.suite.dummy.arithmetic;

import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;

/**
 * Implements {@link SInt} for the Dummy Arithmetic suite.
 * 
 * <p>
 * As the Dummy Arithmetic suite does not do actual MPC, but does all work locally, this is just a
 * wrapper around the {@link BigInteger} class.
 * </p>
 *
 */
public class DummyArithmeticSInt implements SInt {

  private final BigInteger value;

  /**
   * Constructs an SInt with a given value.
   * 
   * @param value the given value
   */
  public DummyArithmeticSInt(BigInteger value) {
    this.value = value;
  }

  /**
   * Constructs an SInt with a given value.
   * 
   * @param value the given value
   */
  public DummyArithmeticSInt(int value) {
    this.value = BigInteger.valueOf(value);
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
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((value == null) ? 0 : value.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    DummyArithmeticSInt other = (DummyArithmeticSInt) obj;
    if (value == null) {
      if (other.value != null)
        return false;
    } else if (!value.equals(other.value))
      return false;
    return true;
  }


}
