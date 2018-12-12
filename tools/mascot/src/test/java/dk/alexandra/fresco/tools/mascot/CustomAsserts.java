package dk.alexandra.fresco.tools.mascot;

import dk.alexandra.fresco.framework.builder.numeric.FieldDefinition;
import dk.alexandra.fresco.framework.builder.numeric.FieldElement;
import dk.alexandra.fresco.tools.mascot.field.AuthenticatedElement;
import dk.alexandra.fresco.tools.mascot.field.MultiplicationTriple;
import java.math.BigInteger;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import org.hamcrest.collection.IsCollectionWithSize;
import org.hamcrest.core.Is;
import org.junit.Assert;

public class CustomAsserts {

  /**
   * Asserts that expected matrix is equal to actual.
   *
   * @param expected expected matrix
   * @param actual actual matrix
   */
  public static void assertMatrixEquals(List<List<FieldElement>> expected,
      List<List<FieldElement>> actual) {
    Assert.assertThat(expected, IsCollectionWithSize.hasSize(actual.size()));
    for (int i = 0; i < expected.size(); i++) {
      assertEquals(expected.get(i), actual.get(i));
    }
  }

  /**
   * Asserts that expected list of field elements is equal to actual.
   *
   * @param expected expected list
   * @param actual actual list
   */
  public static void assertEquals(List<FieldElement> expected, List<FieldElement> actual) {
    Function<Integer, BiConsumer<FieldElement, FieldElement>> innerAssert =
        (idx) -> (l, r) -> assertEqualsMessaged(" - at index " + idx, l, r);
    assertEquals(expected, actual, innerAssert);
  }

  private static <T> void assertEquals(List<T> expected, List<T> actual,
      Function<Integer, BiConsumer<T, T>> innerAssert) {
    Assert.assertThat(expected, IsCollectionWithSize.hasSize(actual.size()));
    for (int i = 0; i < expected.size(); i++) {
      innerAssert.apply(i).accept(expected.get(i), actual.get(i));
    }
  }

  public static void assertEquals(FieldElement expected, FieldElement actual) {
    assertEqualsMessaged("", expected, actual);
  }

  public static void assertEquals(AuthenticatedElement expected, AuthenticatedElement actual) {
    assertEqualsMessaged("", expected, actual);
  }

  /**
   * Asserts that expected list of field elements is equal to actual.
   *
   * @param expected expected list
   * @param actual actual list
   */
  public static void assertEqualsAuth(List<AuthenticatedElement> expected,
      List<AuthenticatedElement> actual) {
    Function<Integer, BiConsumer<AuthenticatedElement, AuthenticatedElement>> innerAssert =
        (idx) -> (l, r) -> assertEqualsMessaged(" - at index " + idx, l, r);
    assertEquals(expected, actual, innerAssert);
  }

  /**
   * Asserts that expected authenticated element equals actual.
   *
   * @param expected expected authenticated element
   * @param actual actual authenticated element
   */
  private static void assertEqualsMessaged(String message, AuthenticatedElement expected,
      AuthenticatedElement actual) {
    assertEqualsMessaged(" share mismatch " + message, expected.getShare(), actual.getShare());
    assertEqualsMessaged(" mac mismatch " + message, expected.getMac(), actual.getMac());
  }

  /**
   * Asserts that expected field element is equal to actual.
   *
   * @param message message if not equal
   * @param expected expected field element
   * @param actual actual field element
   */
  private static void assertEqualsMessaged(String message, FieldElement expected,
      FieldElement actual) {
    Assert.assertThat("Value mismatch" + message + " in " + actual, expected,
        Is.is(actual));
  }

  /**
   * Asserts that left and right factor are equal to product and that macs are correct.
   *
   * @param triple triple to check
   * @param macKey recombined mac key
   */
  public static void assertTripleIsValid(MultiplicationTriple triple, FieldElement macKey) {
    AuthenticatedElement left = triple.getLeft();
    AuthenticatedElement right = triple.getRight();
    AuthenticatedElement product = triple.getProduct();

    FieldElement leftValue = left.getShare();
    FieldElement rightValue = right.getShare();
    FieldElement productValue = product.getShare();
    assertEqualsMessaged("Shares of product do not equal product ", productValue,
        leftValue.multiply(rightValue));

    FieldElement leftMac = left.getMac();
    FieldElement rightMac = right.getMac();
    FieldElement productMac = product.getMac();
    assertEqualsMessaged("Mac check failed for left ", leftValue.multiply(macKey), leftMac);
    assertEqualsMessaged("Mac check failed for right ", rightValue.multiply(macKey), rightMac);
    assertEqualsMessaged("Mac check failed for product", productValue.multiply(macKey), productMac);
  }

  public static void assertFieldElementIsBit(
      FieldDefinition fieldDefinition, FieldElement actualBit) {
    BigInteger output = fieldDefinition.convertRepresentation(actualBit);
    String message = "Not a bit " + actualBit;
    Assert.assertTrue(message, output.equals(BigInteger.ZERO) || output.equals(BigInteger.ONE));
  }
}
