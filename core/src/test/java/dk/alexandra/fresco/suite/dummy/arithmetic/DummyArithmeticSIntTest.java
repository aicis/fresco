package dk.alexandra.fresco.suite.dummy.arithmetic;

import dk.alexandra.fresco.framework.builder.numeric.FieldDefinitionBigInteger;
import dk.alexandra.fresco.framework.builder.numeric.ModulusBigInteger;
import dk.alexandra.fresco.framework.util.ModulusFinder;
import org.hamcrest.core.Is;
import org.hamcrest.core.IsNot;
import org.hamcrest.core.StringContains;
import org.junit.Assert;
import org.junit.Test;

public class DummyArithmeticSIntTest {

  private ModulusBigInteger modulus = new ModulusBigInteger(ModulusFinder.findSuitableModulus(128));
  private FieldDefinitionBigInteger definition = new FieldDefinitionBigInteger(modulus);

  @Test
  public void testToString() {
    FieldDefinitionBigInteger fd500 = new FieldDefinitionBigInteger(new ModulusBigInteger(500));
    DummyArithmeticSInt value = new DummyArithmeticSInt(fd500.createElement(42));
    String toString = value.toString();
    Assert.assertThat(toString, StringContains.containsString("42"));
    Assert.assertThat(value.toString(), Is.is(toString));
  }

  @Test
  public void testEquals() {
    FieldDefinitionBigInteger fd500 = new FieldDefinitionBigInteger(new ModulusBigInteger(500));
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