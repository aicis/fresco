package dk.alexandra.fresco.tools.ot.otextension;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.NoSuchAlgorithmException;
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

  @Test(expected = NoSuchAlgorithmException.class)
  public void testAdjustDigestNotSupported() throws Throwable {
    try {
      Method m = LengthAdjustment.class.getDeclaredMethod("getDigest", String.class);
      m.setAccessible(true);
      m.invoke(LengthAdjustment.class, "Test");
    } catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException e) {
      e.printStackTrace();
      throw e;
    } catch (InvocationTargetException f) {
      throw f.getCause().getCause();
    } finally {
      Method m = LengthAdjustment.class.getDeclaredMethod("getDigest", String.class);
      m.setAccessible(false);
    }
  }

  private void testShorterCandidateLength(final int candidateLength, final int adjustedLength) {
    byte[] candidate = new byte[candidateLength];
    new Random().nextBytes(candidate);
    byte[] adjusted1 = LengthAdjustment.adjust(candidate, adjustedLength);
    assertEquals(adjustedLength, adjusted1.length);
    byte[] adjusted2 = LengthAdjustment.adjust(candidate, adjustedLength);
    assertArrayEquals(adjusted1, adjusted2);
    assertFalse(Arrays.equals(new byte[adjusted1.length], adjusted1));
  }

}
