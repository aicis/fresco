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

  private Boolean value;

  /**
   * Constructs an SBool with value <code>null</code>.
   */
  public DummyBooleanSBool() {
    this.value = null;
  }

  /**
   * Constructs an SBool with a given value.
   * 
   * @param value the given value
   */
  public DummyBooleanSBool(Boolean value) {
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

  /**
   * Sets the value of this SBool.
   * 
   * @param value the value to set.
   */
  public void setValue(Boolean value) {
    this.value = value;
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
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    DummyBooleanSBool other = (DummyBooleanSBool) obj;
    if (value == null) {
      if (other.value != null)
        return false;
    } else if (!value.equals(other.value))
      return false;
    return true;
  }
}
