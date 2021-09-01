package dk.alexandra.fresco.suite.dummy.arithmetic;

import dk.alexandra.fresco.framework.builder.numeric.field.FieldElement;
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

  private final FieldElement value;

  /**
   * Constructs an SInt with a given value.
   *
   * @param value the given value
   */
  public DummyArithmeticSInt(FieldElement value) {
    this.value = Objects.requireNonNull(value);
  }

  /**
   * Gets the value of this SInt.
   *
   * @return the value
   */
  @Override
  public FieldElement getShare() {
    return value;
  }

  @Override
  public FieldElement getMac() {
    return null;
  }

  @Override
  public String toString() {
    return "DummyArithmeticSInt [value=" + value + "]";
  }

  @Override
  public SInt out() {
    return this;
  }
}
