package dk.alexandra.fresco.framework.util;

/** Contains methods for validating ids. */
public final class ValidationUtils {

  private ValidationUtils() {}

  /**
   * Validates that the given party id is within the valid range of ids, without a known max id.
   *
   * @param partyId Id to validate
   * @exception IllegalArgumentException if id is not valid
   */
  public static void assertValidId(int partyId) {
    if (partyId < 1) {
      throw new IllegalArgumentException(String.format("Party id %d must be one-indexed", partyId));
    }
  }

  /**
   * Validates that the given party id is within the valid range of ids, with a known max id.
   *
   * @param partyId Id to validate
   * @param numParties Max id
   * @exception IllegalArgumentException if id is not valid
   */
  public static void assertValidId(int partyId, int numParties) {
    assertValidId(partyId);
    if (numParties < partyId) {
      throw new IllegalArgumentException(
          String.format("Party id %d must be in range [1,%d]", partyId, numParties));
    }
  }
}
