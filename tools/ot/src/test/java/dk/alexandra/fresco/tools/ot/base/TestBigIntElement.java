package dk.alexandra.fresco.tools.ot.base;

import org.junit.Test;

import java.math.BigInteger;
import java.util.Arrays;

import static org.junit.Assert.*;

public class TestBigIntElement {

  @Test
  public void testToByteArray() {
    BigInteger x = BigInteger.valueOf(127);
    BigInteger m = BigInteger.valueOf(128);
    BigIntElement val = new BigIntElement(x, m);
    byte[] bytes = val.toByteArray();
    assertArrayEquals(bytes, new byte[]{127});

    // Should just be able to fit
    x = BigInteger.valueOf(255);
    m = BigInteger.valueOf(256);
    val = new BigIntElement(x, m);
    bytes = val.toByteArray();
    assertArrayEquals(bytes, new byte[]{-1});

    // Should be one byte longer
    x = BigInteger.valueOf(256);
    m = BigInteger.valueOf(257);
    val = new BigIntElement(x, m);
    bytes = val.toByteArray();
    assertArrayEquals(bytes, new byte[]{1, 0});

    // Should be length of modulo
    x = new BigInteger(1, new byte[]{-1, -1});
    m = BigInteger.valueOf(2 << 29);
    val = new BigIntElement(x, m);
    bytes = val.toByteArray();
    assertArrayEquals(bytes, new byte[]{0, 0, -1, -1});
  }
}
