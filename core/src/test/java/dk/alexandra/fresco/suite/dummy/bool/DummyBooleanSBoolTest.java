package dk.alexandra.fresco.suite.dummy.bool;

import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticSInt;
import org.hamcrest.core.Is;
import org.hamcrest.core.IsNot;
import org.hamcrest.core.StringContains;
import org.junit.Assert;
import org.junit.Test;

public class DummyBooleanSBoolTest {

  @Test
  public void testToString() throws Exception {
    DummyBooleanSBool value = new DummyBooleanSBool(true);
    String toString = value.toString();
    Assert.assertThat(toString, StringContains.containsString("true"));
    Assert.assertThat(value.toString(), Is.is(toString));
  }

  @Test
  public void testEquals() throws Exception {
    DummyBooleanSBool value1 = new DummyBooleanSBool(true);
    DummyBooleanSBool value2 = new DummyBooleanSBool(true);
    DummyBooleanSBool value3 = new DummyBooleanSBool(false);
    Assert.assertThat(value1, Is.is(value2));
    Assert.assertThat(value1, IsNot.not(value3));
    Assert.assertThat(value1, Is.is(value1));

    Assert.assertThat(value1, IsNot.not((DummyArithmeticSInt) null));
    Assert.assertThat(value1, IsNot.not("42"));
  }

  @Test
  public void testHashCode() throws Exception {
    DummyBooleanSBool value1 = new DummyBooleanSBool(true);
    DummyBooleanSBool value2 = new DummyBooleanSBool(true);
    DummyBooleanSBool value3 = new DummyBooleanSBool(false);
    Assert.assertThat(value1.hashCode(), Is.is(value2.hashCode()));
    Assert.assertThat(value1.hashCode(), IsNot.not(value3.hashCode()));
    Assert.assertThat(value1.hashCode(), Is.is(value1.hashCode()));
  }

}