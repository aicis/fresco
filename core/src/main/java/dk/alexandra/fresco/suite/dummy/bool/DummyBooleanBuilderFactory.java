/*
 * Copyright (c) 2015, 2016 FRESCO (http://github.com/aicis/fresco).
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

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.binary.Binary;
import dk.alexandra.fresco.framework.builder.binary.BuilderFactoryBinary;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary;
import dk.alexandra.fresco.framework.network.SCENetwork;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.SBool;

public class DummyBooleanBuilderFactory implements BuilderFactoryBinary {

  @Override
  public Binary createBinary(ProtocolBuilderBinary builder) {

    return new Binary() {

      @Override
      public DRes<SBool> known(boolean value) {
        return () -> new DummyBooleanSBool(value);
      }

      @Override
      public DRes<SBool> input(boolean value, int inputParty) {
        DummyBooleanCloseProtocol c = new DummyBooleanCloseProtocol(inputParty, () -> value);
        builder.append(c);
        return c;
      }

      @Override
      public DRes<SBool> randomBit() {
        DummyBooleanNativeProtocol<SBool> c = new DummyBooleanNativeProtocol<SBool>() {

          DummyBooleanSBool bit;

          @Override
          public EvaluationStatus evaluate(int round, ResourcePool resourcePool,
              SCENetwork network) {
            bit = new DummyBooleanSBool(resourcePool.getRandom().nextBoolean());
            return EvaluationStatus.IS_DONE;
          }

          @Override
          public SBool out() {
            return bit;
          }
        };
        builder.append(c);
        return c;
      }

      @Override
      public DRes<Boolean> open(DRes<SBool> secretShare) {
        DummyBooleanOpenProtocol c = new DummyBooleanOpenProtocol(secretShare);
        builder.append(c);
        return c;
      }

      @Override
      public DRes<Boolean> open(DRes<SBool> secretShare, int outputParty) {
        DummyBooleanOpenProtocol c = new DummyBooleanOpenProtocol(secretShare, outputParty);
        builder.append(c);
        return c;
      }

      @Override
      public DRes<SBool> and(DRes<SBool> a, DRes<SBool> b) {
        DummyBooleanAndProtocol c = new DummyBooleanAndProtocol(a, b);
        builder.append(c);
        return c;
      }

      @Override
      public DRes<SBool> xor(DRes<SBool> a, DRes<SBool> b) {
        DummyBooleanXorProtocol c = new DummyBooleanXorProtocol(a, b);
        builder.append(c);
        return c;
      }

      @Override
      public DRes<SBool> not(DRes<SBool> a) {
        DummyBooleanNotProtocol c = new DummyBooleanNotProtocol(a);
        builder.append(c);
        return c;
      }
    };
  }


}
