package dk.alexandra.fresco.framework.network.serializers;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests that the Serializers works as expected.
 *
 */
public class TestSerializers {

  @Test
  public void testBigIntegerSerializer() {
    BigInteger b = new BigInteger("12983762173218321342");
    byte[] bytes = BigIntegerSerializerStream.toBytes(b);
    ByteBuffer buf = ByteBuffer.wrap(bytes);
    BigInteger bb = BigIntegerSerializerStream.toBigInteger(buf);
    Assert.assertEquals(b, bb);
  }

	 @Test
	  public void testBigIntegerSerializerArray() {
	    BigInteger b = new BigInteger("12983762173218321342");
	    BigInteger b2 = new BigInteger("8376321832134446642");
	    BigInteger b3 = new BigInteger("1298372323732183234");
	    BigInteger[] bigInts  = {b, b2, b3};
	    byte[] bytes = BigIntegerSerializerStream.toBytes(bigInts);
	    ByteBuffer buf = ByteBuffer.wrap(bytes);
	    BigInteger[] bb = BigIntegerSerializerStream.toBigIntegers(buf);
	    Assert.assertEquals(bigInts, bb);
	  }
	
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
  public void testByteArrayHelper() {
    BigInteger b = new BigInteger("12983762173218321342");
    byte[] bytes = b.toByteArray();
    byte[] res = ByteArrayHelper.addSize(bytes);
    byte[] bb = ByteArrayHelper.getByteObject(ByteBuffer.wrap(res));
    Assert.assertArrayEquals(bytes, bb);
  }

  @Test
  public void testBooleanSerializer() {
    boolean b = true;
    byte[] bytes = BooleanSerializer.toBytes(b);
    boolean bb = BooleanSerializer.fromBytes(ByteBuffer.wrap(bytes));
    Assert.assertEquals(b, bb);

    b = false;
    bytes = BooleanSerializer.toBytes(b);
    bb = BooleanSerializer.fromBytes(ByteBuffer.wrap(bytes));
    Assert.assertEquals(b, bb);
  }
}
