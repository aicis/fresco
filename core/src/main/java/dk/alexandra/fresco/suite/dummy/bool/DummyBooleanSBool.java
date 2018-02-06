package dk.alexandra.fresco.suite.dummy.bool;

import dk.alexandra.fresco.framework.value.SBool;

/**
 * Implements {@link SBool} for the Dummy Boolean suite.
 * 
 * <p>
 * As the Dummy Boolean suite does not do actual MPC, but does all work locally, this is just a
 * wrapper around the {@link Boolean} class.
 * </p>
 *
 */
public class DummyBooleanSBool implements SBool {

  private final boolean value;

  /**
   * Constructs an SBool with a given value.
   * 
   * @param value the given value
   */
  public DummyBooleanSBool(boolean value) {
    this.value = value;
  }

  /**
   * Gets the value of this SBool.
   * 
   * @return the value
   */
  public Boolean getValue() {
    return value;
  }

  @Override
  public String toString() {
    return "DummyBooleanSBool [value=" + value + "]";
  }

  @Override
  public SBool out() {
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

    DummyBooleanSBool that = (DummyBooleanSBool) o;

    return value == that.value;
  }

  @Override
  public int hashCode() {
    return (value ? 1 : 0);
  }
}
