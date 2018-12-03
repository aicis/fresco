package dk.alexandra.fresco.framework.network.serializers;

import dk.alexandra.fresco.framework.builder.numeric.BigInt;
import dk.alexandra.fresco.framework.builder.numeric.BigIntegerI;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class TestByteSerializer {

  @Test
  public void testDeserializeList() {
    ByteSerializer<BigIntegerI> serializer =
        new BigIntegerWithFixedLengthSerializer(2,
            BigInt::fromBytes);
    List<byte[]> bytes = new ArrayList<>();
    bytes.add(new byte[]{1, 1});
    bytes.add(new byte[]{2, 2});
    bytes.add(new byte[]{3, 3});
    List<BigIntegerI> actual = serializer.deserializeList(bytes);
    List<BigIntegerI> expected = new ArrayList<>();
    expected.add(new BigInt(257));
    expected.add(new BigInt(514));
    expected.add(new BigInt(771));
    Assert.assertEquals(expected, actual);
  }
}
