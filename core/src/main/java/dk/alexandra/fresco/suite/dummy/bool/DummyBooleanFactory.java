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
package dk.alexandra.fresco.suite.dummy.bool;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.lib.field.bool.BasicLogicFactory;
import dk.alexandra.fresco.lib.field.bool.XorProtocol;


//Class is no longer maintained and should be deleted when were migrated to new architecture
@Deprecated
public class DummyBooleanFactory implements BasicLogicFactory {

  /**
   * For unique names to values. For debugging.
   */
  private int counter;

  public DummyBooleanFactory() {
    this.counter = 0;
  }

  @Override
  public SBool getSBool() {
    return new DummySBool("" + this.counter++);
  }

  @Override
  public SBool getKnownConstantSBool(boolean b) {
    return new DummySBool("" + this.counter++, b);
  }
  
/*  @Override
  public OBool getOBool() {
    return new DummyOBool("" + this.counter++);
  }

  @Override
  public OBool getKnownConstantOBool(boolean b) {
    return new DummyOBool("" + this.counter++, b);
  }
*/
  @Override
  public XorProtocol getXorProtocol(SBool inLeft, SBool inRight, SBool out) {
    return new DummyXorProtocol(inLeft, inRight, out);
  }

  /*@Override
  public ProtocolProducer getAndProtocol(SBool inLeft, SBool inRight, SBool out) {
    return SingleProtocolProducer.wrap(new DummyAndProtocol(inLeft, inRight, out));
  }

  @Override
  public CloseBoolProtocol getCloseProtocol(int source, OBool open, SBool closed) {
    return new DummyCloseBoolProtocol(open, closed, source);
  }

  @Override
  public OpenBoolProtocol getOpenProtocol(SBool closed, OBool open) {
    return new DummyOpenBoolProtocol(closed, open);
  }

  @Override
  public OpenBoolProtocol getOpenProtocol(int target, SBool closed, OBool open) {
    return new DummyOpenBoolProtocol(closed, open, target);
  }

  @Override
  public XorProtocol getXorProtocol(SBool inLeft, OBool inRight, SBool out) {
    SBool dummy = new DummySBool(((DummyOBool)inRight).getId(), inRight.getValue());
    return new DummyXorProtocol(inLeft, dummy, out);
  }
*/
  @Override
  public Computation<? extends SBool> getCloseProtocol(int source, Boolean open, SBool closed) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Computation<Boolean> getOpenProtocol(SBool closed) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Computation<Boolean> getOpenProtocol(int target, SBool closed) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Computation<SBool> getSBool(Boolean input, SBool output) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Computation<? extends SBool> getAndProtocol(SBool inLeft, SBool inRight, SBool out) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Computation<? extends SBool> getAndProtocol(SBool inLeft, Boolean inRight, SBool out) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Computation<? extends SBool> getXorProtocol(SBool inLeft, Boolean inRight, SBool out) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public SBool[] getSBools(int amount) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public SBool[] getKnownConstantSBools(boolean[] bools) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Computation<? extends SBool> getNotProtocol(SBool in, SBool out) {
    // TODO Auto-generated method stub
    return null;
  }

}
