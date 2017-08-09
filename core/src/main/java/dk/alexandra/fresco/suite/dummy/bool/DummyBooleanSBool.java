/*******************************************************************************
 * Copyright (c) 2017 FRESCO (http://github.com/aicis/fresco).
 *
 * This file is part of the FRESCO project.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * FRESCO uses SCAPI - http://crypto.biu.ac.il/SCAPI, Crypto++, Miracl, NTL, and Bouncy Castle.
 * Please see these projects for any further licensing issues.
 *******************************************************************************/

package dk.alexandra.fresco.suite.dummy.bool;

import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;

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
