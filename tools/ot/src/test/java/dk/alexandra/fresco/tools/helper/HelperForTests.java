package dk.alexandra.fresco.tools.helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.util.StrictBitVector;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

public class HelperForTests {
  public static final byte[] seedOne = new byte[] { 0x42, 0x42, 0x42, 0x42,
      0x42,
      0x42,
      0x42, 0x42, 0x42, 0x42, 0x42, 0x42, 0x42, 0x42, 0x42, 0x42, 0x42, 0x42,
      0x42, 0x42, 0x42, 0x42, 0x42, 0x42, 0x42, 0x42, 0x42, 0x42, 0x42, 0x42,
      0x42, 0x42 };
  public static final byte[] seedTwo = new byte[] { 0x42, 0x00, 0x42, 0x42,
      0x42,
      0x42,
      0x42, 0x42, 0x42, 0x42, 0x42, 0x42, 0x42, 0x42, 0x42, 0x42, 0x42, 0x42,
      0x42, 0x42, 0x42, 0x42, 0x42, 0x42, 0x42, 0x42, 0x42, 0x42, 0x42, 0x42,
      0x42, 0x42 };
  public static final byte[] seedThree = new byte[] { 0x54, 0x00, 0x42, 0x42,
      0x42,
      0x42,
      0x42, 0x42, 0x42, 0x42, 0x42, 0x42, 0x42, 0x42, 0x42, 0x42, 0x42, 0x42,
      0x42, 0x42, 0x42, 0x42, 0x42, 0x42, 0x42, 0x42, 0x42, 0x42, 0x42, 0x42,
      0x42, 0x42 };

  /**
   * Helper method which uses reflection to change a private and final field
   * variable to a desired value.
   *
   * @param field
   *          The field of the class to change
   * @param newValue
   *          The new value the field should take
   * @param objectToWorkOn
   *          The object of which the field should be changed
   * @throws Exception
   *           Thrown if something goes wrong
   */
  public static void setFinalStatic(Field field, Object newValue,
      Object objectToWorkOn)
      throws Exception {
    field.setAccessible(true);

    Field modifiersField = Field.class.getDeclaredField("modifiers");
    modifiersField.setAccessible(true);
    modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

    field.set(objectToWorkOn, newValue);
  }

  /**
   * Checks correctness of the outcome of an OT protocol. Also executes a set of sanity checks, s.t.
   * messages are distinct and no message is a zero-string.
   *
   * @param senderResults
   *          The sender's output of a list of OT executions
   * @param receiverResults
   *          the receiver's output of a list of OT executions
   * @param choices
   *          The receiver's choices for the list of OT executions
   */
  public static void verifyOts(
      List<Pair<StrictBitVector, StrictBitVector>> senderResults,
      List<StrictBitVector> receiverResults, StrictBitVector choices) {
    // Check that choices are not the 0-string
    assertNotEquals(new StrictBitVector(choices.getSize()), choices);
    // Check the length the lists
    assertEquals(choices.getSize(), senderResults.size());
    assertEquals(choices.getSize(), senderResults.size());
    for (int i = 0; i < choices.getSize(); i++) {
      StrictBitVector msgZero = senderResults.get(i).getFirst();
      StrictBitVector msgOne = senderResults.get(i).getSecond();
      StrictBitVector recMsg = receiverResults.get(i);
      // Verify the receiver's result
      if (choices.getBit(i, false) == false) {
        assertTrue(msgZero.equals(recMsg));
      } else {
        assertTrue(msgOne.equals(recMsg));
      }
      // Check that the two sent messages are not the same
      assertNotEquals(msgZero, msgOne);
      // Check the messages are not 0-strings
      StrictBitVector zeroVec = new StrictBitVector(msgZero.getSize());
      assertNotEquals(zeroVec, msgZero);
      assertNotEquals(zeroVec, msgOne);
      assertNotEquals(zeroVec, recMsg);
      // Check that they are not all equal
      if (i > 0) {
        assertNotEquals(senderResults.get(i - 1).getFirst(), senderResults.get(
            i).getFirst());
        assertNotEquals(senderResults.get(i - 1).getSecond(), senderResults.get(
            i).getSecond());
        // The following check is needed to ensure that the senders messages are
        // actually randomized and that not only the receiver's choices are
        // randomized, as the receiver's choices affect the order of each pair of messages
        assertNotEquals(senderResults.get(i - 1).getFirst(), senderResults.get(
            i).getSecond());
      }
    }
  }
}
