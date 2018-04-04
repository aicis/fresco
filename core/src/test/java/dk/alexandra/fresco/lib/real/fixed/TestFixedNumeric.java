package dk.alexandra.fresco.lib.real.fixed;

import dk.alexandra.fresco.framework.builder.numeric.BuilderFactoryNumeric;
import dk.alexandra.fresco.lib.field.integer.BasicNumericContext;
import dk.alexandra.fresco.lib.real.RealNumericContext;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticBuilderFactory;
import java.math.BigInteger;
import org.junit.Test;

public class TestFixedNumeric {

  @Test
  public void testFixedNumericProtocolBuilderNumericInt() {
    BuilderFactoryNumeric bfn = new DummyArithmeticBuilderFactory(
        new BasicNumericContext(16, BigInteger.TEN, 1, 1),
        new RealNumericContext(0));
    new FixedNumeric(bfn.createSequential(), 4);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFixedNumericProtocolBuilderNumericIntFail() {
    BuilderFactoryNumeric bfn = new DummyArithmeticBuilderFactory(
        new BasicNumericContext(16, BigInteger.TEN, 1, 1),
        new RealNumericContext(0));
    new FixedNumeric(bfn.createSequential(), 5);
  }

}
