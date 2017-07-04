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

import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.OBool;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.framework.value.SBoolFactory;
import dk.alexandra.fresco.lib.field.bool.AndProtocol;
import dk.alexandra.fresco.lib.helper.CopyProtocol;

public class AndFromCopyConstProtocol implements AndProtocol, ProtocolProducer {

  private NativeProtocol copyCir;
  private SBoolFactory sboolFactory;
  private SBool inLeft;
  private OBool inRight;
  private SBool out;

  public AndFromCopyConstProtocol(SBoolFactory sboolFactory,
      SBool inLeft, OBool inRight, SBool out) {
    this.sboolFactory = sboolFactory;
    this.inLeft = inLeft;
    this.inRight = inRight;
    this.out = out;
  }

  @Override
  public void getNextProtocols(ProtocolCollection protocolCollection) {
    if (copyCir == null) {
      if (inRight.getValue()) {
        copyCir = new CopyProtocol<>(inLeft, out);
      } else {
        copyCir = new CopyProtocol<>(sboolFactory.getKnownConstantSBool(false), out);
      }
    }
    protocolCollection.addProtocol(copyCir);
  }

  @Override
  public boolean hasNextProtocols() {
    return copyCir == null;
  }

}
