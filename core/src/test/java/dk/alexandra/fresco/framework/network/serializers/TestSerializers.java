package dk.alexandra.fresco.framework.network.serializers;

import java.math.BigInteger;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests that the Serializers works as expected.
 */
public class TestSerializers {

  @Test
  public void testBigIntegerWithFixedLengthSerializer() {
    BigInteger b = new BigInteger("1298376217321832");
    BigIntegerWithFixedLengthSerializer serializer = new BigIntegerWithFixedLengthSerializer(20);
    byte[] bytes = serializer.toBytes(b);
    BigInteger bb = serializer.toBigInteger(bytes);
    Assert.assertEquals(b, bb);

    b = BigInteger.ZERO;
    bytes = serializer.toBytes(b);
    bb = serializer.toBigInteger(bytes);
    Assert.assertEquals(b, bb);
  }

  @Test
  public void constructor() throws Exception {
    new BooleanSerializer();
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
