/*
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
package dk.alexandra.fresco.suite.spdz.gates;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.BigIntegerSerializer;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePool;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzElement;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzInputMask;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.storage.SpdzStorage;
import java.math.BigInteger;
import java.util.List;

public class SpdzOutputSingleProtocol extends SpdzNativeProtocol<BigInteger>
    implements SpdzOutputProtocol {

  private DRes<SInt> in;
  private BigInteger out;
  private int target_player;
  private SpdzInputMask mask;

  public SpdzOutputSingleProtocol(DRes<SInt> in, int target_player) {
    this.in = in;
    this.target_player = target_player;
  }

  @Override
  public BigInteger out() {
    return out;
  }

  @Override
  public EvaluationStatus evaluate(int round, SpdzResourcePool spdzResourcePool,
      Network network) {

    int myId = spdzResourcePool.getMyId();
    SpdzStorage storage = spdzResourcePool.getStore();
    BigIntegerSerializer serializer = spdzResourcePool.getSerializer();
    switch (round) {
      case 0:
        this.mask = storage.getSupplier().getNextInputMask(target_player);
        SpdzSInt closedValue = (SpdzSInt) this.in.out();
        SpdzElement inMinusMask = closedValue.value.subtract(this.mask.getMask());
        storage.addClosedValue(inMinusMask);
        network.sendToAll(serializer.toBytes(inMinusMask.getShare()));
        return EvaluationStatus.HAS_MORE_ROUNDS;
      case 1:
        List<byte[]> shares = network.receiveFromAll();
        BigInteger openedVal = BigInteger.valueOf(0);
        for (byte[] buffer : shares) {
          openedVal = openedVal.add(serializer.toBigInteger(buffer));
        }
        openedVal = openedVal.mod(spdzResourcePool.getModulus());
        storage.addOpenedValue(openedVal);
        if (target_player == myId) {
          openedVal = openedVal.add(this.mask.getRealValue()).mod(spdzResourcePool.getModulus());
          this.out = openedVal;
        }
        return EvaluationStatus.IS_DONE;
      default:
        throw new MPCException("No more rounds to evaluate.");
    }
  }

}
