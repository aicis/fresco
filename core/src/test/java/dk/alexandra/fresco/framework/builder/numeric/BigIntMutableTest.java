package dk.alexandra.fresco.framework.builder.numeric;

import static org.junit.Assert.assertThat;

import org.hamcrest.core.Is;
import org.junit.Test;

public class BigIntMutableTest {

  @Test
  public void serialize() {
    BigIntMutable bigIntMutable = new BigIntMutable(27);
    byte[] bytes = new byte[4];
    bigIntMutable.toByteArray(bytes, 3, 1);
    BigIntMutable after = BigIntMutable.fromBytes(bytes);
    assertThat(after.intValue(), Is.is(27));
  }

  @Test
  public void serialize2() {
    BigIntMutable bigIntMutable = new BigIntMutable(Long.MAX_VALUE);
    byte[] bytes = new byte[4];
    bigIntMutable.toByteArray(bytes, 0, 4);
    assertThat(bytes, Is.is(new byte[]{-1, -1, -1, -1}));
    BigIntMutable after = BigIntMutable.fromBytes(bytes);
    assertThat(after.longValue(), Is.is((1L << 32) - 1));
  }
}