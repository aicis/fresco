
/*******************************************************************************
 * Copyright (c) 2017 FRESCO (http://github.com/aicis/fresco).
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
package dk.alexandra.fresco.suite.dummy.arithmetic;

import java.math.BigInteger;

import dk.alexandra.fresco.framework.network.SCENetwork;
import dk.alexandra.fresco.framework.network.serializers.BigIntegerSerializer;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.CloseIntProtocol;

public class DummyArithmeticCloseProtocol extends DummyArithmeticProtocol implements CloseIntProtocol{

  private int targetId;
  private DummyArithmeticOInt open;
  private DummyArithmeticSInt closed;
  
  public DummyArithmeticCloseProtocol(int targetId, OInt open, SInt closed) {
    super();
    this.targetId = targetId;
    this.open = (DummyArithmeticOInt) open;
    this.closed = (DummyArithmeticSInt) closed;
  }

  @Override
  public Object getOutputValues() {    
    return closed;
  }

  @Override
  public EvaluationStatus evaluate(int round, ResourcePool resourcePool, SCENetwork network) {
    if(round == 0) {
      if(targetId == resourcePool.getMyId()) {
        network.sendToAll(BigIntegerSerializer.toBytes(open.getValue()));
      }
      network.expectInputFromPlayer(targetId);
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else {
      BigInteger b = BigIntegerSerializer.toBigInteger(network.receive(targetId));
      this.closed.setValue(b);
      return EvaluationStatus.IS_DONE;
    }
    
  }

}
