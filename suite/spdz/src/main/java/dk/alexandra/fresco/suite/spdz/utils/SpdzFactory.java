/*
 * Copyright (c) 2015, 2016 FRESCO (http://github.com/aicis/fresco).
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
package dk.alexandra.fresco.suite.spdz.utils;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzElement;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.gates.SpdzAddProtocolOld;
import dk.alexandra.fresco.suite.spdz.gates.SpdzInputProtocol;
import dk.alexandra.fresco.suite.spdz.gates.SpdzKnownSIntProtocol;
import dk.alexandra.fresco.suite.spdz.gates.SpdzMultProtocolOld;
import dk.alexandra.fresco.suite.spdz.gates.SpdzOutputProtocol;
import dk.alexandra.fresco.suite.spdz.gates.SpdzOutputToAllProtocolOld;
import dk.alexandra.fresco.suite.spdz.gates.SpdzSubtractProtocolOld;
import dk.alexandra.fresco.suite.spdz.storage.SpdzStorage;
import java.math.BigInteger;

public class SpdzFactory implements BasicNumericFactory {

  private int maxBitLength;
  private SpdzStorage storage;
  private int pID;

  /**
   * @param maxBitLength The maximum length in bits that the numbers in the application will have.
   * If you have greater knowledge of your application, you can create several factorys, each with a
   * different maxBitLength to increase performance.
   */
  public SpdzFactory(SpdzStorage storage, int pID, int maxBitLength) {
    this.maxBitLength = maxBitLength;
    this.storage = storage;
    this.pID = pID;
  }

  @Override
  public SpdzSInt getSInt() {
    return new SpdzSInt();
  }

  /**
   * Careful - This creates a publicly known integer which is secret shared.
   */
  @Override
  public Computation<SInt> getSInt(BigInteger value, SInt sValue) {
    return new SpdzKnownSIntProtocol(value, sValue);
  }

  public SpdzSInt getRandomBitFromStorage() {
    return this.storage.getSupplier().getNextBit();
  }

  public SInt[] getExponentiationPipe() {
    return this.storage.getSupplier().getNextExpPipe();
  }

  @Override
  public NativeProtocol<SInt, ?> getAddProtocol(SInt a, SInt b, SInt out) {
    return new SpdzAddProtocolOld(a, b, out);
  }


  @Override
  public NativeProtocol<SInt, ?> getAddProtocol(SInt a, BigInteger b, SInt out) {
    return new SpdzAddProtocolOld(a, b, out, this);
  }

  @Override
  public NativeProtocol<SInt, ?> getSubtractProtocol(SInt a, SInt b, SInt out) {
    return new SpdzSubtractProtocolOld(a, b, out);
  }


  @Override
  public NativeProtocol<SInt, ?> getMultProtocol(SInt a, SInt b, SInt out) {
    return new SpdzMultProtocolOld(a, b, out);
  }

  @Override
  public int getMaxBitLength() {
    return this.maxBitLength;
  }

  @Override
  @Deprecated
  public SInt getSInt(int i) {

    BigInteger b = BigInteger.valueOf(i).mod(getModulus());
    SpdzElement elm;
    if (pID == 1) {
      elm = new SpdzElement(b, b.multiply(this.storage.getSSK()).mod(getModulus()),
          getModulus());
    } else {
      elm = new SpdzElement(BigInteger.ZERO, b.multiply(this.storage
          .getSSK()).mod(getModulus()), getModulus());
    }
    return new SpdzSInt(elm);
  }

  @Override
  @Deprecated
  public SpdzSInt getSInt(BigInteger b) {
    b = b.mod(getModulus());
    SpdzElement elm;
    if (pID == 1) {
      elm = new SpdzElement(b, b.multiply(this.storage.getSSK()).mod(getModulus()),
          getModulus());
    } else {
      elm = new SpdzElement(BigInteger.ZERO, b.multiply(this.storage
          .getSSK()).mod(getModulus()), getModulus());
    }
    return new SpdzSInt(elm);
  }


  @Override
  public NativeProtocol<SInt, ?> getCloseProtocol(int source, BigInteger open, SInt closed) {
    return new SpdzInputProtocol(open, closed, source);
  }

  @Override
  public NativeProtocol<BigInteger, ?> getOpenProtocol(int target, SInt closed) {
    return new SpdzOutputProtocol(closed, target);
  }

  @Override
  public NativeProtocol<BigInteger, ?> getOpenProtocol(SInt closed) {
    return new SpdzOutputToAllProtocolOld(closed);
  }

  @Override
  public BigInteger getModulus() {
    return this.storage.getSupplier().getModulus();
  }

}
