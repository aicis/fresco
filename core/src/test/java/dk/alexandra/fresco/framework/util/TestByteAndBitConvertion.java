package dk.alexandra.fresco.framework.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.BitSet;
import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the {@link ByteAndBitConverter} class.
 */
public class TestByteAndBitConvertion {

  @Test
  public void testIntToBitSet() {
    BitSet x = ByteAndBitConverter.intToBitSet(5);
    BitSet expectedX = new BitSet(Integer.SIZE);
    expectedX.set(0);
    expectedX.set(2);
    Assert.assertEquals(expectedX, x);
    BitSet y = ByteAndBitConverter.intToBitSet(0);
    BitSet expectedY = new BitSet(Integer.SIZE);
    Assert.assertEquals(expectedY, y);
    BitSet z = ByteAndBitConverter.intToBitSet(-9);
    BitSet expectedZ = new BitSet(Integer.SIZE);
    expectedZ.set(3);
    expectedZ.flip(0, Integer.SIZE);
    Assert.assertEquals(expectedZ, z);
  }

  @Test
  public void testToBoolean() {
    boolean exception = false;
    try {
      ByteAndBitConverter.toBoolean("Foo");
    } catch (IllegalArgumentException e) {
      exception = true;
    }
    Assert.assertTrue(exception);
    Boolean[] x = ByteAndBitConverter.toBoolean("00");
    Boolean[] expectedX = new Boolean[] { false, false, false, false, false, false, false, false };
    Assert.assertEquals(expectedX.length, x.length);
    Assert.assertArrayEquals(expectedX, x);
    Boolean[] y = ByteAndBitConverter.toBoolean("42");
    Boolean[] expectedY = new Boolean[] { false, true, false, false, false, false, true, false };
    Assert.assertEquals(expectedY.length, y.length);
    Assert.assertArrayEquals(expectedY, y);
    Boolean[] z = ByteAndBitConverter.toBoolean("42FF");
    Boolean[] expectedZ = new Boolean[] { false, true, false, false, false, false, true, false,
        true, true, true, true, true, true, true, true };
    Assert.assertEquals(expectedZ.length, z.length);
    Assert.assertArrayEquals(expectedZ, z);
  }

  @Test
  public void testConstructor()
      throws NoSuchMethodException, SecurityException, InstantiationException,
      IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    // A hack to test a private constructor
    Constructor<ByteAndBitConverter> constructor =
        ByteAndBitConverter.class.getDeclaredConstructor();
    Assert.assertTrue(Modifier.isPrivate(constructor.getModifiers()));
    constructor.setAccessible(true);
    constructor.newInstance();
  }

  @Test
  public void testToHex() {
    Assert.assertThat(ByteAndBitConverter.toHex(
        new boolean[] { false, false, false, false, false, false, false, false }), Is.is("00"));
    Assert.assertThat(
        ByteAndBitConverter.toHex(new boolean[] { true, true, true, true, true, true, true, true }),
        Is.is("ff"));
    Assert.assertThat(
        ByteAndBitConverter.toHex(new boolean[] { true, true, true, true, true, true, true }),
        Is.is("7f"));
    Assert.assertThat(
        ByteAndBitConverter.toHex(new boolean[] { true, true, true, true, true, true }),
        Is.is("3f"));
    Assert.assertThat(ByteAndBitConverter.toHex(new boolean[] { true, true, true, true, true }),
        Is.is("1f"));
    Assert.assertThat(ByteAndBitConverter.toHex(new boolean[] { true, true, true, true }),
        Is.is("0f"));
    Assert.assertThat(
        ByteAndBitConverter
            .toHex(new boolean[] { false, true, true, true, true, true, true, true, true }),
        Is.is("00ff"));
    Assert.assertThat(ByteAndBitConverter.toHex(
        new boolean[] { true, true, true, true, true, true, true, true, true }), Is.is("01ff"));
    Assert.assertThat(
        ByteAndBitConverter.toHex(Arrays
            .asList((new Boolean[] { true, true, true, true, true, true, true, true, true }))),
        Is.is("01ff"));
  }
}

