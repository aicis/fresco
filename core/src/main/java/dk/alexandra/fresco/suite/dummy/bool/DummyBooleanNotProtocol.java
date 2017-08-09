/*******************************************************************************
 * Copyright (c) 2017 FRESCO (http://github.com/aicis/fresco).
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
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.SBool;

/**
 * Implements logical NOT for the Dummy Boolean protocol suite, where all operations are done in the
 * clear.
 *
 */
public class DummyBooleanNotProtocol extends DummyBooleanNativeProtocol<SBool> {

  private Computation<SBool> operand;
  private DummyBooleanSBool out;

  /**
   * Constructs a protocol to NOT the result of a computation.
   * 
   * @param operand the operand
   */
  public DummyBooleanNotProtocol(Computation<SBool> operand) {
    super();
    this.operand = operand;
    this.out = null;
  }

  /**
   * Constructs a protocol to NOT the result of a computation.
   * 
   * <p>
   * Lets the caller specify where to store the output. This is for backward compatibility.
   * </p>
   * 
   * @param operand the left operand
   * @param out the {@link SBool} in which to store the output
   */
  public DummyBooleanNotProtocol(Computation<SBool> operand, SBool out) {
    super();
    this.operand = operand;
    this.out = (DummyBooleanSBool) out;
  }

  @Override
  public EvaluationStatus evaluate(int round, ResourcePool resourcePool,
      SCENetwork network) {
    
    out = (out == null) ? new DummyBooleanSBool() : out;
    this.out.setValue(!((DummyBooleanSBool)operand.out()).getValue());
    return EvaluationStatus.IS_DONE;
  }

  @Override
  public SBool out() {
    return out;
  }
}
