/*******************************************************************************
 * Copyright (c) 2015 FRESCO (http://github.com/aicis/fresco).
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
import dk.alexandra.fresco.framework.builder.ProtocolBuilderNumeric.SequentialProtocolBuilder;
import dk.alexandra.fresco.framework.value.SInt;
import java.util.ArrayList;

public class LPTableau4 {

  // The constraint matrix
  private final Matrix4<Computation<SInt>> C;
  // The rightmost column and bottom row  of the tableau, except for the last entry of both
  private final ArrayList<Computation<SInt>> B;
  private final ArrayList<Computation<SInt>> F;
  // The the bottom right hand corner entry of the tableau
  private final Computation<SInt> z;

  public LPTableau4(Matrix4<Computation<SInt>> C, ArrayList<Computation<SInt>> B,
      ArrayList<Computation<SInt>> F, Computation<SInt> z) {
    if (C.getWidth() == F.size() && C.getHeight() == B.size()) {
      this.C = C;
      this.B = B;
      this.F = F;
      this.z = z;
    } else {
      throw new RuntimeException("Dimenssions of tableau does not match");
    }
  }

  public Matrix4<Computation<SInt>> getC() {
    return C;
  }

  public ArrayList<Computation<SInt>> getB() {
    return B;
  }

  public ArrayList<Computation<SInt>> getF() {
    return F;
  }

  public Computation<SInt> getZ() {
    return z;
  }

  public void toString(SequentialProtocolBuilder builder) {
    builder.append(
        new OpenAndPrintProtocol("C: ", C.toArray(Computation::out, SInt[]::new, SInt[][]::new),
            builder.getBasicNumericFactory()));
    builder.append(
        new OpenAndPrintProtocol("B: ", B.stream().map(Computation::out).toArray(SInt[]::new),
            builder.getBasicNumericFactory()));
    builder.append(
        new OpenAndPrintProtocol("F: ", F.stream().map(Computation::out).toArray(SInt[]::new),
            builder.getBasicNumericFactory()));
    builder.append(new OpenAndPrintProtocol("z: ", z.out(), builder.getBasicNumericFactory()));
  }
}
