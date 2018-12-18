package dk.alexandra.fresco.suite.spdz2k.datatypes;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import dk.alexandra.fresco.framework.builder.numeric.field.FieldElement;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUInt128;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUInt128Factory;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUIntFactory;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.junit.Test;

public class TestCompUIntFactorySerialization {

  private final CompUIntFactory<CompUInt128> factory = new CompUInt128Factory();

  @Test
  public void testSerialize() {
    byte[] rawBytes = new byte[16];
    rawBytes[0] = 0x01;
    rawBytes[1] = 0x02;
    rawBytes[2] = 0x03;
    rawBytes[15] = 0x16;
    CompUInt128 element = factory.deserialize(rawBytes);
    assertArrayEquals(rawBytes, factory.serialize(element));
  }

  @Test
  public void testSerializeList() {
    Random random = new Random(42);
    byte[] rawBytes = new byte[32];
    random.nextBytes(rawBytes);
    List<FieldElement> elements = Arrays.asList(
        factory.deserialize(Arrays.copyOfRange(rawBytes, 0, 16)),
        factory.deserialize(Arrays.copyOfRange(rawBytes, 16, 32))
    );
    byte[] actual = factory.serialize(elements);
    assertArrayEquals(rawBytes, actual);
  }

  @Test
  public void testDeserialize() {
    Random random = new Random(42);
    byte[] bytes = new byte[16];
    random.nextBytes(bytes);
    CompUInt128 uint = factory.deserialize(bytes);
    assertArrayEquals(bytes, uint.toByteArray());
  }

  @Test
  public void testDeserializeList() {
    Random random = new Random(42);
    byte[] rawBytes = new byte[32];
    random.nextBytes(rawBytes);
    List<CompUInt128> expected = Arrays.asList(
        factory.deserialize(Arrays.copyOfRange(rawBytes, 0, 16)),
        factory.deserialize(Arrays.copyOfRange(rawBytes, 16, 32))
    );
    List<FieldElement> actual = factory.deserializeList(rawBytes);
    assertEquals(expected.size(), actual.size());
    for (int i = 0; i < actual.size(); i++) {
      CompUInt128 cast = (CompUInt128) actual.get(i);
      assertArrayEquals(expected.get(i).toByteArray(), cast.toByteArray());
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void testDeserializeListWrongLength() {
    Random random = new Random(42);
    byte[] rawBytes = new byte[33];
    factory.deserializeList(rawBytes);
  }

}
