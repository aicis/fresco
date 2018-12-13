package dk.alexandra.fresco.suite.dummy.arithmetic;

import dk.alexandra.fresco.framework.builder.numeric.field.BigIntegerFieldDefinition;
import dk.alexandra.fresco.framework.builder.numeric.field.BigIntegerModulus;
import dk.alexandra.fresco.framework.util.ModulusFinder;
import org.hamcrest.core.Is;
import org.hamcrest.core.IsNot;
import org.hamcrest.core.StringContains;
import org.junit.Assert;
import org.junit.Test;

public class DummyArithmeticSIntTest {

  private BigIntegerModulus modulus = new BigIntegerModulus(ModulusFinder.findSuitableModulus(128));
  private BigIntegerFieldDefinition definition = new BigIntegerFieldDefinition(modulus);

  @Test
  public void testToString() {
    BigIntegerFieldDefinition fd500 = new BigIntegerFieldDefinition(new BigIntegerModulus(500));
    DummyArithmeticSInt value = new DummyArithmeticSInt(fd500.createElement(42));
    String toString = value.toString();
    Assert.assertThat(toString, StringContains.containsString("42"));
    Assert.assertThat(value.toString(), Is.is(toString));
  }

  @Test
  public void testEquals() {
    BigIntegerFieldDefinition fd500 = new BigIntegerFieldDefinition(new BigIntegerModulus(500));
    DummyArithmeticSInt value1 = new DummyArithmeticSInt(fd500.createElement(42));
    DummyArithmeticSInt value2 = new DummyArithmeticSInt(fd500.createElement(42));
    DummyArithmeticSInt value3 = new DummyArithmeticSInt(definition.createElement(41));
    Assert.assertThat(value1, Is.is(value2));
    Assert.assertThat(value1, IsNot.not(value3));
    Assert.assertThat(value1, Is.is(value1));

    Assert.assertThat(value1, IsNot.not((DummyArithmeticSInt) null));
    Assert.assertThat(value1, IsNot.not("42"));
  }

  @Test
  public void testHashCode() {
    DummyArithmeticSInt value1 = new DummyArithmeticSInt(definition.createElement(42));
    DummyArithmeticSInt value2 = new DummyArithmeticSInt(definition.createElement(42));
    DummyArithmeticSInt value3 = new DummyArithmeticSInt(definition.createElement(41));
    Assert.assertThat(value1.hashCode(), Is.is(value2.hashCode()));
    Assert.assertThat(value1.hashCode(), IsNot.not(value3.hashCode()));
    Assert.assertThat(value1.hashCode(), Is.is(value1.hashCode()));
  }
}