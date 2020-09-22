package dk.alexandra.fresco.lib.fixed;

import dk.alexandra.fresco.framework.builder.numeric.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.builder.numeric.field.BigIntegerFieldDefinition;
import dk.alexandra.fresco.framework.util.ModulusFinder;
import dk.alexandra.fresco.lib.field.integer.BasicNumericContext;
import dk.alexandra.fresco.lib.fixed.DefaultFixedNumeric;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticBuilderFactory;
import org.junit.Test;

public class TestFixedNumeric {

  private final BigIntegerFieldDefinition fieldDefinition = new BigIntegerFieldDefinition(
      ModulusFinder.findSuitableModulus(8));

  @Test
  public void testFixedNumericLegalPrecision() {
    BuilderFactoryNumeric bfn = new DummyArithmeticBuilderFactory(
        new BasicNumericContext(16, 1, 1, fieldDefinition, 0));
    FixedNumeric.using(bfn.createSequential());
  }

  @Test(expected = NullPointerException.class)
  public void testFixedNumericNullBuilder() {
    FixedNumeric.using(null);
  }
}
