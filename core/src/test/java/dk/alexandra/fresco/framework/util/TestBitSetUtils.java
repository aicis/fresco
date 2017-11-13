package dk.alexandra.fresco.framework.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import org.junit.Assert;
import org.junit.Test;

public class TestBitSetUtils {

  @Test
  public void testConstructor()
      throws NoSuchMethodException, SecurityException, InstantiationException,
      IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    // A hack to test a private constructor
    Constructor<BitSetUtils> constructor =
        BitSetUtils.class.getDeclaredConstructor();
    Assert.assertTrue(Modifier.isPrivate(constructor.getModifiers()));
    constructor.setAccessible(true);
    constructor.newInstance();
  }

  @Test
  public void testCopy() {
    BitSet x = BitSet.valueOf(new long[] { 0L });
    Assert.assertEquals(x, BitSetUtils.copy(x));
    BitSet y = BitSet.valueOf(new long[] { 12034L });
    Assert.assertEquals(y, BitSetUtils.copy(y));
    BitSet z = BitSet.valueOf(new long[] { -12034L });
    Assert.assertEquals(z, BitSetUtils.copy(z));
    BitSet w = BitSet.valueOf(new long[] { 1L });
    BitSet copy = BitSetUtils.copy(w);
    w.set(1);
    Assert.assertNotEquals(w, copy);
  }

  @Test
  public void testGetRandomBits() {
    final int limit = 100;
    Random rand1 = new Random(0);
    BitSet b = BitSetUtils.getRandomBits(limit, rand1);
    Random rand2 = new Random(0);
    for (int i = 0; i < limit; i++) {
      Assert.assertEquals(rand2.nextBoolean(), b.get(i));
    }
  }

  @Test
  public void testToString() {
    BitSet bitSet = new BitSet(8);
    bitSet.set(0);
    bitSet.set(5);
    bitSet.set(7);
    Assert.assertEquals("1 0 0 0 0 1 0 1 ", BitSetUtils.toString(bitSet));
    Assert.assertEquals("", BitSetUtils.toString(bitSet, 0));
    Assert.assertEquals("1 0 0 ", BitSetUtils.toString(bitSet, 3));
    Assert.assertEquals("", BitSetUtils.toString(bitSet, -1));
  }

  @Test
  public void testInnerProduct() {
    BitSet a = new BitSet(8);
    a.set(0);
    a.set(5);
    a.set(7);
    BitSet b = new BitSet(8);
    b.set(0);
    b.set(5);
    Assert.assertFalse(BitSetUtils.innerProduct(a, b));
    b.set(1);
    Assert.assertFalse(BitSetUtils.innerProduct(a, b));
    b.set(7);
    Assert.assertTrue(BitSetUtils.innerProduct(a, b));
    BitSet c = new BitSet(10);
    Assert.assertFalse(BitSetUtils.innerProduct(a, c));
    c.set(10);
    Assert.assertFalse(BitSetUtils.innerProduct(a, c));
    c.set(0);
    Assert.assertTrue(BitSetUtils.innerProduct(a, c));
  }

  @Test
  public void testFromList() {
    Boolean[] array = new Boolean[] { true, false, true, false, false, false, true };
    List<Boolean> list = Arrays.asList(array);
    BitSet bitSet = BitSetUtils.fromList(list);
    for (int i = 0; i < array.length; i++) {
      Assert.assertEquals(array[i], bitSet.get(i));
    }
    List<Boolean> emptyList = new LinkedList<>();
    BitSet emptyBitSet = BitSetUtils.fromList(emptyList);
    Assert.assertTrue(emptyBitSet.isEmpty());
  }

  @Test
  public void testFromArray() {
    boolean[] array = new boolean[] { true, false, true, false, false, false, true };
    BitSet bitSet = BitSetUtils.fromArray(array);
    for (int i = 0; i < array.length; i++) {
      Assert.assertEquals(array[i], bitSet.get(i));
    }
  }

  @Test
  public void testToList() {
    int[] indices = new int[] { 0, 1, 4, 10, 6, 9, 11 };
    BitSet bitSet = new BitSet(20);
    for (int i: indices) {
      bitSet.set(i);
    }
    List<Boolean> list = BitSetUtils.toList(bitSet, bitSet.size());
    Assert.assertEquals(bitSet.size(), list.size());
    for (int i = 0; i < bitSet.size(); i++) {
      Assert.assertEquals(bitSet.get(i), list.get(i));
    }
    List<Boolean> shortList = BitSetUtils.toList(bitSet, 5);
    Assert.assertEquals(5, shortList.size());
    for (int i = 0; i < 5; i++) {
      Assert.assertEquals(bitSet.get(i), shortList.get(i));
    }
    List<Boolean> longList = BitSetUtils.toList(bitSet, bitSet.size() + 10);
    Assert.assertEquals(bitSet.size() + 10, longList.size());
    for (int i = 0; i < bitSet.size() + 10; i++) {
      Assert.assertEquals(bitSet.get(i), longList.get(i));
    }
    boolean exception = false;
    try {
      BitSetUtils.toList(bitSet, -2);
    } catch (IllegalArgumentException e) {
      exception = true;
    }
    Assert.assertTrue(exception);
  }

  @Test
  public void testToArray() {
    int[] indices = new int[] { 0, 1, 4, 10, 6, 9, 11 };
    BitSet bitSet = new BitSet(20);
    for (int i: indices) {
      bitSet.set(i);
    }
    boolean[] array = BitSetUtils.toArray(bitSet, bitSet.size());
    Assert.assertEquals(bitSet.size(), array.length);
    for (int i = 0; i < bitSet.size(); i++) {
      Assert.assertEquals(bitSet.get(i), array[i]);
    }
    boolean[] shortArray = BitSetUtils.toArray(bitSet, 5);
    Assert.assertEquals(5, shortArray.length);
    for (int i = 0; i < 5; i++) {
      Assert.assertEquals(bitSet.get(i), shortArray[i]);
    }
    boolean[] longArray = BitSetUtils.toArray(bitSet, bitSet.size() + 10);
    Assert.assertEquals(bitSet.size() + 10, longArray.length);
    for (int i = 0; i < bitSet.size() + 10; i++) {
      Assert.assertEquals(bitSet.get(i), longArray[i]);
    }
    boolean exception = false;
    try {
      BitSetUtils.toArray(bitSet, -2);
    } catch (IllegalArgumentException e) {
      exception = true;
    }
    Assert.assertTrue(exception);
  }

}
