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

import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;
import dk.alexandra.fresco.framework.BuilderFactory;
import dk.alexandra.fresco.framework.builder.ProtocolBuilderNumeric.SequentialNumericBuilder;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.SCENetwork;
import dk.alexandra.fresco.suite.NumericProtocolSuite;
import dk.alexandra.fresco.suite.ProtocolSuite;


/**
 * The {@link ProtocolSuite} of the Dummy Arithmetic suite. Uses a
 * {@link DummyArithmeticResourcePool} and provides a {@link SequentialNumericBuilder}.
 */
public class DummyArithmeticProtocolSuite
    implements NumericProtocolSuite<DummyArithmeticResourcePool, SequentialNumericBuilder> {

  private BigInteger modulus;
  private int maxBitLength;

  public DummyArithmeticProtocolSuite(BigInteger modulus, int maxBitLength) {
    this.modulus = modulus;
    this.maxBitLength = maxBitLength;
  }

  @Override
  public BigInteger getModulus() {
    return modulus;
  }

  @Override
  public int getMaxBitLength() {
    return maxBitLength;
  }

  @Override
  public BuilderFactory<SequentialNumericBuilder> init(DummyArithmeticResourcePool resourcePool) {
    return new DummyArithmeticBuilderFactory(new DummyArithmeticFactory(modulus, maxBitLength));
  }

  @Override
  public RoundSynchronization<DummyArithmeticResourcePool> createRoundSynchronization() {
    return new RoundSynchronization<DummyArithmeticResourcePool>() {

      @Override
      public void finishedBatch(int gatesEvaluated, DummyArithmeticResourcePool resourcePool,
          SCENetwork sceNetwork) throws IOException {}

      @Override
      public void finishedEval(DummyArithmeticResourcePool resourcePool, SCENetwork sceNetwork)
          throws IOException {}
    };
  }

  @Override
  public DummyArithmeticResourcePool createResourcePool(int myId, int size, Network network,
      Random rand, SecureRandom secRand) {
    return new DummyArithmeticResourcePoolImpl(myId, size, network, rand, secRand, modulus);
  }

}
