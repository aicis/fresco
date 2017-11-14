package dk.alexandra.fresco.framework.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

import dk.alexandra.fresco.framework.MPCException;
import java.util.Arrays;
import org.junit.Test;

public class TestDetermSecureRandom {

  @Test
  public void testNextBytes() {
    byte[] bytes = new byte[] { 0x10, 0x09, 0x01 };
    DetermSecureRandom rand1 = new DetermSecureRandom(3, bytes);
    DetermSecureRandom rand2 = new DetermSecureRandom(3, bytes);
    DetermSecureRandom rand3 = new DetermSecureRandom();
    byte[] randBytes1 = new byte[10];
    byte[] randBytes2 = new byte[10];
    byte[] randBytes3 = new byte[10];
    rand1.nextBytes(randBytes1);
    rand2.nextBytes(randBytes2);
    rand3.nextBytes(randBytes3);
    assertArrayEquals(randBytes1, randBytes2);
    assertTrue(!Arrays.equals(randBytes1, randBytes3));
  }

  @SuppressWarnings("unused")
  @Test
  public void testDetermSecureConstructor() {
    byte[] bytes = new byte[] { 0x10, 0x09, 0x01 };
    DetermSecureRandom rand1 = new DetermSecureRandom(3, bytes);
    DetermSecureRandom rand2 = new DetermSecureRandom();
    boolean exception;
    exception = false;
    try {
      new DetermSecureRandom(-1, bytes);
    } catch (IllegalArgumentException e) {
      exception = true;
    }
    assertTrue(exception);
    exception = false;
    try {
      new DetermSecureRandom(33, bytes);
    } catch (IllegalArgumentException e) {
      exception = true;
    }
    assertTrue(exception);
    exception = false;
    try {
      new DetermSecureRandom(3, bytes, "BLA");
    } catch (MPCException e) {
      exception = true;
    }
    assertTrue(exception);
  }
}
