package dk.alexandra.fresco.suite.dummy.arithmetic;

import dk.alexandra.fresco.framework.builder.numeric.field.BigIntegerFieldDefinition;
import dk.alexandra.fresco.framework.util.ModulusFinder;
import org.hamcrest.core.Is;
import org.hamcrest.core.StringContains;
import org.junit.Assert;
import org.junit.Test;

public class DummyArithmeticSIntTest {

  @Test
  public void testToString() {
    BigIntegerFieldDefinition fd500 = new BigIntegerFieldDefinition(
        ModulusFinder.findSuitableModulus(8));
    DummyArithmeticSInt value = new DummyArithmeticSInt(fd500.createElement(42));
    String toString = value.toString();
    Assert.assertThat(toString, StringContains.containsString("42"));
    Assert.assertThat(value.toString(), Is.is(toString));
  }
  @Test
  public void testOut() {
    BigIntegerFieldDefinition fd500 = new BigIntegerFieldDefinition(
        ModulusFinder.findSuitableModulus(8));
    DummyArithmeticSInt value = new DummyArithmeticSInt(fd500.createElement(42));
    Assert.assertThat(value.out(), Is.is(value));
  }
}
