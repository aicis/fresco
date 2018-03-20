package dk.alexandra.fresco.tools.ot.otextension;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Arrays;
import java.util.Random;
import org.junit.Test;

public class PseudoOtpTest {

  @Test
  public void testLongCandidate() {
    int candidateLength = 100;
    int messageLength = 40;
    int cipherLength = 50;
    testEncryptDecrypt(messageLength, candidateLength, cipherLength);
  }

  @Test
  public void testLongCandidateNoCipherLength() {
    int candidateLength = 100;
    int messageLength = 40;
    testEncryptDecrypt(messageLength, candidateLength);
  }

  @Test
  public void testShortCandidateNoCipherLength() {
    int candidateLength = 40;
    int messageLength = 100;
    testEncryptDecrypt(messageLength, candidateLength);
  }

  @Test
  public void testLongMessage1() {
    int candidateLength = 60;
    int messageLength = 100;
    int cipherLength = 50;
    testEncryptDecrypt(messageLength, candidateLength, cipherLength);
  }

  @Test
  public void testLongCipher() {
    int candidateLength = 50;
    int messageLength = 40;
    int cipherLength = 100;
    testEncryptDecrypt(messageLength, candidateLength, cipherLength);
  }

  @Test
  public void testZeroMessageLength() {
    int candidateLength = 50;
    int messageLength = 0;
    int cipherLength = 100;
    testEncryptDecrypt(messageLength, candidateLength, cipherLength);
  }

  @Test
  public void testZeroCandidateLength() {
    int candidateLength = 0;
    int messageLength = 40;
    int cipherLength = 100;
    testEncryptDecrypt(messageLength, candidateLength, cipherLength);
  }

  @Test
  public void testZeroCipherLength() {
    int candidateLength = 50;
    int messageLength = 40;
    int cipherLength = 0;
    testEncryptDecrypt(messageLength, candidateLength, cipherLength);
  }

  @Test (expected = IllegalArgumentException.class)
  public void testNegativeCipherLength() {
    int candidateLength = 50;
    int messageLength = 40;
    int cipherLength = -100;
    testEncryptDecrypt(messageLength, candidateLength, cipherLength);
  }

  @Test (expected = NullPointerException.class)
  public void testEncryptNullMessage() {
    PseudoOtp.encrypt(null, new byte[] { 0x00 });
  }

  @Test (expected = NullPointerException.class)
  public void testEncryptNullCandidate() {
    PseudoOtp.encrypt(new byte[] { 0x00 }, null);
  }

  @Test (expected = NullPointerException.class)
  public void testDecryptNullCipher() {
    PseudoOtp.decrypt(null, new byte[] { 0x00 });
  }

  @Test (expected = NullPointerException.class)
  public void testDecryptNullCandidate() {
    PseudoOtp.encrypt(new byte[] { 0x00 }, null);
  }

  private void testEncryptDecrypt(int messageLength, int candidateLength) {
    Random rand = new Random();
    byte[] message = new byte[messageLength];
    rand.nextBytes(message);
    byte[] candidate = new byte[candidateLength];
    rand.nextBytes(candidate);
    byte[] cipherText = PseudoOtp.encrypt(message, candidate);
    assertEquals(messageLength, cipherText.length);
    assertFalse(Arrays.equals(message, cipherText));
    assertFalse(Arrays.equals(Arrays.copyOf(candidate, messageLength), cipherText));
    byte[] decryptedMessage = PseudoOtp.decrypt(cipherText, candidate);
    assertArrayEquals(message, decryptedMessage);
  }

  private void testEncryptDecrypt(int messageLength, int candidateLength, int cipherLength) {
    Random rand = new Random();
    byte[] message = new byte[messageLength];
    rand.nextBytes(message);
    byte[] candidate = new byte[candidateLength];
    rand.nextBytes(candidate);
    byte[] cipherText = PseudoOtp.encrypt(message, candidate, cipherLength);
    assertEquals(cipherLength, cipherText.length);
    if (cipherText.length > 0) {
      assertFalse(Arrays.equals(Arrays.copyOf(message, cipherLength), cipherText));
      assertFalse(Arrays.equals(Arrays.copyOf(candidate, cipherLength), cipherText));
    }
    byte[] decryptedMessage = PseudoOtp.decrypt(cipherText, candidate, cipherLength);
    assertArrayEquals(Arrays.copyOf(message, cipherLength), decryptedMessage);
  }

}
