package dk.alexandra.fresco.tools.mascot.field;

import static org.junit.Assert.assertEquals;

import dk.alexandra.fresco.framework.builder.numeric.field.BigIntegerFieldDefinition;
import dk.alexandra.fresco.framework.builder.numeric.field.BigIntegerModulus;
import dk.alexandra.fresco.framework.builder.numeric.field.FieldElement;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.mascot.CustomAsserts;
import dk.alexandra.fresco.tools.mascot.MascotTestUtils;
import java.math.BigInteger;
import java.util.List;
import org.junit.Test;

public class TestMascotFieldElementUtils {

  private final BigInteger modulus = new BigInteger("65521");
  private final BigIntegerFieldDefinition definition = new BigIntegerFieldDefinition(
      new BigIntegerModulus(modulus));
  private final FieldElementUtils fieldElementUtils = new FieldElementUtils(definition);
  private final int[] leftArr = {1, 2, 3, 4};
  private final List<FieldElement> left =
      MascotTestUtils.generateSingleRow(leftArr, definition);
  private final int[] rightArr = {5, 6, 7, 8};
  private final List<FieldElement> right =
      MascotTestUtils.generateSingleRow(rightArr, definition);

  @Test
  public void testPairWiseMultiply() {
    int[] expectedArr = {5, 12, 21, 32};
    List<FieldElement> expected =
        MascotTestUtils.generateSingleRow(expectedArr, definition);

    List<FieldElement> actual = fieldElementUtils.pairWiseMultiply(left, right);
    CustomAsserts.assertEquals(expected, actual);
  }

  @Test
  public void testScalarMultiply() {
    int[] expectedArr = {2, 4, 6, 8};
    List<FieldElement> expected =
        MascotTestUtils.generateSingleRow(expectedArr, definition);
    List<FieldElement> actual =
        fieldElementUtils.scalarMultiply(left, definition.createElement(2));
    CustomAsserts.assertEquals(expected, actual);
  }

  @Test
  public void testInnerProduct() {
    FieldElement expected = definition.createElement(70);
    FieldElement actual = fieldElementUtils.innerProduct(left, right);
    CustomAsserts.assertEquals(expected, actual);
  }

  @Test
  public void testRecombine() {
    FieldElement actual = fieldElementUtils.recombine(left);
    CustomAsserts.assertEquals(definition.createElement(49), actual);
  }

  @Test
  public void testStretch() {
    int[] expectedArr = {1, 1, 2, 2, 3, 3, 4, 4};
    List<FieldElement> expected =
        MascotTestUtils.generateSingleRow(expectedArr, definition);
    List<FieldElement> actual = fieldElementUtils.stretch(left, 2);
    CustomAsserts.assertEquals(expected, actual);
  }

  @Test
  public void testPadWith() {
    int[] expectedArr = {1, 2, 3, 4, 0, 0};
    List<FieldElement> expected =
        MascotTestUtils.generateSingleRow(expectedArr, definition);
    FieldElement pad = definition.createElement(0);
    List<FieldElement> actual = fieldElementUtils.padWith(left, pad, 2);
    CustomAsserts.assertEquals(expected, actual);
  }

  @Test
  public void testPack() {
    StrictBitVector actual = fieldElementUtils.pack(left, true);
    byte[] expectedBytes = {0x00, 0x04, 0x00, 0x03, 0x00, 0x02, 0x00, 0x01};
    StrictBitVector expected = new StrictBitVector(expectedBytes);
    assertEquals(expected, actual);
  }
}
