/*******************************************************************************
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
package dk.alexandra.fresco.lib.debug;

import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.OBool;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.lib.field.bool.BasicLogicFactory;
import dk.alexandra.fresco.lib.helper.SequentialProtocolProducer;
import dk.alexandra.fresco.lib.helper.SingleProtocolProducer;


public class BinaryOpenAndPrint implements ProtocolProducer {

  private SBool number = null;
  private SBool[] string = null;

  private OBool oNumber = null;
  private OBool[] oString = null;


  private enum STATE {OUTPUT, WRITE, DONE}

  ;
  private STATE state = STATE.OUTPUT;
  private String label;

  ProtocolProducer pp = null;

  private BasicLogicFactory factory;


  public BinaryOpenAndPrint(String label, SBool[] string, BasicLogicFactory factory) {
    this.string = string;
    this.factory = factory;
    this.label = label;
  }

  @Override
  public void getNextProtocols(ProtocolCollection protocolCollection) {
    if (pp == null) {
      if (state == STATE.OUTPUT) {
        if (number != null) {
          oNumber = factory.getOBool();
          pp = SingleProtocolProducer.wrap(factory.getOpenProtocol(number, oNumber));
        } else if (string != null) {
          oString = new OBool[string.length];
          SequentialProtocolProducer seq = new SequentialProtocolProducer();
          for (int i = 0; i < string.length; i++) {
            oString[i] = factory.getOBool();
            seq.append(factory.getOpenProtocol(string[i], oString[i]));
          }
          pp = seq;
        }
      } else if (state == STATE.WRITE) {
        StringBuilder sb = new StringBuilder();
        sb.append(label);
        if (oNumber != null) {
          sb.append(oNumber.getValue());
        } else if (oString != null) {
          sb.append('\n');
          for (OBool entry : oString) {
            if (entry.getValue()) {
              sb.append(1);
            } else {
              sb.append(0);
            }
          }
        }
        pp = new MarkerProtocolImpl(sb.toString());
      }
    }
    if (pp.hasNextProtocols()) {
      pp.getNextProtocols(protocolCollection);
    } else {
      switch (state) {
        case OUTPUT:
          state = STATE.WRITE;
          pp = null;
          break;
        case WRITE:
          state = STATE.DONE;
          pp = null;
          break;
        default:
          break;
      }
    }
  }

  @Override
  public boolean hasNextProtocols() {
    // TODO Auto-generated method stub
    return state != STATE.DONE;
  }
}
