package dk.alexandra.fresco.framework.network.serializers;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class TestByteSerializer {

  @Test
  public void testDeserializeList() {
    ByteSerializer<BigInteger> serializer = new BigIntegerWithFixedLengthSerializer(2);
    List<byte[]> bytes = new ArrayList<>();
    bytes.add(new byte[]{1, 1});
    bytes.add(new byte[]{2, 2});
    bytes.add(new byte[]{3, 3});
    List<BigInteger> actual = serializer.deserializeList(bytes);
    List<BigInteger> expected = new ArrayList<>();
    expected.add(BigInteger.valueOf(257));
    expected.add(BigInteger.valueOf(514));
    expected.add(BigInteger.valueOf(771));
    Assert.assertEquals(expected, actual);
  }

}
