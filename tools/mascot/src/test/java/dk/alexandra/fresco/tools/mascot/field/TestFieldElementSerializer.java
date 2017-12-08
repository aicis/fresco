package dk.alexandra.fresco.tools.mascot.field;

import static org.junit.Assert.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import dk.alexandra.fresco.tools.mascot.MascotTestUtils;

public class TestFieldElementSerializer {

  public static BigInteger modulus = new BigInteger("65521");
  public static int modBitLength = 16;

  @Test
  public void testSerializeList() {
    int[] arr = {1, 42, 777, 111};
    List<FieldElement> elements = MascotTestUtils.generateSingleRow(arr, modulus, modBitLength);
    byte[] actual = FieldElementSerializer.serialize(elements, modulus, modBitLength);
    byte[] expected = {0x02, 0x00, 0x01, 0x00, 0x2A, 0x03, 0x09, 0x00, 0x6F};
    assertArrayEquals(expected, actual);
  }

  @Test
  public void testSerializeEmptyList() {
    byte[] actual = FieldElementSerializer.serialize(new ArrayList<>(), modulus, modBitLength);
    byte[] expected = new byte[] {};
    assertArrayEquals(expected, actual);
  }

  @Test
  public void testDeserializeList() {
    byte[] serialized = {0x02, 0x00, 0x01, 0x00, 0x02, 0x00, 0x03, 0x00, 0x04, 0x00, 0x05};
    int[] expectedArr = {1, 2, 3, 4, 5};
    List<FieldElement> expected =
        MascotTestUtils.generateSingleRow(expectedArr, modulus, modBitLength);
    List<FieldElement> actual =
        FieldElementSerializer.deserializeList(serialized, modulus, modBitLength);
    assertEquals(expected, actual);
  }

  @Test
  public void testDeserializeEmptyArray() {
    byte[] ser = new byte[] {};
    List<FieldElement> des = FieldElementSerializer.deserializeList(ser, modulus, modBitLength);
    assertEquals(true, des.isEmpty());
  }

  @Test
  public void testSerializeDesirializeList() {
    int[] arr = {1, 42, 777, 111};
    List<FieldElement> elements = MascotTestUtils.generateSingleRow(arr, modulus, modBitLength);
    List<FieldElement> actual = FieldElementSerializer.deserializeList(
        FieldElementSerializer.serialize(elements, modulus, modBitLength), modulus, modBitLength);
    assertEquals(elements, actual);
  }

}
