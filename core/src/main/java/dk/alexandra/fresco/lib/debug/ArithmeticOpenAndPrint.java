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
import dk.alexandra.fresco.framework.builder.ComputationBuilder;
import dk.alexandra.fresco.framework.builder.NumericBuilder;
import dk.alexandra.fresco.framework.builder.ProtocolBuilderNumeric.SequentialNumericBuilder;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.lp.Matrix;
import java.io.PrintStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ArithmeticOpenAndPrint implements ComputationBuilder<Void> {

  private Computation<SInt> number = null;
  private List<Computation<SInt>> vector = null;
  private Matrix<Computation<SInt>> matrix = null;
  private String label;
  private PrintStream stream;

  public ArithmeticOpenAndPrint(String label, Computation<SInt> number, PrintStream stream) {
    this.label = label;
    this.number = number;
    this.stream = stream;
  }

  public ArithmeticOpenAndPrint(String label, List<Computation<SInt>> vector, PrintStream stream) {
    this.label = label;
    this.vector = vector;
    this.stream = stream;
  }

  public ArithmeticOpenAndPrint(String label, Matrix<Computation<SInt>> matrix,
      PrintStream stream) {
    this.label = label;
    this.matrix = matrix;
    this.stream = stream;
  }

  @Override
  public Computation<Void> build(SequentialNumericBuilder builder) {
    return builder.seq(seq -> {
      NumericBuilder num = seq.numeric();
      List<Computation<BigInteger>> res = new ArrayList<>();
      if (number != null) {
        res.add(num.open(number));
      } else if (vector != null) {
        for (Computation<SInt> c : vector) {
          res.add(num.open(c));
        }
      } else {
        // matrix
        for (int i = 0; i < matrix.getHeight(); i++) {
          List<Computation<SInt>> l = matrix.getRow(i);
          for (Computation<SInt> c : l) {
            res.add(num.open(c));
          }
        }
      }
      return () -> res;
    }).seq((res, seq) -> {
      StringBuilder sb = new StringBuilder();
      sb.append(label);
      sb.append("\n");
      if (number != null) {
        sb.append(res.get(0).out());
      } else if (vector != null) {
        for (Computation<BigInteger> v : res) {
          sb.append(v.out() + ", ");
        }
      } else {
        Iterator<Computation<BigInteger>> it = res.iterator();
        for (int i = 0; i < this.matrix.getHeight(); i++) {
          for (int j = 0; j < this.matrix.getWidth(); j++) {
            sb.append(it.next().out() + ", ");
          }
          sb.append("\n");
        }
      }
      seq.utility().marker(sb.toString(), stream);
      return null;
    });
  }
  //
  // @Override
  // public void getNextProtocols(ProtocolCollection protocolCollection) {
  // if (pp == null) {
  // if (state == State.OUTPUT) {
  // if (number != null) {
  // openNumber = factory.getOpenProtocol(number);
  // pp = new SingleProtocolProducer<>(openNumber);
  // } else if (vector != null) {
  // openVector = new ArrayList<>();
  // pp = makeOpenProtocol(vector, openVector, factory);
  // } else {
  // openMatrix = new ArrayList<>();
  // pp = makeOpenProtocol(matrix, openMatrix, factory);
  // }
  // } else if (state == State.WRITE) {
  // StringBuilder sb = new StringBuilder();
  // sb.append(label);
  // if (openNumber != null) {
  // sb.append(openNumber.out().toString());
  // } else if (openVector != null) {
  // sb.append('\n');
  // for (Computation<BigInteger> entry : openVector) {
  // sb.append(entry.out().toString() + ", ");
  // }
  // } else if (openMatrix != null) {
  // sb.append('\n');
  // for (List<Computation<BigInteger>> row : openMatrix) {
  // for (Computation<BigInteger> entry : row) {
  // sb.append(entry.out().toString() + ", ");
  // }
  // sb.append('\n');
  // }
  // }
  // pp = new MarkerProtocolImpl(sb.toString(), null);
  // } else if (state == State.DONE) {
  // // TODO: This should really never occur as a state of DONE should give false in
  // // hasNextProtocols, but it does, find out why.
  // return;
  // }
  // }
  // if (pp.hasNextProtocols()) {
  // pp.getNextProtocols(protocolCollection);
  // } else if (!pp.hasNextProtocols()) {
  // switch (state) {
  // case OUTPUT:
  // state = State.WRITE;
  // pp = null;
  // break;
  // case WRITE:
  // state = State.DONE;
  // pp = null;
  // break;
  // default:
  // break;
  // }
  // }
  // }
}
