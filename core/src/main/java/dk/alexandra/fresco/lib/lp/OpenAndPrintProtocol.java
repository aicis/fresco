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

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.debug.MarkerProtocolImpl;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.field.integer.generic.IOIntProtocolFactory;
import dk.alexandra.fresco.lib.helper.ParallelProtocolProducer;
import dk.alexandra.fresco.lib.helper.SingleProtocolProducer;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class OpenAndPrintProtocol implements ProtocolProducer {

  private SInt number = null;
  private SInt[] vector = null;
  private SInt[][] matrix = null;

  private List<Computation<BigInteger>> oVector = null;
  private List<List<Computation<BigInteger>>> oMatrix = null;
  private Computation<BigInteger> oNumber;

  public OpenAndPrintProtocol(String s,
      List<Computation<SInt>> comps, BasicNumericFactory bnFactory) {
    this(s, comps.stream().map(Computation::out).toArray(SInt[]::new), bnFactory);
  }

  private enum STATE {OUTPUT, WRITE, DONE}

  ;
  private STATE state = STATE.OUTPUT;
  private String label;

  ProtocolProducer pp = null;

  private BasicNumericFactory factory;


  public OpenAndPrintProtocol(String label, SInt number, BasicNumericFactory factory) {
    Objects.requireNonNull(number);
    this.number = number;
    this.factory = factory;
    this.label = label;
  }

  public OpenAndPrintProtocol(String label, SInt[] vector, BasicNumericFactory factory) {
    Objects.requireNonNull(vector);
    this.vector = vector;
    this.factory = factory;
    this.label = label;
  }

  public OpenAndPrintProtocol(String label, SInt[][] matrix, BasicNumericFactory factory) {
    Objects.requireNonNull(matrix);
    this.matrix = matrix;
    this.factory = factory;
    this.label = label;
  }

  @Override
  public void getNextProtocols(ProtocolCollection protocolCollection) {
    if (pp == null) {
      if (state == STATE.OUTPUT) {
        if (number != null) {
          oNumber = factory.getOpenProtocol(number);
          pp = SingleProtocolProducer.wrap(oNumber);
        } else if (vector != null) {
          oVector = new ArrayList<>();
          pp = makeOpenProtocol(vector, oVector, factory);
        } else {
          oMatrix = new ArrayList<>();
          pp = makeOpenProtocol(matrix, oMatrix, factory);
        }
      } else if (state == STATE.WRITE) {
        StringBuilder sb = new StringBuilder();
        sb.append(label);
        if (oNumber != null) {
          sb.append(oNumber.toString());
        } else if (oVector != null) {
          sb.append('\n');
          for (Computation<BigInteger> entry : oVector) {
            sb.append(entry.out().toString() + ",\t");
          }
        } else if (oMatrix != null) {
          sb.append('\n');
          for (List<Computation<BigInteger>> row : oMatrix) {
            for (Computation<BigInteger> entry : row) {
              sb.append(entry.out().toString() + "," + "\t");
            }
            sb.append('\n');
          }
        }
        pp = new MarkerProtocolImpl(sb.toString());
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

  ProtocolProducer makeOpenProtocol(SInt[][] closed, List<List<Computation<BigInteger>>> open,
      IOIntProtocolFactory factory) {
    ProtocolProducer[] openings = new ProtocolProducer[closed.length];
    for (int i = 0; i < closed.length; i++) {
      ArrayList<Computation<BigInteger>> columnResult = new ArrayList<>();
      open.add(columnResult);
      openings[i] = makeOpenProtocol(closed[i], columnResult, factory);
    }
    return new ParallelProtocolProducer(openings);
  }


  ProtocolProducer makeOpenProtocol(SInt[] closed, List<Computation<BigInteger>> result,
      IOIntProtocolFactory factory) {
    ParallelProtocolProducer parallelProtocolProducer = new ParallelProtocolProducer();
    for (int i = 0; i < closed.length; i++) {
      Computation<BigInteger> openProtocol = factory.getOpenProtocol(closed[i]);
      result.add(openProtocol);
      parallelProtocolProducer.append(openProtocol);
    }
    return parallelProtocolProducer;
  }

  @Override
  public boolean hasNextProtocols() {
    return state != STATE.DONE;
  }

}
