package dk.alexandra.fresco.tools.bitTriples.utils;

import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class VectorOperationsTest {

  private Drbg drng;

  @Before
  public void setup() {
    Random rand = new Random(42);
    byte[] seed = new byte[32];
    rand.nextBytes(seed);
    this.drng = new AesCtrDrbg(seed);
  }

  @Test
  public void isZero() {
    StrictBitVector vector = new StrictBitVector(8);
    Assert.assertTrue(VectorOperations.isZero(vector));
  }

  @Test
  public void multiplyTrue() {
    StrictBitVector vector = new StrictBitVector(8, drng);
    StrictBitVector result = VectorOperations.multiply(vector, true);
    Assert.assertEquals(result, vector);
  }

  @Test
  public void multiplyFalse() {
    StrictBitVector vector = new StrictBitVector(8, drng);
    StrictBitVector result = VectorOperations.multiply(vector, false);
    Assert.assertEquals(result, new StrictBitVector(8));
  }

  @Test
  public void testMultiply() {
    List<StrictBitVector> arg1 = new ArrayList<>();
    for (int i = 0; i < 8; i++) {
      arg1.add(new StrictBitVector(8, drng));
    }
    StrictBitVector arg2 = new StrictBitVector(8, drng);
    List<StrictBitVector> result = VectorOperations.multiply(arg1, arg2);
    for (int i = 0; i < arg2.getSize(); i++) {
      if (arg2.getBit(i, false)) {
        Assert.assertEquals(arg1.get(i), result.get(i));
      } else {
        Assert.assertTrue(VectorOperations.isZero(result.get(i)));
      }
    }
  }

  @Test(expected = IllegalStateException.class)
  public void testMultiplyMustHaveSameLength() {
    List<StrictBitVector> arg1 = new ArrayList<>();
    for (int i = 0; i < 8; i++) {
      arg1.add(new StrictBitVector(8, drng));
    }
    StrictBitVector arg2 = new StrictBitVector(16, drng);
    VectorOperations.multiply(arg1, arg2);
  }

  @Test
  public void xorAll() {
    StrictBitVector vector = new StrictBitVector(8);
    Assert.assertFalse(VectorOperations.xorAll(vector));
    vector.setBit(0, true,false);
    Assert.assertTrue(VectorOperations.xorAll(vector));
    vector.setBit(1, true,false);
    Assert.assertFalse(VectorOperations.xorAll(vector));
    vector.setBit(2, true,false);
    Assert.assertTrue(VectorOperations.xorAll(vector));
  }

  @Test
  public void bitwiseXorTwo() {
    StrictBitVector v1 = new StrictBitVector(8);
    StrictBitVector v2 = new StrictBitVector(8);

    v1.setBit(1, true,false);

    List<StrictBitVector> arg = new ArrayList<>();
    arg.add(v1);
    arg.add(v2);

    Assert.assertTrue(VectorOperations.bitwiseXor(arg).getBit(1,false));
    Assert.assertFalse(VectorOperations.bitwiseXor(arg).getBit(0,false));
  }

  @Test
  public void bitwiseXorThree() {
    StrictBitVector v1 = new StrictBitVector(8);
    StrictBitVector v2 = new StrictBitVector(8);
    StrictBitVector v3 = new StrictBitVector(8);

    v1.setBit(1, true,false);
    v2.setBit(2, true,false);
    v3.setBit(1, true,false);

    List<StrictBitVector> arg = new ArrayList<>();
    arg.add(v1);
    arg.add(v2);
    arg.add(v3);

    Assert.assertFalse(VectorOperations.bitwiseXor(arg).getBit(0,false));
    Assert.assertFalse(VectorOperations.bitwiseXor(arg).getBit(1,false));
    Assert.assertTrue(VectorOperations.bitwiseXor(arg).getBit(2,false));
  }

  @Test
  public void xorIndex() {
    StrictBitVector v1 = new StrictBitVector(8);
    StrictBitVector v2 = new StrictBitVector(8);

    v1.setBit(1, true,false);
    v2.setBit(2, true,false);

    List<StrictBitVector> l1 = new ArrayList<>();
    l1.add(v1);
    List<StrictBitVector> l2 = new ArrayList<>();
    l2.add(v2);

    List<List<StrictBitVector>> arg = new ArrayList<>();
    arg.add(l1);
    arg.add(l2);

    Assert.assertFalse(VectorOperations.xorIndex(arg, 0).getBit(0,false));
    Assert.assertTrue(VectorOperations.xorIndex(arg, 0).getBit(1,false));
    Assert.assertTrue(VectorOperations.xorIndex(arg, 0).getBit(2,false));
  }

  @Test(expected = IllegalStateException.class)
  public void xorIndexShouldThrowIfListsAreNotOfSameSize() {
    StrictBitVector v1 = new StrictBitVector(8);
    StrictBitVector v2 = new StrictBitVector(8);
    StrictBitVector v3 = new StrictBitVector(8);

    v1.setBit(1, true );
    v2.setBit(2, true);
    v3.setBit(3, true);

    List<StrictBitVector> l1 = new ArrayList<>();
    l1.add(v1);
    l1.add(v2);
    List<StrictBitVector> l2 = new ArrayList<>();
    l2.add(v3);

    List<List<StrictBitVector>> arg = new ArrayList<>();
    arg.add(l1);
    arg.add(l2);

    VectorOperations.xorIndex(arg, 0);
  }

  @Test(expected = IllegalStateException.class)
  public void bitwiseAndThrowsIfDifferentSize() {
    StrictBitVector arg1 = new StrictBitVector(8);
    StrictBitVector arg2 = new StrictBitVector(16);
    VectorOperations.bitwiseAnd(arg1, arg2);
  }

  @Test
  public void bitwiseAnd() {
    StrictBitVector arg1 = new StrictBitVector(8);
    StrictBitVector arg2 = new StrictBitVector(8);

    arg1.setBit(1, true, false);
    arg1.setBit(2, true, false);
    arg2.setBit(2, true, false);

    StrictBitVector result = VectorOperations.bitwiseAnd(arg1, arg2);

    Assert.assertFalse(result.getBit(0));
    Assert.assertFalse(result.getBit(1));
    Assert.assertTrue(result.getBit(2));
  }
}
