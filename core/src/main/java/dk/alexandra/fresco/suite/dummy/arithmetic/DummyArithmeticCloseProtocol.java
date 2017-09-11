
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

package dk.alexandra.fresco.suite.dummy.arithmetic;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.SCENetwork;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;

/**
 * Implements closing a value in the Dummy Arithmetic suite where all operations are done in the
 * clear. I.e., this really does nothing but send the open value to the other parties so they can
 * compute on it.
 *
 */
public class DummyArithmeticCloseProtocol extends DummyArithmeticNativeProtocol<SInt> {

  private int targetId;
  private DRes<BigInteger> open;
  private DummyArithmeticSInt closed;

  /**
   * Constructs a protocol to close an open value.
   *
   * @param targetId id of the party supplying the open value.
   * @param open a computation output the value to close.
   */
  public DummyArithmeticCloseProtocol(int targetId, DRes<BigInteger> open) {
    this.targetId = targetId;
    this.open = open;
  }

  @Override
  public EvaluationStatus evaluate(int round, DummyArithmeticResourcePool rp, SCENetwork network) {
    if (round == 0) {
      if (targetId == rp.getMyId()) {
        network.sendToAll(rp.getSerializer().toBytes((open.out())));
      }
      network.expectInputFromPlayer(targetId);
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else if (round == 1) {
      BigInteger b = rp.getSerializer().toBigInteger(network.receive(targetId));
      closed = new DummyArithmeticSInt(b);
      return EvaluationStatus.IS_DONE;
    } else {
      throw new IllegalStateException("No round " + round);
    }
  }

  @Override
  public SInt out() {
    return closed;
  }

}
