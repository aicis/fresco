package dk.alexandra.fresco.framework.network.serializers;

import dk.alexandra.fresco.framework.builder.numeric.BigInt;
import dk.alexandra.fresco.framework.builder.numeric.BigIntegerI;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class TestByteSerializer {

  @Test
  public void testDeserializeList() {
    BigInteger modulus = BigInteger.valueOf(1234);
    ByteSerializer<BigIntegerI> serializer =
        new BigIntegerWithFixedLengthSerializer(2,
            bytes -> BigInt.fromBytes(bytes, modulus));
    List<byte[]> bytes = new ArrayList<>();
    bytes.add(new byte[]{1, 1});
    bytes.add(new byte[]{2, 2});
    bytes.add(new byte[]{3, 3});
    List<BigIntegerI> actual = serializer.deserializeList(bytes);
    List<BigIntegerI> expected = new ArrayList<>();
    expected.add(new BigInt(257, modulus));
    expected.add(new BigInt(514, modulus));
    expected.add(new BigInt(771, modulus));
    Assert.assertEquals(expected, actual);
  }
}
