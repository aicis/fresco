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
package dk.alexandra.fresco.lib.lp;

import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.debug.MarkerProtocolImpl;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.field.integer.OpenIntProtocol;
import dk.alexandra.fresco.lib.field.integer.generic.IOIntProtocolFactory;
import dk.alexandra.fresco.lib.helper.AlgebraUtil;
import dk.alexandra.fresco.lib.helper.ParallelProtocolProducer;
import dk.alexandra.fresco.lib.helper.SingleProtocolProducer;

public class OpenAndPrintProtocol implements ProtocolProducer {

  private SInt number = null;
  private SInt[] vector = null;
  private SInt[][] matrix = null;

  private OInt oNumber = null;
  private OInt[] oVector = null;
  private OInt[][] oMatrix = null;

  private enum STATE {OUTPUT, WRITE, DONE}

  ;
  private STATE state = STATE.OUTPUT;
  private String label;

  ProtocolProducer pp = null;

  private BasicNumericFactory factory;


  public OpenAndPrintProtocol(String label, SInt number, BasicNumericFactory factory) {
    this.number = number;
    this.factory = factory;
    this.label = label;
  }

  public OpenAndPrintProtocol(String label, SInt[] vector, BasicNumericFactory factory) {
    this.vector = vector;
    this.factory = factory;
    this.label = label;
  }

  public OpenAndPrintProtocol(String label, SInt[][] matrix, BasicNumericFactory factory) {
    this.matrix = matrix;
    this.factory = factory;
    this.label = label;
  }

  @Override
  public void getNextProtocols(ProtocolCollection protocolCollection) {
    if (pp == null) {
      if (state == STATE.OUTPUT) {
        if (number != null) {
          oNumber = factory.getOInt();
          pp = SingleProtocolProducer.wrap(factory.getOpenProtocol(number, oNumber));
        } else if (vector != null) {
          oVector = AlgebraUtil.oIntFill(new OInt[vector.length], factory);
          pp = makeOpenProtocol(vector, oVector, factory);
        } else {
          oMatrix = AlgebraUtil.oIntFill(new OInt[matrix.length][matrix[0].length], factory);
          pp = makeOpenProtocol(matrix, oMatrix, factory);
        }
      } else if (state == STATE.WRITE) {
        StringBuilder sb = new StringBuilder();
        sb.append(label);
        if (oNumber != null) {
          sb.append(oNumber.getValue().toString());
        } else if (oVector != null) {
          sb.append('\n');
          for (OInt entry : oVector) {
            sb.append(entry.getValue().toString() + ",\t");
          }
        } else if (oMatrix != null) {
          sb.append('\n');
          for (OInt[] row : oMatrix) {
            for (OInt entry : row) {
              sb.append(entry.getValue().toString() + "," + "\t");
            }
            sb.append('\n');
          }
        }
        pp = new MarkerProtocolImpl(sb.toString(), null);
      }
    }
    if (pp.hasNextProtocols()) {
      pp.getNextProtocols(protocolCollection);
    } else if (!pp.hasNextProtocols()) {
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

  ProtocolProducer makeOpenProtocol(SInt[][] closed, OInt[][] open, IOIntProtocolFactory factory) {
    if (open.length != closed.length) {
      throw new IllegalArgumentException("Amount of closed and open integers does not match. " +
          "Open: " + open.length + " Closed: " + closed.length);
    }
    ProtocolProducer[] openings = new ProtocolProducer[open.length];
    for (int i = 0; i < open.length; i++) {
      openings[i] = makeOpenProtocol(closed[i], open[i], factory);
    }
    return new ParallelProtocolProducer(openings);
  }


  ProtocolProducer makeOpenProtocol(SInt[] closed, OInt[] open, IOIntProtocolFactory factory) {
    if (open.length != closed.length) {
      throw new IllegalArgumentException("Amount of closed and open integers does not match. " +
          "Open: " + open.length + " Closed: " + closed.length);
    }
    OpenIntProtocol[] openings = new OpenIntProtocol[open.length];
    for (int i = 0; i < open.length; i++) {
      openings[i] = factory.getOpenProtocol(closed[i], open[i]);
    }
    return new ParallelProtocolProducer(openings);
  }

  @Override
  public boolean hasNextProtocols() {
    // TODO Auto-generated method stub
    return state != STATE.DONE;
  }

}
