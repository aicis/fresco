package dk.alexandra.fresco.suite.dummy.arithmetic;

import org.hamcrest.core.Is;
import org.hamcrest.core.IsNot;
import org.hamcrest.core.StringContains;
import org.junit.Assert;
import org.junit.Test;

public class DummyArithmeticSIntTest {

  @Test
  public void testToString() throws Exception {
    DummyArithmeticSInt value = new DummyArithmeticSInt(42);
    String toString = value.toString();
    Assert.assertThat(toString, StringContains.containsString("42"));
    Assert.assertThat(value.toString(), Is.is(toString));
  }

  @Test
  public void testEquals() throws Exception {
    DummyArithmeticSInt value1 = new DummyArithmeticSInt(42);
    DummyArithmeticSInt value2 = new DummyArithmeticSInt(42);
    DummyArithmeticSInt value3 = new DummyArithmeticSInt(41);
    Assert.assertThat(value1, Is.is(value2));
    Assert.assertThat(value1, IsNot.not(value3));
    Assert.assertThat(value1, Is.is(value1));

    Assert.assertThat(value1, IsNot.not((DummyArithmeticSInt) null));
    Assert.assertThat(value1, IsNot.not("42"));
  }

  @Test
  public void testHashCode() throws Exception {
    DummyArithmeticSInt value1 = new DummyArithmeticSInt(42);
    DummyArithmeticSInt value2 = new DummyArithmeticSInt(42);
    DummyArithmeticSInt value3 = new DummyArithmeticSInt(41);
    Assert.assertThat(value1.hashCode(), Is.is(value2.hashCode()));
    Assert.assertThat(value1.hashCode(), IsNot.not(value3.hashCode()));
    Assert.assertThat(value1.hashCode(), Is.is(value1.hashCode()));
  }
}