package dk.alexandra.fresco.framework.network.serializers;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
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
    byte[] bytes = serializer.serialize(b);
    BigInteger bb = serializer.deserialize(bytes);
    Assert.assertEquals(b, bb);

    b = BigInteger.ZERO;
    bytes = serializer.serialize(b);
    bb = serializer.deserialize(bytes);
    Assert.assertEquals(b, bb);
  }

  @Test
  public void testBigIntegerWithFixedLengthSerializerList() {
    BigInteger b = new BigInteger("1298376217321832");
    BigIntegerWithFixedLengthSerializer serializer = new BigIntegerWithFixedLengthSerializer(20);
    byte[] bytes = serializer.serialize(Arrays.asList(b, BigInteger.ZERO, BigInteger.TEN));
    List<BigInteger> bb = serializer.deserializeList(bytes);
    Assert.assertEquals(b, bb.get(0));
    Assert.assertEquals(BigInteger.ZERO, bb.get(1));
    Assert.assertEquals(BigInteger.TEN, bb.get(2));
  }

  @Test
  public void constructor() {
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
