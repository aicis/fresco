package dk.alexandra.fresco.suite.dummy.arithmetic;

import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.builder.numeric.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.ModulusFinder;
import dk.alexandra.fresco.lib.field.integer.BasicNumericContext;
import dk.alexandra.fresco.lib.real.RealNumericContext;
import dk.alexandra.fresco.suite.ProtocolSuite;
import dk.alexandra.fresco.suite.ProtocolSuiteNumeric;
import java.math.BigInteger;


/**
 * The {@link ProtocolSuite} of the Dummy Arithmetic suite. Uses a
 * {@link DummyArithmeticResourcePool} and provides a {@link ProtocolBuilderNumeric}. <b>NB: Do NOT
 * use in production!</b>
 */
public class DummyArithmeticProtocolSuite
    implements ProtocolSuiteNumeric<DummyArithmeticResourcePool> {

  private final BigInteger modulus;
  private final int maxBitLength;
  private final int precision;

  public DummyArithmeticProtocolSuite() {
    this(ModulusFinder.findSuitableModulus(128), 32, 4);
  }

  public DummyArithmeticProtocolSuite(BigInteger modulus, int maxBitLength, int precision) {
    this.modulus = modulus;
    this.maxBitLength = maxBitLength;
    this.precision = precision;
  }

  @Override
  public BuilderFactoryNumeric init(DummyArithmeticResourcePool resourcePool, Network network) {
    BasicNumericContext basicNumericContext = new BasicNumericContext(maxBitLength, modulus,
        resourcePool.getMyId(), resourcePool.getNoOfParties());
    RealNumericContext realNumericContext = new RealNumericContext(precision);
    return new DummyArithmeticBuilderFactory(basicNumericContext, realNumericContext);
  }

  @Override
  public RoundSynchronization<DummyArithmeticResourcePool> createRoundSynchronization() {
    return new RoundSynchronization<DummyArithmeticResourcePool>() {

      @Override
      public void finishedBatch(int gatesEvaluated, DummyArithmeticResourcePool resourcePool,
          Network network) {}

      @Override
      public void finishedEval(DummyArithmeticResourcePool resourcePool, Network network) {}

      @Override
      public void beforeBatch(ProtocolCollection<DummyArithmeticResourcePool> protocols,
          DummyArithmeticResourcePool resourcePool, Network network) {

      }
    };
  }
}
