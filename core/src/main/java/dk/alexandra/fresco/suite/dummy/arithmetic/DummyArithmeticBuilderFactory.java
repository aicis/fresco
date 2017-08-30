package dk.alexandra.fresco.suite.dummy.arithmetic;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.numeric.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.builder.numeric.NumericBuilder;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.network.SCENetwork;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.compare.MiscOIntGenerators;
import dk.alexandra.fresco.lib.field.integer.BasicNumericContext;
import java.math.BigInteger;

/**
 * A {@link BuilderFactoryNumeric} implementation for the Dummy Arithmetic suite.
 *
 */
public class DummyArithmeticBuilderFactory implements BuilderFactoryNumeric {

  private static final int EXP_PIPE_LENGTH = 201;
  private BasicNumericContext factory;
  private MiscOIntGenerators mog;

  public DummyArithmeticBuilderFactory(BasicNumericContext factory) {
    super();
    this.factory = factory;
  }

  @Override
  public BasicNumericContext getBasicNumericFactory() {
    return factory;
  }

  @Override
  public NumericBuilder createNumericBuilder(ProtocolBuilderNumeric builder) {

    return new NumericBuilder() {

      @Override
      public Computation<SInt> sub(Computation<SInt> a, BigInteger b) {
        DummyArithmeticNativeProtocol<SInt> c =
            new DummyArithmeticSubtractProtocol(a, () -> new DummyArithmeticSInt(b));
        return builder.append(c);
      }

      @Override
      public Computation<SInt> sub(BigInteger a, Computation<SInt> b) {
        DummyArithmeticSubtractProtocol c =
            new DummyArithmeticSubtractProtocol(() -> new DummyArithmeticSInt(a), b);
        return builder.append(c);
      }

      @Override
      public Computation<SInt> sub(Computation<SInt> a, Computation<SInt> b) {
        DummyArithmeticSubtractProtocol c = new DummyArithmeticSubtractProtocol(a, b);
        return builder.append(c);
      }

      @Override
      public Computation<SInt> randomElement() {
        DummyArithmeticNativeProtocol<SInt> c = new DummyArithmeticNativeProtocol<SInt>() {

          DummyArithmeticSInt elm;

          @Override
          public EvaluationStatus evaluate(int round, DummyArithmeticResourcePool resourcePool,
              SCENetwork network) {
            BigInteger r;
            do {
              r = new BigInteger(factory.getModulus().bitLength(), resourcePool.getRandom());
            } while (r.compareTo(factory.getModulus()) >= 0);
            elm = new DummyArithmeticSInt(r);
            return EvaluationStatus.IS_DONE;
          }

          @Override
          public SInt out() {
            return elm;
          }
        };
        return builder.append(c);
      }

      @Override
      public Computation<SInt> randomBit() {
        DummyArithmeticNativeProtocol<SInt> c = new DummyArithmeticNativeProtocol<SInt>() {

          DummyArithmeticSInt bit;

          @Override
          public EvaluationStatus evaluate(int round, DummyArithmeticResourcePool resourcePool,
              SCENetwork network) {
            bit = new DummyArithmeticSInt(BigInteger.valueOf(resourcePool.getRandom().nextInt(2)));
            return EvaluationStatus.IS_DONE;
          }

          @Override
          public SInt out() {
            return bit;
          }
        };
        return builder.append(c);
      }

      @Override
      public Computation<BigInteger> open(Computation<SInt> secretShare) {
        DummyArithmeticOpenToAllProtocol c = new DummyArithmeticOpenToAllProtocol(secretShare);
        return builder.append(c);
      }

      @Override
      public Computation<BigInteger> open(Computation<SInt> secretShare, int outputParty) {
        DummyArithmeticOpenProtocol c = new DummyArithmeticOpenProtocol(secretShare, outputParty);
        return builder.append(c);
      }

      @Override
      public Computation<SInt> mult(BigInteger a, Computation<SInt> b) {
        DummyArithmeticMultProtocol c =
            new DummyArithmeticMultProtocol(() -> new DummyArithmeticSInt(a), b);
        return builder.append(c);
      }

      @Override
      public Computation<SInt> mult(Computation<SInt> a, Computation<SInt> b) {
        DummyArithmeticMultProtocol c = new DummyArithmeticMultProtocol(a, b);
        return builder.append(c);
      }

      @Override
      public Computation<SInt> known(BigInteger value) {
        DummyArithmeticNativeProtocol<SInt> c = new DummyArithmeticNativeProtocol<SInt>() {

          DummyArithmeticSInt val;

          @Override
          public EvaluationStatus evaluate(int round, DummyArithmeticResourcePool resourcePool,
              SCENetwork network) {
            val = new DummyArithmeticSInt(value);
            return EvaluationStatus.IS_DONE;
          }

          @Override
          public SInt out() {
            return val;
          }

        };
        return builder.append(c);
      }

      @Override
      public Computation<SInt> input(BigInteger value, int inputParty) {
        DummyArithmeticCloseProtocol c = new DummyArithmeticCloseProtocol(inputParty, () -> value);
        return builder.append(c);
      }

      @Override
      public Computation<SInt[]> getExponentiationPipe() {
        // TODO: fix how to set exponentiation pipe length
        DummyArithmeticNativeProtocol<SInt[]> c = new DummyArithmeticNativeProtocol<SInt[]>() {

          DummyArithmeticSInt[] pipe;

          @Override
          public EvaluationStatus evaluate(int round, DummyArithmeticResourcePool resourcePool,
              SCENetwork network) {
            pipe = new DummyArithmeticSInt[EXP_PIPE_LENGTH];
            for (int i = 0; i < pipe.length; i++) {
              pipe[i] = new DummyArithmeticSInt(1);
            }
            return EvaluationStatus.IS_DONE;
          }

          @Override
          public SInt[] out() {
            return pipe;
          }
        };
        return builder.append(c);
      }

      @Override
      public Computation<SInt> add(BigInteger a, Computation<SInt> b) {
        DummyArithmeticAddProtocol c =
            new DummyArithmeticAddProtocol(() -> new DummyArithmeticSInt(a), b);
        return builder.append(c);
      }

      @Override
      public Computation<SInt> add(Computation<SInt> a, Computation<SInt> b) {
        DummyArithmeticAddProtocol c = new DummyArithmeticAddProtocol(a, b);
        return builder.append(c);
      }
    };
  }

  @Override
  public MiscOIntGenerators getBigIntegerHelper() {
    if (mog == null) {
      mog = new MiscOIntGenerators(factory.getModulus());
    }
    return mog;
  }

}
