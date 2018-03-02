package dk.alexandra.fresco.framework.sce;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.ProtocolEvaluator;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedProtocolEvaluator;
import dk.alexandra.fresco.framework.sce.evaluator.SequentialStrategy;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticProtocolSuite;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticResourcePool;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticResourcePoolImpl;
import java.math.BigInteger;
import java.time.Duration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestSecureComputationEngineImpl {

  private SecureComputationEngineImpl<DummyArithmeticResourcePool, ProtocolBuilderNumeric> sce;

  /**
   * Sets up an SCE with the dummy arithmetic suite.
   */
  @Before
  public void setup() {
    DummyArithmeticProtocolSuite suite =
        new DummyArithmeticProtocolSuite(BigInteger.valueOf(101), 2);
    ProtocolEvaluator<DummyArithmeticResourcePool> evaluator =
        new BatchedProtocolEvaluator<>(new SequentialStrategy<>(), suite);
    sce = new SecureComputationEngineImpl<>(suite, evaluator);
    sce.shutdownSCE(); // test this before setup
    sce.setup();
  }

  @Test
  public void testSecureComputationEngineImpl() {
    assertThat(sce, instanceOf(SecureComputationEngineImpl.class));
  }

  @Test
  public void testRunApplication() {
    Application<BigInteger, ProtocolBuilderNumeric> app =
        builder -> {
          DRes<SInt> a = builder.numeric().known(BigInteger.valueOf(10));
          DRes<SInt> b = builder.numeric().known(BigInteger.valueOf(10));
          return builder.numeric().open(builder.numeric().add(a, b));
        };
    DummyArithmeticResourcePool rp =
        new DummyArithmeticResourcePoolImpl(0, 1, BigInteger.valueOf(101));

    BigInteger b = sce.runApplication(app, rp, null);
    assertThat(b, is(BigInteger.valueOf(20)));
  }

  @Test(expected = RuntimeException.class)
  public void testRunApplicationAppThrows() {
    Application<Object, ProtocolBuilderNumeric> app =
        builder -> {
          throw new RuntimeException();
        };
    DummyArithmeticResourcePool rp =
        new DummyArithmeticResourcePoolImpl(0, 1, BigInteger.valueOf(101));
    sce.runApplication(app, rp, null);
    fail("Should not be reachable");
  }

  @Test(expected = RuntimeException.class)
  public void testRunApplicationAppTimesOut() {
    Application<Object, ProtocolBuilderNumeric> app =
        builder -> {
          while (true) {

          }
        };
    DummyArithmeticResourcePool rp =
        new DummyArithmeticResourcePoolImpl(0, 1, BigInteger.valueOf(101));
    sce.runApplication(app, rp, null, Duration.ofNanos(1));
    fail("Should not be reachable");
  }

  /**
   * Shuts down the SCE.
   */
  @After
  public void tearDown() {
    sce.shutdownSCE();
  }

}
