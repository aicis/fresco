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

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.ProtocolFactory;
import dk.alexandra.fresco.framework.builder.binary.BasicBinaryFactory;
import dk.alexandra.fresco.framework.builder.binary.BinaryBuilder;
import dk.alexandra.fresco.framework.builder.binary.BuilderFactoryBinary;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary;
import dk.alexandra.fresco.framework.value.SBool;

public class DummyBuilderFactory implements BuilderFactoryBinary {

  private DummyFactory factory;

  public DummyBuilderFactory() {
    this.factory = new DummyFactory();
  }

  @Override
  public ProtocolFactory getProtocolFactory() {
    return factory;
  }

  @Override
  public BasicBinaryFactory createBasicBinaryFactory() {
    return factory;
  }

  @Override
  public BinaryBuilder createBinaryBuilder(ProtocolBuilderBinary builder) {
    return new BinaryBuilder() {

      @Override
      public Computation<SBool> xor(Computation<SBool> left, boolean right) {
        return xor(left, () -> factory.getKnownConstantSBool(right));
      }

      @Override
      public Computation<SBool> xor(Computation<SBool> left, Computation<SBool> right) {
        DummyNativeProtocol<SBool> c = new DummyXorProtocol(left, right);
        builder.append(c);
        return c;
      }

      @Override
      public Computation<Boolean> open(Computation<SBool> toOpen) {
        DummyNativeProtocol<Boolean> c = new DummyOpenBoolProtocol(toOpen);
        builder.append(c);
        return c;
      }

      @Override
      public Computation<Boolean> open(Computation<SBool> toOpen, int towardsPartyId) {
        DummyNativeProtocol<Boolean> c = new DummyOpenBoolProtocol(toOpen, towardsPartyId);
        builder.append(c);
        return c;
      }

      @Override
      public Computation<SBool> not(Computation<SBool> in) {
        DummyNativeProtocol<SBool> c = new DummyNotProtocol(in);
        builder.append(c);
        return c;
      }

      @Override
      public Computation<SBool> known(boolean known) {
        return () -> factory.getKnownConstantSBool(known);
      }

      @Override
      public Computation<SBool[]> known(boolean[] known) {
        return () -> factory.getKnownConstantSBools(known);
      }

      @Override
      public Computation<SBool> input(boolean in, int inputter) {
        DummyNativeProtocol<SBool> c = new DummyCloseBoolProtocol(in, inputter);
        builder.append(c);
        return c;
      }

      @Override
      public Computation<SBool> and(Computation<SBool> left, boolean right) {
        return and(left, () -> factory.getKnownConstantSBool(right));
      }

      @Override
      public Computation<SBool> and(Computation<SBool> left, Computation<SBool> right) {
        DummyNativeProtocol<SBool> c = new DummyAndProtocol(left, right);
        builder.append(c);
        return c;
      }
    };
  }

}
