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

  private BigInteger value;

  /**
   * Constructs an SInt with value <code>null</code>.
   */
  public DummyArithmeticSInt() {
    this.value = null;
  }

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

  /**
   * Sets the value of this SInt.
   * 
   * @param value the value to set.
   */
  public void setValue(BigInteger value) {
    this.value = value;
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
