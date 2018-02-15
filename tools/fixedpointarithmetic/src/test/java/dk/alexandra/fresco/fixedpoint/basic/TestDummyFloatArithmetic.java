package dk.alexandra.fresco.fixedpoint.basic;

import dk.alexandra.fresco.decimal.RealNumericProvider;
import dk.alexandra.fresco.decimal.floating.FloatNumeric;
import dk.alexandra.fresco.decimal.utils.BigBinary;
import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import java.math.BigDecimal;
import org.junit.Test;


public class TestDummyFloatArithmetic extends RealAbstractDummyArithmeticTest {

  @Override
  RealNumericProvider getProvider() {
    return scope -> new FloatNumeric(scope);
  }

  @Test
  public void testBigBinary() {
    double x = 0.0012345;
    BigBinary value = new BigBinary(BigDecimal.valueOf(x), 32);
    System.out.println(value);
  }
  @Test
  public void testTrunc() throws Exception {
    runTest(new MathTests.TestTrunc<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }
  
  
}
