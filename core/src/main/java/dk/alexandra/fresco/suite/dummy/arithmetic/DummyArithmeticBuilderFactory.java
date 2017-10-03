package dk.alexandra.fresco.suite.dummy.arithmetic;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.network.SCENetwork;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.compare.MiscBigIntegerGenerators;
import dk.alexandra.fresco.lib.field.integer.BasicNumericContext;
import java.math.BigInteger;

/**
 * A {@link BuilderFactoryNumeric} implementation for the Dummy Arithmetic suite.
 *
 */
public class DummyArithmeticBuilderFactory implements BuilderFactoryNumeric {

  private static final int EXP_PIPE_LENGTH = 201;
  private BasicNumericContext factory;
  private MiscBigIntegerGenerators mog;

  public DummyArithmeticBuilderFactory(BasicNumericContext factory) {
    super();
    this.factory = factory;
  }

  @Override
  public BasicNumericContext getBasicNumericContext() {
    return factory;
  }

  @Override
  public Numeric createNumeric(ProtocolBuilderNumeric builder) {

    return new Numeric() {

      @Override
      public DRes<SInt> sub(DRes<SInt> a, BigInteger b) {
        DummyArithmeticNativeProtocol<SInt> c =
            new DummyArithmeticSubtractProtocol(a, () -> new DummyArithmeticSInt(b));
        return builder.append(c);
      }

      @Override
      public DRes<SInt> sub(BigInteger a, DRes<SInt> b) {
        DummyArithmeticSubtractProtocol c =
            new DummyArithmeticSubtractProtocol(() -> new DummyArithmeticSInt(a), b);
        return builder.append(c);
      }

      @Override
      public DRes<SInt> sub(DRes<SInt> a, DRes<SInt> b) {
        DummyArithmeticSubtractProtocol c = new DummyArithmeticSubtractProtocol(a, b);
        return builder.append(c);
      }

      @Override
      public DRes<SInt> randomElement() {
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
      public DRes<SInt> randomBit() {
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
      public DRes<BigInteger> open(DRes<SInt> secretShare) {
        DummyArithmeticOpenToAllProtocol c = new DummyArithmeticOpenToAllProtocol(secretShare);
        return builder.append(c);
      }

      @Override
      public DRes<BigInteger> open(DRes<SInt> secretShare, int outputParty) {
        DummyArithmeticOpenProtocol c = new DummyArithmeticOpenProtocol(secretShare, outputParty);
        return builder.append(c);
      }

      @Override
      public DRes<SInt> mult(BigInteger a, DRes<SInt> b) {
        DummyArithmeticMultProtocol c =
            new DummyArithmeticMultProtocol(() -> new DummyArithmeticSInt(a), b);
        return builder.append(c);
      }

      @Override
      public DRes<SInt> mult(DRes<SInt> a, DRes<SInt> b) {
        DummyArithmeticMultProtocol c = new DummyArithmeticMultProtocol(a, b);
        return builder.append(c);
      }

      @Override
      public DRes<SInt> known(BigInteger value) {
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
      public DRes<SInt> input(BigInteger value, int inputParty) {
        DummyArithmeticCloseProtocol c = new DummyArithmeticCloseProtocol(inputParty, () -> value);
        return builder.append(c);
      }

      @Override
      public DRes<SInt[]> getExponentiationPipe() {
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
      public DRes<SInt> add(BigInteger a, DRes<SInt> b) {
        DummyArithmeticAddProtocol c =
            new DummyArithmeticAddProtocol(() -> new DummyArithmeticSInt(a), b);
        return builder.append(c);
      }

      @Override
      public DRes<SInt> add(DRes<SInt> a, DRes<SInt> b) {
        DummyArithmeticAddProtocol c = new DummyArithmeticAddProtocol(a, b);
        return builder.append(c);
      }
    };
  }

  @Override
  public MiscBigIntegerGenerators getBigIntegerHelper() {
    if (mog == null) {
      mog = new MiscBigIntegerGenerators(factory.getModulus());
    }
    return mog;
  }

}
