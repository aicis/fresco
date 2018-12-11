package dk.alexandra.fresco.lib.real.fixed;

import dk.alexandra.fresco.framework.builder.numeric.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.builder.numeric.FieldDefinitionBigInteger;
import dk.alexandra.fresco.framework.builder.numeric.ModulusBigInteger;
import dk.alexandra.fresco.lib.field.integer.BasicNumericContext;
import dk.alexandra.fresco.lib.real.RealNumericContext;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticBuilderFactory;
import org.junit.Test;

public class TestFixedNumeric {

  private final FieldDefinitionBigInteger fieldDefinition = new FieldDefinitionBigInteger(
      new ModulusBigInteger(10));

  @Test
  public void testFixedNumericLegalPrecision() {
    BuilderFactoryNumeric bfn = new DummyArithmeticBuilderFactory(
        new BasicNumericContext(16, 1, 1, fieldDefinition),
        new RealNumericContext(0));
    new FixedNumeric(bfn.createSequential(), 4);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFixedNumericPrecisionTooLarge() {
    BuilderFactoryNumeric bfn = new DummyArithmeticBuilderFactory(
        new BasicNumericContext(16, 1, 1, fieldDefinition),
        new RealNumericContext(0));
    new FixedNumeric(bfn.createSequential(), 5);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFixedNumericPrecisionTooLow() {
    BuilderFactoryNumeric bfn = new DummyArithmeticBuilderFactory(
        new BasicNumericContext(16, 1, 1, fieldDefinition),
        new RealNumericContext(0));
    new FixedNumeric(bfn.createSequential(), -1);
  }

  @Test(expected = NullPointerException.class)
  public void testFixedNumericNullBuilder() {
    new FixedNumeric(null, -1);
  }
}
