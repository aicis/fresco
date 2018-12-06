package dk.alexandra.fresco.tools.mascot.field;

import static org.junit.Assert.assertEquals;

import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.mascot.CustomAsserts;
import dk.alexandra.fresco.tools.mascot.MascotTestUtils;
import java.math.BigInteger;
import java.util.List;

import org.junit.Test;

public class TestMascotFieldElementUtils {

  private final BigInteger modulus = new BigInteger("65521");
  private final int modBitLength = 16;
  private final FieldElementUtils fieldElementUtils = new FieldElementUtils(modulus);
  private final int[] leftArr = {1, 2, 3, 4};
  private final List<MascotFieldElement> left =
      MascotTestUtils.generateSingleRow(leftArr, modulus);
  private final int[] rightArr = {5, 6, 7, 8};
  private final List<MascotFieldElement> right =
      MascotTestUtils.generateSingleRow(rightArr, modulus);


  @Test
  public void testPairWiseMultiply() {
    int[] expectedArr = {5, 12, 21, 32};
    List<MascotFieldElement> expected =
        MascotTestUtils.generateSingleRow(expectedArr, modulus);

    List<MascotFieldElement> actual = fieldElementUtils.pairWiseMultiply(left, right);
    CustomAsserts.assertEquals(expected, actual);
  }

  @Test
  public void testScalarMultiply() {
    int[] expectedArr = {2, 4, 6, 8};
    List<MascotFieldElement> expected =
        MascotTestUtils.generateSingleRow(expectedArr, modulus);
    List<MascotFieldElement> actual =
        fieldElementUtils.scalarMultiply(left, new MascotFieldElement(2, modulus));
    CustomAsserts.assertEquals(expected, actual);
  }

  @Test
  public void testInnerProduct() {
    MascotFieldElement expected = new MascotFieldElement(70, modulus);
    MascotFieldElement actual = fieldElementUtils.innerProduct(left, right);
    CustomAsserts.assertEquals(expected, actual);
  }

  @Test
  public void testRecombine() {
    MascotFieldElement actual = fieldElementUtils.recombine(left);
    CustomAsserts.assertEquals(new MascotFieldElement(49, modulus), actual);
  }

  @Test
  public void testStretch() {
    int[] expectedArr = {1, 1, 2, 2, 3, 3, 4, 4};
    List<MascotFieldElement> expected =
        MascotTestUtils.generateSingleRow(expectedArr, modulus);
    List<MascotFieldElement> actual = fieldElementUtils.stretch(left, 2);
    CustomAsserts.assertEquals(expected, actual);
  }

  @Test
  public void testPadWith() {
    int[] expectedArr = {1, 2, 3, 4, 0, 0};
    List<MascotFieldElement> expected =
        MascotTestUtils.generateSingleRow(expectedArr, modulus);
    MascotFieldElement pad = new MascotFieldElement(0, modulus);
    List<MascotFieldElement> actual = fieldElementUtils.padWith(left, pad, 2);
    CustomAsserts.assertEquals(expected, actual);
  }

  @Test
  public void testPack() {
    StrictBitVector actual = fieldElementUtils.pack(left, true);
    byte[] expectedBytes = {0x00, 0x04, 0x00, 0x03, 0x00, 0x02, 0x00, 0x01};
    StrictBitVector expected = new StrictBitVector(expectedBytes);
    assertEquals(expected, actual);
  }

  // negative tests

  @Test(expected = IllegalArgumentException.class)
  public void testRecombineWrongModulus() {
    BigInteger missingMod = new BigInteger("251");
    int[] leftArr = {1, 2, 3, 4};
    List<MascotFieldElement> left = MascotTestUtils.generateSingleRow(leftArr, missingMod);
    fieldElementUtils.recombine(left);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInnerProductDifferentSizes() {
    int[] rightArr = {5, 6, 7};
    List<MascotFieldElement> right = MascotTestUtils.generateSingleRow(rightArr, modulus);
    fieldElementUtils.innerProduct(left, right);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testRecombineIncorrectSize() {
    int[] leftArr = new int[17];
    List<MascotFieldElement> left = MascotTestUtils.generateSingleRow(leftArr, modulus);
    fieldElementUtils.recombine(left);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testPairwiseMultiplyIncorrectSize() {
    int[] leftArr = new int[17];
    List<MascotFieldElement> left = MascotTestUtils.generateSingleRow(leftArr, modulus);
    fieldElementUtils.pairWiseMultiply(left, right);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUnpackIncorrectSize() {
    byte[] packed = new byte[17];
    fieldElementUtils.unpack(packed);
  }

}
