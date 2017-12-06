package dk.alexandra.fresco.tools.mascot.field;

import static org.junit.Assert.assertEquals;

import java.math.BigInteger;
import java.util.List;

import org.junit.Test;

import dk.alexandra.fresco.tools.mascot.MascotTestUtils;

public class TestFieldElementCollectionUtils {

  static final BigInteger modulus = new BigInteger("65521");
  static final int modBitLength = 16;

  @Test
  public void testPairWiseMultiply() {
    int[] leftArr = {1, 2, 3, 4};
    List<FieldElement> left = MascotTestUtils.generateSingleRow(leftArr, modulus, modBitLength);

    int[] rightArr = {5, 6, 7, 8};
    List<FieldElement> right = MascotTestUtils.generateSingleRow(rightArr, modulus, modBitLength);

    int[] expectedArr = {5, 12, 21, 32};
    List<FieldElement> expected =
        MascotTestUtils.generateSingleRow(expectedArr, modulus, modBitLength);

    List<FieldElement> actual = FieldElementCollectionUtils.pairWiseMultiply(left, right);
    assertEquals(expected, actual);
  }
  
  @Test
  public void testInnerProduct() {
    int[] leftArr = {1, 2, 3, 4};
    List<FieldElement> left = MascotTestUtils.generateSingleRow(leftArr, modulus, modBitLength);

    int[] rightArr = {5, 6, 7, 8};
    List<FieldElement> right = MascotTestUtils.generateSingleRow(rightArr, modulus, modBitLength);

    FieldElement expected = new FieldElement(70, modulus, modBitLength);
        
  }

}
