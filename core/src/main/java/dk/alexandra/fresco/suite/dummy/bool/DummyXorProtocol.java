/*
 * Copyright (c) 2015 FRESCO (http://github.com/aicis/fresco).
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
package dk.alexandra.fresco.suite.dummy.bool;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.network.SCENetwork;
import dk.alexandra.fresco.framework.sce.resources.ResourcePoolImpl;
import dk.alexandra.fresco.framework.value.SBool;


public class DummyXorProtocol extends DummyNativeProtocol<SBool> {

  private final Computation<SBool> inA;
  private final Computation<SBool> inB;
  private DummySBool outC;

  public DummyXorProtocol(Computation<SBool> inA, Computation<SBool> inB) {
    this.inA = inA;
    this.inB = inB;
  }

  @Override
  public String toString() {
    return "DummyXorGate(" + inA + "," + inB + "," + outC + ")";
  }

  @Override
  public SBool out() {
    return outC;
  }

  @Override
  public EvaluationStatus evaluate(int round, ResourcePoolImpl resourcePool, SCENetwork network) {
    this.outC = new DummySBool();
    this.outC.setValue(
        ((DummySBool) this.inA.out()).getValue() ^ ((DummySBool) this.inB.out()).getValue());
    return EvaluationStatus.IS_DONE;
  }
}
