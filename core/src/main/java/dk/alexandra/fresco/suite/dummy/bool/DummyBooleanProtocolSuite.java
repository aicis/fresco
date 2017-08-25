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

import dk.alexandra.fresco.framework.BuilderFactory;
import dk.alexandra.fresco.framework.builder.ProtocolBuilderBinary.ParallelBinaryBuilder;
import dk.alexandra.fresco.framework.builder.ProtocolBuilderBinary.SequentialBinaryBuilder;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.resources.ResourcePoolImpl;
import dk.alexandra.fresco.suite.ProtocolSuite;
import java.security.SecureRandom;
import java.util.Random;


/**
 * Dummy protocol suite that does no secret computation. Only for testing purposes.
 *
 * Do NOT use in production! :-)
 *
 * Currently it only implements basic logic operations "natively".
 */
public class DummyBooleanProtocolSuite
    implements ProtocolSuite<ResourcePoolImpl, SequentialBinaryBuilder> {

  @Override
  public RoundSynchronization<ResourcePoolImpl> createRoundSynchronization() {
    return new DummyRoundSynchronization<ResourcePoolImpl>();
  }

  @Override
  public ResourcePoolImpl createResourcePool(int myId, int size, Network network, Random rand,
      SecureRandom secRand) {
    return new ResourcePoolImpl(myId, size, network, rand, secRand);
  }


  @Override
  public BuilderFactory<SequentialBinaryBuilder, ParallelBinaryBuilder> init(
      ResourcePoolImpl resourcePool) {
    BuilderFactory<SequentialBinaryBuilder, ParallelBinaryBuilder> b =
        new DummyBooleanBuilderFactory(new DummyBooleanFactory());
    return b;
  }


}
