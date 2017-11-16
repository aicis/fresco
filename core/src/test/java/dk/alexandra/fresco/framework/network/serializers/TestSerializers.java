package dk.alexandra.fresco.framework.network.serializers;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests that the Serializers works as expected.
 */
public class TestSerializers {

  @Test
  public void testBigIntegerWithFixedLengthSerializer() {
    BigInteger mod = new BigInteger("1298376217321832134223");

    BigInteger b = new BigInteger("1298376217321832");
    int modulusSize = mod.toByteArray().length;
    byte[] bytes = BigIntegerWithFixedLengthSerializer.toBytes(b, modulusSize);
    ByteBuffer buf = ByteBuffer.wrap(bytes);
    BigInteger bb = BigIntegerWithFixedLengthSerializer.toBigInteger(buf, modulusSize);
    Assert.assertEquals(b, bb);

    b = BigInteger.ZERO;
    bytes = BigIntegerWithFixedLengthSerializer.toBytes(b, modulusSize);
    buf = ByteBuffer.wrap(bytes);
    bb = BigIntegerWithFixedLengthSerializer.toBigInteger(buf, modulusSize);
    Assert.assertEquals(b, bb);
  }

  @Test
  public void testBooleanSerializer() {
    boolean b = true;
    byte bytes = BooleanSerializer.toBytes(b);
    boolean bb = BooleanSerializer.fromBytes(bytes);
    Assert.assertEquals(b, bb);

    b = false;
    bytes = BooleanSerializer.toBytes(b);
    bb = BooleanSerializer.fromBytes(bytes);
    Assert.assertEquals(b, bb);
  }
}
