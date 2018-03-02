package dk.alexandra.fresco.framework.network.serializers;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import dk.alexandra.fresco.framework.util.StrictBitVector;

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

  @Test
  public void testBitVectorSerializer() {
    byte[] input = new byte[] { 0x01, 0x02, 0x03 };
    StrictBitVector vector = new StrictBitVector(input);
    StrictBitVectorSerializer serializer = new StrictBitVectorSerializer();
    // Test serialization
    byte[] output = serializer.serialize(vector);
    Assert.assertArrayEquals(input, output);
    // Test deserialization
    StrictBitVector deserializedVector = serializer.deserialize(output);
    Assert.assertEquals(vector, deserializedVector);
  }

  /**** NEGATIVE TESTS. ****/

  @Test(expected = UnsupportedOperationException.class)
  public void testBitVectorSerializerSerializeList() {
    StrictBitVectorSerializer serializer = new StrictBitVectorSerializer();
    serializer.serialize(new ArrayList<StrictBitVector>());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testBitVectorSerializerDeserializeList() {
    StrictBitVectorSerializer serializer = new StrictBitVectorSerializer();
    serializer.deserializeList(new byte[10]);
  }
}
