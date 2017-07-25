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
package dk.alexandra.fresco.lib.field.bool.generic;

import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.lib.field.bool.BasicLogicFactory;
import dk.alexandra.fresco.lib.field.bool.XnorProtocol;
import dk.alexandra.fresco.lib.helper.SingleProtocolProducer;

public class XnorFromXorAndNotProtocolImpl implements XnorProtocol {

  private SBool left;
  private SBool right;
  private SBool out;
  private BasicLogicFactory factory;
  private ProtocolProducer curPP = null;
  private boolean done = false;
  private boolean xorDone = false;
  private SBool tmpOut;

  public XnorFromXorAndNotProtocolImpl(SBool left, SBool right, SBool out,
      BasicLogicFactory factory) {
    this.left = left;
    this.right = right;
    this.out = out;
    this.factory = factory;
  }

  @Override
  public void getNextProtocols(ProtocolCollection protocolCollection) {
    if (curPP == null) {
      tmpOut = factory.getSBool();
      curPP = SingleProtocolProducer.wrap(factory.getXorProtocol(left, right, tmpOut));
      curPP.getNextProtocols(protocolCollection);
    } else {
      if (!xorDone) {
        curPP = factory.getNotProtocol(tmpOut, out);
        xorDone = true;
        curPP.getNextProtocols(protocolCollection);
      } else {
        done = true;
      }
    }
  }

  @Override
  public boolean hasNextProtocols() {
    return !done;
  }
}
