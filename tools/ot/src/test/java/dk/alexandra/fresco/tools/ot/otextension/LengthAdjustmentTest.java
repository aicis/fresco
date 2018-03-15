package dk.alexandra.fresco.tools.ot.otextension;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Random;
import org.junit.Test;

public class LengthAdjustmentTest {

  @Test
  public void testAdjustLongCandidate() {
    final int candidateLength = 10;
    final int adjustedLength = 5;
    testLongerOrEqualCandidateLength(candidateLength, adjustedLength);
  }

  @Test
  public void testAdjustEqualLengthCandidate() {
    final int candidateLength = 5;
    final int adjustedLength = 5;
    testLongerOrEqualCandidateLength(candidateLength, adjustedLength);
  }

  @Test
  public void testZeroLength() {
    final int candidateLength = 0;
    final int adjustedLength = 0;
    testLongerOrEqualCandidateLength(candidateLength, adjustedLength);
  }

  @Test
  public void testZeroLength2() {
    final int candidateLength = 10;
    final int adjustedLength = 0;
    testLongerOrEqualCandidateLength(candidateLength, adjustedLength);
  }

  private void testLongerOrEqualCandidateLength(final int candidateLength,
      final int adjustedLength) {
    byte[] candidate = new byte[candidateLength];
    new Random().nextBytes(candidate);
    byte[] adjusted = LengthAdjustment.adjust(candidate, adjustedLength);
    assertArrayEquals(Arrays.copyOf(candidate, adjustedLength), adjusted);
  }

  @Test
  public void testAdjustShortCandidate() {
    final int candidateLength = 5;
    final int adjustedLength = 10;
    testShorterCandidateLength(candidateLength, adjustedLength);
  }

  @Test
  public void testAdjustLongStrech() {
    final int candidateLength = 5;
    final int adjustedLength = 32;
    testShorterCandidateLength(candidateLength, adjustedLength);
  }

  @Test
  public void testAdjustEmptyCandidate() {
    final int candidateLength = 0;
    final int adjustedLength = 10;
    testShorterCandidateLength(candidateLength, adjustedLength);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAdjustNegativeLength() {
    final int candidateLength = 5;
    final int adjustedLength = -10;
    testShorterCandidateLength(candidateLength, adjustedLength);
  }

  @Test(expected = NullPointerException.class)
  public void testAdjustNullCandidate() {
    LengthAdjustment.adjust(null, 10);
  }

  @Test(expected = RuntimeException.class)
  public void testAdjustDigestNotSupported() throws IllegalArgumentException,
      IllegalAccessException, NoSuchFieldException, SecurityException {
    Field field = null;
    try {
      field = LengthAdjustment.class.getDeclaredField("DIGEST_ALGO");
      field.setAccessible(true);
      Field modifiersField = Field.class.getDeclaredField("modifiers");
      modifiersField.setAccessible(true);
      modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
      field.set(null, "NONEXISTING_DIGEST_ALGO");
      LengthAdjustment.adjust(new byte[] { 0x00 }, 10);
    } finally {
      if (field != null) {
        // Clean up reflection mess
        field.set(null, "SHA-256");
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & Modifier.FINAL);
        modifiersField.setAccessible(false);
        field.setAccessible(false);
      }
    }
  }

  private void testShorterCandidateLength(final int candidateLength, final int adjustedLength) {
    byte[] candidate = new byte[candidateLength];
    new Random().nextBytes(candidate);
    byte[] adjusted1 = LengthAdjustment.adjust(candidate, adjustedLength);
    assertEquals(adjustedLength, adjusted1.length);
    byte[] adjusted2 = LengthAdjustment.adjust(candidate, adjustedLength);
    assertArrayEquals(adjusted1, adjusted2);

  }

}
