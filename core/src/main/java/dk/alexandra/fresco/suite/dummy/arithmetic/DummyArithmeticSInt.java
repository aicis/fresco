/*******************************************************************************
 * Copyright (c) 2017 FRESCO (http://github.com/aicis/fresco).
 *
 * This file is part of the FRESCO project.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * FRESCO uses SCAPI - http://crypto.biu.ac.il/SCAPI, Crypto++, Miracl, NTL,
 * and Bouncy Castle. Please see these projects for any further licensing issues.
 *******************************************************************************/
package dk.alexandra.fresco.suite.dummy.arithmetic;

import java.math.BigInteger;

import dk.alexandra.fresco.framework.value.SInt;

public class DummyArithmeticSInt implements SInt {

  private static final long serialVersionUID = 5434286774741799773L;
  private BigInteger value;
  
  public DummyArithmeticSInt() {
    
  }
  
  public DummyArithmeticSInt(BigInteger value) {
    this.value = value;
  }
  
  public DummyArithmeticSInt(int value) {
    this.value = BigInteger.valueOf(value);
  }
  
  public BigInteger getValue() {
    return value;
  }
  
  public void setValue(BigInteger value) {
    this.value = value;
  }
  
  @Override
  public byte[] getSerializableContent() {
    return this.value.toByteArray();
  }

  @Override
  public void setSerializableContent(byte[] val) {
    this.value = new BigInteger(val);
  }

  @Override
  public boolean isReady() {
    return this.value != null;
  }

  @Override
  public String toString() {
    return "DummyArithmeticSInt [value=" + value + "]";
  }
}
