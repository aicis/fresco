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

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.network.SCENetwork;
import dk.alexandra.fresco.framework.network.serializers.BigIntegerSerializer;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePool;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzElement;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzTriple;
import dk.alexandra.fresco.suite.spdz.storage.SpdzStorage;
import java.math.BigInteger;

public class SpdzMultProtocol extends SpdzNativeProtocol<SInt> {

  private Computation<SInt> left;
  private Computation<SInt> right;
  private SpdzSInt out;
  private SpdzTriple triple;
  private SpdzElement epsilon, delta; // my share of the differences [x]-[a]
  // and [y]-[b].

  public SpdzMultProtocol(Computation<SInt> left, Computation<SInt> right) {
    this.left = left;
    this.right = right;
  }

  @Override
  public EvaluationStatus evaluate(int round, SpdzResourcePool spdzResourcePool,
      SCENetwork network) {
    SpdzStorage store = spdzResourcePool.getStore();
    int noOfPlayers = spdzResourcePool.getNoOfParties();
    BigIntegerSerializer serializer = spdzResourcePool.getSerializer();
    switch (round) {
      case 0:
        this.triple = store.getSupplier().getNextTriple();

        epsilon = ((SpdzSInt) left.out()).value.subtract(triple.getA());
        delta = ((SpdzSInt) right.out()).value.subtract(triple.getB());

        network.sendToAll(serializer.toBytes(epsilon.getShare()));
        network.sendToAll(serializer.toBytes(delta.getShare()));
        network.expectInputFromAll();
        return EvaluationStatus.HAS_MORE_ROUNDS;
      case 1:
        BigInteger[] epsilonShares = new BigInteger[noOfPlayers];
        BigInteger[] deltaShares = new BigInteger[noOfPlayers];
        for (int i = 0; i < noOfPlayers; i++) {
          epsilonShares[i] = serializer.toBigInteger(network.receive(i + 1));
          deltaShares[i] = serializer.toBigInteger(network.receive(i + 1));
        }
        SpdzElement res = triple.getC();
        BigInteger e = epsilonShares[0];
        BigInteger d = deltaShares[0];
        for (int i = 1; i < epsilonShares.length; i++) {
          e = e.add(epsilonShares[i]);
          d = d.add(deltaShares[i]);
        }
        BigInteger modulus = spdzResourcePool.getModulus();
        e = e.mod(modulus);
        d = d.mod(modulus);

        BigInteger eTimesd = e.multiply(d).mod(modulus);
        SpdzElement ed = new SpdzElement(
            eTimesd,
            store.getSSK().multiply(eTimesd).mod(modulus),
            modulus);
        res = res.add(triple.getB().multiply(e))
            .add(triple.getA().multiply(d))
            .add(ed, spdzResourcePool.getMyId());
        out = new SpdzSInt(res);
        // Set the opened and closed value.
        store.addOpenedValue(e);
        store.addOpenedValue(d);
        store.addClosedValue(epsilon);
        store.addClosedValue(delta);
        // help the garbage collector.
        left = null;
        right = null;
        triple = null;
        epsilon = null;
        delta = null;
        return EvaluationStatus.IS_DONE;
    }
    throw new MPCException("Cannot evaluate rounds larger than 1");
  }

  @Override
  public SpdzSInt out() {
    return out;
  }

}
