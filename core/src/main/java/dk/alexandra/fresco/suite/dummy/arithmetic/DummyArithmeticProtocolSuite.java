package dk.alexandra.fresco.suite.dummy.arithmetic;

import dk.alexandra.fresco.framework.BuilderFactory;
import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.network.SCENetwork;
import dk.alexandra.fresco.lib.field.integer.BasicNumericContext;
import dk.alexandra.fresco.suite.ProtocolSuite;
import java.io.IOException;
import java.math.BigInteger;


/**
 * The {@link ProtocolSuite} of the Dummy Arithmetic suite. Uses a
 * {@link DummyArithmeticResourcePool} and provides a {@link ProtocolBuilderNumeric}.
 * <b>NB: Do NOT use in production!</b>
 */
public class DummyArithmeticProtocolSuite
    implements ProtocolSuite<DummyArithmeticResourcePool, ProtocolBuilderNumeric> {

  private BasicNumericContext basicNumericContext;
  private final BigInteger modulus;
  private final int maxBitLength;

  public DummyArithmeticProtocolSuite(BigInteger modulus, int maxBitLength) {
    this.modulus = modulus;
    this.maxBitLength = maxBitLength;
  }

  @Override
  public BuilderFactory<ProtocolBuilderNumeric> init(DummyArithmeticResourcePool resourcePool) {
    basicNumericContext = new BasicNumericContext(maxBitLength, modulus, resourcePool);
    return new DummyArithmeticBuilderFactory(basicNumericContext);
  }

  @Override
  public RoundSynchronization<DummyArithmeticResourcePool> createRoundSynchronization() {
    return new RoundSynchronization<DummyArithmeticResourcePool>() {

      @Override
      public void finishedBatch(int gatesEvaluated, DummyArithmeticResourcePool resourcePool,
          SCENetwork sceNetwork) throws IOException {}

      @Override
      public void finishedEval(DummyArithmeticResourcePool resourcePool, SCENetwork sceNetwork)
          throws IOException {
      }

      @Override
      public void beforeBatch(ProtocolCollection<DummyArithmeticResourcePool> protocols,
          DummyArithmeticResourcePool resourcePool) throws IOException {
        
      }
    };
  }
}
