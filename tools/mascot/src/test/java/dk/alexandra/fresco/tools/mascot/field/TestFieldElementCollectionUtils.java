package dk.alexandra.fresco.tools.mascot.field;

import static org.junit.Assert.assertEquals;

import java.math.BigInteger;
import java.util.List;

import org.junit.Test;

import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.mascot.MascotTestUtils;

public class TestFieldElementCollectionUtils {

  static final BigInteger modulus = new BigInteger("65521");
  static final int modBitLength = 16;
  static final int[] leftArr = {1, 2, 3, 4};
  static final List<FieldElement> left =
      MascotTestUtils.generateSingleRow(leftArr, modulus, modBitLength);
  static final int[] rightArr = {5, 6, 7, 8};
  static final List<FieldElement> right =
      MascotTestUtils.generateSingleRow(rightArr, modulus, modBitLength);


  @Test
  public void testPairWiseMultiply() {
    int[] expectedArr = {5, 12, 21, 32};
    List<FieldElement> expected =
        MascotTestUtils.generateSingleRow(expectedArr, modulus, modBitLength);

    List<FieldElement> actual = FieldElementCollectionUtils.pairWiseMultiply(left, right);
    assertEquals(expected, actual);
  }

  @Test
  public void testInnerProduct() {
    FieldElement expected = new FieldElement(70, modulus, modBitLength);
    FieldElement actual = FieldElementCollectionUtils.innerProduct(left, right);
    assertEquals(expected, actual);
  }

  @Test
  public void testRecombineCacheHit() {
    FieldElement actual = FieldElementCollectionUtils.recombine(left, modulus, modBitLength);
    assertEquals(new FieldElement(49, modulus, modBitLength), actual);
  }

  @Test
  public void testRecombineCacheMiss() {
    BigInteger missingMod = new BigInteger("251");
    int missingLength = 8;
    assertEquals(false, FieldElementGeneratorCache.isCached(missingMod));
    int[] leftArr = {1, 2, 3, 4};
    List<FieldElement> left = MascotTestUtils.generateSingleRow(leftArr, missingMod, missingLength);
    FieldElement actual = FieldElementCollectionUtils.recombine(left, missingMod, missingLength);
    assertEquals(new FieldElement(49, missingMod, missingLength), actual);
  }

  @Test
  public void testStretch() {
    int[] expectedArr = {1, 1, 2, 2, 3, 3, 4, 4};
    List<FieldElement> expected =
        MascotTestUtils.generateSingleRow(expectedArr, modulus, modBitLength);
    List<FieldElement> actual = FieldElementCollectionUtils.stretch(left, 2);
    assertEquals(expected, actual);
  }

  @Test
  public void testPadWith() {
    int[] expectedArr = {1, 2, 3, 4, 0, 0};
    List<FieldElement> expected =
        MascotTestUtils.generateSingleRow(expectedArr, modulus, modBitLength);
    FieldElement pad = new FieldElement(0, modulus, modBitLength);
    List<FieldElement> actual = FieldElementCollectionUtils.padWith(left, pad, 2);
    assertEquals(expected, actual);
  }

  // negative tests

  @Test
  public void testInnerProductDifferentSizes() {
    int[] rightArr = {5, 6, 7};
    List<FieldElement> right = MascotTestUtils.generateSingleRow(rightArr, modulus, modBitLength);

    boolean thrown = false;
    try {
      FieldElementCollectionUtils.innerProduct(left, right);
    } catch (IllegalArgumentException e) {
      assertEquals("Lists must have same size", e.getMessage());
      thrown = true;
    }
    assertEquals(thrown, true);
  }

  @Test
  public void testRecombineIncorrectSize() {
    int[] leftArr = new int[17];
    List<FieldElement> left = MascotTestUtils.generateSingleRow(leftArr, modulus, modBitLength);
    boolean thrown = false;
    try {
      FieldElementCollectionUtils.recombine(left, modulus, modBitLength);
    } catch (IllegalArgumentException e) {
      assertEquals("Number of elements cannot exceed bit-length", e.getMessage());
      thrown = true;
    }
    assertEquals(thrown, true);
  }

  @Test
  public void testPack() {
    StrictBitVector actual = FieldElementCollectionUtils.pack(left);
    byte[] expectedBytes = {0x00, 0x04, 0x00, 0x03, 0x00, 0x02, 0x00, 0x01};
    StrictBitVector expected = new StrictBitVector(expectedBytes, modBitLength * left.size());
    assertEquals(expected, actual);
  }

}
