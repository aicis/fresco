/*
 * Copyright (c) 2015, 2016 FRESCO (http://github.com/aicis/fresco).
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
package dk.alexandra.fresco.lib.debug;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.field.integer.generic.IOIntProtocolFactory;
import dk.alexandra.fresco.lib.helper.ParallelProtocolProducer;
import dk.alexandra.fresco.lib.helper.SingleProtocolProducer;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

//TODO refactor into new architecture
public class ArithmeticOpenAndPrint implements ProtocolProducer {

  private SInt number = null;
  private SInt[] vector = null;
  private SInt[][] matrix = null;

  private List<Computation<BigInteger>> openVector = null;
  private List<List<Computation<BigInteger>>> openMatrix = null;
  private Computation<BigInteger> openNumber;

  public ArithmeticOpenAndPrint(String s, List<Computation<SInt>> comps,
      BasicNumericFactory bnFactory) {
    this(s, comps.stream().map(Computation::out).toArray(SInt[]::new), bnFactory);
  }

  private enum State { OUTPUT, WRITE, DONE }
  
  private State state = State.OUTPUT;
  private String label;

  ProtocolProducer pp = null;

  private BasicNumericFactory factory;
 
  /**
   * Prints a single number.
   * @param label label identify the print out 
   * @param number the number to print
   * @param factory a basic numeric factory
   */
  public ArithmeticOpenAndPrint(String label, SInt number, BasicNumericFactory factory) {
    Objects.requireNonNull(number);
    this.number = number;
    this.factory = factory;
    this.label = label;
  }

  /**
   * Prints a vector.
   * @param label label identify the print out 
   * @param vector the vector to print
   * @param factory a basic numeric factory
   */
  public ArithmeticOpenAndPrint(String label, SInt[] vector, BasicNumericFactory factory) {
    Objects.requireNonNull(vector);
    this.vector = vector;
    this.factory = factory;
    this.label = label;
  }

  /**
   * Prints a matrix number.
   * @param label label identify the print out 
   * @param matrix the matrix to print
   * @param factory a basic numeric factory
   */
  public ArithmeticOpenAndPrint(String label, SInt[][] matrix, BasicNumericFactory factory) {
    Objects.requireNonNull(matrix);
    this.matrix = matrix;
    this.factory = factory;
    this.label = label;
  }

  @Override
  public void getNextProtocols(ProtocolCollection protocolCollection) {
    if (pp == null) {
      if (state == State.OUTPUT) {
        if (number != null) {
          openNumber = factory.getOpenProtocol(number);
          pp = SingleProtocolProducer.wrap(openNumber);
        } else if (vector != null) {
          openVector = new ArrayList<>();
          pp = makeOpenProtocol(vector, openVector, factory);
        } else {
          openMatrix = new ArrayList<>();
          pp = makeOpenProtocol(matrix, openMatrix, factory);
        }
      } else if (state == State.WRITE) {
        StringBuilder sb = new StringBuilder();
        sb.append(label);
        if (openNumber != null) {
          sb.append(openNumber.out().toString());
        } else if (openVector != null) {
          sb.append('\n');
          for (Computation<BigInteger> entry : openVector) {
            sb.append(entry.out().toString() + ", ");
          }
        } else if (openMatrix != null) {
          sb.append('\n');
          for (List<Computation<BigInteger>> row : openMatrix) {
            for (Computation<BigInteger> entry : row) {
              sb.append(entry.out().toString() + ",  ");
            }
            sb.append('\n');
          }
        }
        pp = new MarkerProtocolImpl(sb.toString(), null);
      } else if (state == State.DONE) {
        // TODO: This should really never occur as a state of DONE should give false in
        // hasNextProtocols, but it does, find out why.
        return;
      }
    }
    if (pp.hasNextProtocols()) {
      pp.getNextProtocols(protocolCollection);
    } else if (!pp.hasNextProtocols()) {
      switch (state) {
        case OUTPUT:
          state = State.WRITE;
          pp = null;
          break;
        case WRITE:
          state = State.DONE;
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
    return state != State.DONE;
  }

}
