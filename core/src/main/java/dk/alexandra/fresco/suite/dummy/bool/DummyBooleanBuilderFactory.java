package dk.alexandra.fresco.suite.dummy.bool;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.binary.Binary;
import dk.alexandra.fresco.framework.builder.binary.BuilderFactoryBinary;
import dk.alexandra.fresco.framework.builder.binary.Comparison;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary;
import dk.alexandra.fresco.framework.builder.numeric.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.SBool;
import java.util.Random;

/**
 * A {@link BuilderFactoryNumeric} implementation for the Dummy Boolean suite. This class has
 * built-in support for logging the amount of different operations (i.e. protocols) that the
 * application asks for.
 *
 */
public class DummyBooleanBuilderFactory implements BuilderFactoryBinary {

  private final Random rand;

  public DummyBooleanBuilderFactory() {
    this.rand = new Random(0);
  }

  @Override
  public Comparison createComparison(ProtocolBuilderBinary builder) {
    return BuilderFactoryBinary.super.createComparison(builder);
  }

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
              Network network) {
            bit = new DummyBooleanSBool(rand.nextBoolean());
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
