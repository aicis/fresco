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
 * Implements logical AND for the Dummy Boolean protocol suite, where all operations are done in the
 * clear.
 *
 */
public class DummyBooleanAndProtocol extends DummyBooleanNativeProtocol<SBool> {

  private Computation<SBool> left;
  private Computation<SBool> right;
  private DummyBooleanSBool out;

  /**
   * Constructs a protocol to AND the result of two computations.
   * 
   * @param left the left operand
   * @param right the right operand
   */
  public DummyBooleanAndProtocol(Computation<SBool> left, Computation<SBool> right) {
    super();
    this.left = left;
    this.right = right;
    this.out = null;
  }

  /**
   * Constructs a protocol to AND the result of two computations.
   * 
   * <p>
   * Lets the caller specify where to store the output. This is for backward compatibility.
   * </p>
   * 
   * @param left the left operand
   * @param right the right operand
   * @param out the {@link SBool} in which to store the output
   */
  public DummyBooleanAndProtocol(Computation<SBool> left, Computation<SBool> right, SBool out) {
    super();
    this.left = left;
    this.right = right;
    this.out = (DummyBooleanSBool) out;
  }

  @Override
  public EvaluationStatus evaluate(int round, ResourcePool resourcePool,
      SCENetwork network) {
    
    out = (out == null) ? new DummyBooleanSBool() : out;
    this.out.setValue(((DummyBooleanSBool)left.out()).getValue() & ((DummyBooleanSBool)right.out()).getValue());
    return EvaluationStatus.IS_DONE;
  }

  @Override
  public SBool out() {
    return out;
  }
}
