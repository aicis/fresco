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
import dk.alexandra.fresco.logging.PerformanceLogger;
import dk.alexandra.fresco.logging.binary.BinaryComparisonLoggingDecorator;
import dk.alexandra.fresco.logging.binary.BinaryLoggingDecorator;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * A {@link BuilderFactoryNumeric} implementation for the Dummy Boolean suite. This class has
 * built-in support for logging the amount of different operations (i.e. protocols) that the
 * application asks for.
 *
 */
public class DummyBooleanBuilderFactory implements BuilderFactoryBinary {

  public static final ConcurrentMap<Integer, List<PerformanceLogger>> performanceLoggers =
      new ConcurrentHashMap<>();
  
  private int myId;
  private BinaryComparisonLoggingDecorator compDecorator;
  private BinaryLoggingDecorator binaryDecorator;


  public DummyBooleanBuilderFactory(int myId) {
    this.myId = myId;
    performanceLoggers.putIfAbsent(myId, new ArrayList<PerformanceLogger>());
  }

  @Override
  public Comparison createComparison(ProtocolBuilderBinary builder) {
    Comparison comp = BuilderFactoryBinary.super.createComparison(builder);

    if (compDecorator == null) {
      compDecorator = new BinaryComparisonLoggingDecorator(comp);
      performanceLoggers.get(myId).add(compDecorator);
    } else {
      compDecorator.setDelegate(comp);
    }
    return compDecorator;
  }

  @Override
  public Binary createBinary(ProtocolBuilderBinary builder) {
    Binary binary = new Binary() {

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
    if (binaryDecorator == null) {
      binaryDecorator = new BinaryLoggingDecorator(binary);
      performanceLoggers.get(myId).add(binaryDecorator);
    } else {
      binaryDecorator.setDelegate(binary);
    }
    return binaryDecorator;
  }
}
