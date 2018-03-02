package dk.alexandra.fresco.suite.spdz2k.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUIntFactory;
import dk.alexandra.fresco.suite.spdz2k.datatypes.GenericCompUInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.GenericCompUIntFactory;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.junit.Test;

public class TestUIntSerializer {

  private final CompUIntFactory<GenericCompUInt> factory = new GenericCompUIntFactory(64, 64);
  private final ByteSerializer<GenericCompUInt> serializer = new UIntSerializer<>(factory);

  @Test
  public void testSerialize() {
    byte[] rawBytes = new byte[16];
    rawBytes[0] = 0x01;
    rawBytes[1] = 0x02;
    rawBytes[2] = 0x03;
    rawBytes[15] = 0x16;
    GenericCompUInt element = factory.createFromBytes(rawBytes);
    assertArrayEquals(rawBytes, serializer.serialize(element));
  }

  @Test
  public void testSerializeList() {
    Random random = new Random(42);
    byte[] rawBytes = new byte[32];
    random.nextBytes(rawBytes);
    List<GenericCompUInt> elements = Arrays.asList(
        factory.createFromBytes(Arrays.copyOfRange(rawBytes, 0, 16)),
        factory.createFromBytes(Arrays.copyOfRange(rawBytes, 16, 32))
    );
    byte[] actual = serializer.serialize(elements);
    assertArrayEquals(rawBytes, actual);
  }

  @Test
  public void testDeserialize() {
    Random random = new Random(42);
    byte[] bytes = new byte[16];
    random.nextBytes(bytes);
    GenericCompUInt uint = serializer.deserialize(bytes);
    assertArrayEquals(bytes, uint.toByteArray());
  }

  @Test
  public void testDeserializeList() {
    Random random = new Random(42);
    byte[] rawBytes = new byte[32];
    random.nextBytes(rawBytes);
    List<GenericCompUInt> expected = Arrays.asList(
        factory.createFromBytes(Arrays.copyOfRange(rawBytes, 0, 16)),
        factory.createFromBytes(Arrays.copyOfRange(rawBytes, 16, 32))
    );
    List<GenericCompUInt> actual = serializer.deserializeList(rawBytes);
    assertEquals(expected.size(), actual.size());
    for (int i = 0; i < actual.size(); i++) {
      assertArrayEquals(expected.get(i).toByteArray(), actual.get(i).toByteArray());
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void testDeserializeListWrongLength() {
    Random random = new Random(42);
    byte[] rawBytes = new byte[33];
    serializer.deserializeList(rawBytes);
  }

}
