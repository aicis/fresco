package dk.alexandra.fresco.framework.network.serializers;

import dk.alexandra.fresco.framework.builder.numeric.BigInt;
import dk.alexandra.fresco.framework.builder.numeric.FieldElement;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests that the Serializers works as expected.
 */
public class TestSerializers {

  @Test
  public void testBigIntegerWithFixedLengthSerializer0() {
    testNumber(getOriginal(new BigInteger("0")));
  }

  @Test
  public void testBigIntegerWithFixedLengthSerializer1298376217321832() {
    testNumber(getOriginal(new BigInteger("1298376217321832")));
  }

  @Test
  public void testBigIntegerWithFixedLengthSerializer12983762173218() {
    testNumber(getOriginal(new BigInteger("12983762173218")));
  }

  @Test
  public void testBigIntegerWithFixedLengthSerializer129837621732() {
    testNumber(getOriginal(new BigInteger("129837621732")));
  }

  @Test
  public void testBigIntegerWithFixedLengthSerializer1298376217() {
    testNumber(getOriginal(new BigInteger("1298376217")));
  }

  private BigInteger getOriginal(BigInteger original) {
    return original;
  }

  private void testNumber(BigInteger original) {
    BigInteger modulus = new BigInteger("1234567890123456789");
    BigIntegerWithFixedLengthSerializer serializer =
        new BigIntegerWithFixedLengthSerializer(20, bytes -> BigInt.fromBytes(bytes, modulus));
    byte[] bytes = serializer.serialize(BigInt.fromConstant(original, modulus));
    FieldElement deserializeLargeNumber = serializer.deserialize(bytes);
    Assert.assertEquals(original, deserializeLargeNumber.asBigInteger());
  }

  @Test
  public void testBigIntegerWithFixedLengthSerializerList() {
    BigInteger original = new BigInteger("1298376217321832");
    BigInteger modulus = new BigInteger("1298376217321832123");
    BigIntegerWithFixedLengthSerializer serializer = new BigIntegerWithFixedLengthSerializer(20,
        bytes -> BigInt.fromBytes(bytes, modulus));
    byte[] bytes = serializer.serialize(
        Arrays.asList(
            new BigInt(original.toString(), modulus),
            new BigInt(0, modulus),
            new BigInt(10, modulus)));

    List<FieldElement> bb = serializer.deserializeList(bytes);
    Assert.assertEquals(original, bb.get(0).asBigInteger());
    Assert.assertEquals(BigInteger.ZERO, bb.get(1).asBigInteger());
    Assert.assertEquals(BigInteger.TEN, bb.get(2).asBigInteger());
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
    byte[] input = new byte[]{0x01, 0x02, 0x03};
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
