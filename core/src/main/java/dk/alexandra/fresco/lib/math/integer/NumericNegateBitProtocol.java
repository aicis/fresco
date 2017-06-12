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
package dk.alexandra.fresco.lib.math.integer;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.helper.sequential.SequentialProtocolProducer;
import java.math.BigInteger;

/**
 * Assumes input is 0 or 1 and negates this by outputting 1-input.
 */
public class NumericNegateBitProtocol implements ProtocolProducer {

  private SInt bit, out;
  private BasicNumericFactory factory;
  private boolean done = false;
  private ProtocolProducer pp;

  public NumericNegateBitProtocol(SInt bit, SInt out, BasicNumericFactory factory) {
    this.bit = bit;
    this.out = out;
    this.factory = factory;
  }

  @Override
  public void getNextProtocols(ProtocolCollection protocolCollection) {
    if (pp == null) {
      OInt one = factory.getOInt(BigInteger.ONE);
      Computation<? extends SInt> sub = factory.getSubtractProtocol(one, bit, out);
      pp = new SequentialProtocolProducer(sub);
    }
    if (pp.hasNextProtocols()) {
      pp.getNextProtocols(protocolCollection);
    } else {
      pp = null;
      done = true;
    }
  }

  @Override
  public boolean hasNextProtocols() {
    return !done;
  }
}
