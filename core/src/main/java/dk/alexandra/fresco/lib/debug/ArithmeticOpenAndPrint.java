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

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.collections.Matrix;
import java.io.PrintStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * <b>NB: Use with caution as using this class will open values to all MPC parties.</b>
 * 
 * This class opens a number for debugging purposes and prints a message along with the revealed
 * value(s).
 * 
 */
public class ArithmeticOpenAndPrint implements Computation<Void, ProtocolBuilderNumeric> {

  private DRes<SInt> number = null;
  private List<DRes<SInt>> vector = null;
  private Matrix<DRes<SInt>> matrix = null;
  private String label;
  private PrintStream stream;

  public ArithmeticOpenAndPrint(String label, DRes<SInt> number, PrintStream stream) {
    this.label = label;
    this.number = number;
    this.stream = stream;
  }

  public ArithmeticOpenAndPrint(String label, List<DRes<SInt>> vector, PrintStream stream) {
    this.label = label;
    this.vector = vector;
    this.stream = stream;
  }

  public ArithmeticOpenAndPrint(String label, Matrix<DRes<SInt>> matrix,
      PrintStream stream) {
    this.label = label;
    this.matrix = matrix;
    this.stream = stream;
  }

  @Override
  public DRes<Void> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.seq(seq -> {
      Numeric num = seq.numeric();
      List<DRes<BigInteger>> res = new ArrayList<>();
      if (number != null) {
        res.add(num.open(number));
      } else if (vector != null) {
        for (DRes<SInt> c : vector) {
          res.add(num.open(c));
        }
      } else {
        // matrix
        for (int i = 0; i < matrix.getHeight(); i++) {
          List<DRes<SInt>> l = matrix.getRow(i);
          for (DRes<SInt> c : l) {
            res.add(num.open(c));
          }
        }
      }
      return () -> res;
    }).seq((seq, res) -> {
      StringBuilder sb = new StringBuilder();
      sb.append(label);
      sb.append("\n");
      if (number != null) {
        sb.append(res.get(0).out());
      } else if (vector != null) {
        for (DRes<BigInteger> v : res) {
          sb.append(v.out() + ", ");
        }
      } else {
        Iterator<DRes<BigInteger>> it = res.iterator();
        for (int i = 0; i < this.matrix.getHeight(); i++) {
          for (int j = 0; j < this.matrix.getWidth(); j++) {
            sb.append(it.next().out() + ", ");
          }
          sb.append("\n");
        }
      }
      seq.debug().marker(sb.toString(), stream);
      return null;
    });
  }
}
