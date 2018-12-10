package dk.alexandra.fresco.framework.builder.numeric;

import static org.junit.Assert.assertThat;

import org.hamcrest.core.Is;
import org.junit.Test;

public class MersennePrimeIntegerTest {

  @Test
  public void serialize() {
    MersennePrimeInteger mersennePrimeInteger = new MersennePrimeInteger(27);
    byte[] bytes = new byte[4];
    mersennePrimeInteger.toByteArray(bytes, 3, 1);
    MersennePrimeInteger after = MersennePrimeInteger.fromBytes(bytes);
    assertThat(after.intValue(), Is.is(27));
  }

  @Test
  public void serialize2() {
    MersennePrimeInteger mersennePrimeInteger = new MersennePrimeInteger(Long.MAX_VALUE);
    byte[] bytes = new byte[4];
    mersennePrimeInteger.toByteArray(bytes, 0, 4);
    assertThat(bytes, Is.is(new byte[]{-1, -1, -1, -1}));
    MersennePrimeInteger after = MersennePrimeInteger.fromBytes(bytes);
    assertThat(after.longValue(), Is.is((1L << 32) - 1));
  }
}