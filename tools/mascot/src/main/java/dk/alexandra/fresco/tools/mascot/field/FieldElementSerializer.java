package dk.alexandra.fresco.tools.mascot.field;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FieldElementSerializer {

  /**
   * Serializes a list of field elements (all elements must be in same field). <br>
   * Prefixes list of raw bytes of single element byte length.
   * 
   * @param elements elements to be serialized
   * @param modulus modulus defining the field
   * @param modBitLength bit-length of elements
   * @return
   */
  public static byte[] serialize(List<FieldElement> elements, BigInteger modulus,
      int modBitLength) {
    // nothing to do for empty list
    if (elements.isEmpty()) {
      return new byte[] {};
    }
    // compute byte length and check that it is within range
    int byteLen = modBitLength / 8;
    if (byteLen >= Byte.MAX_VALUE) {
      throw new IllegalArgumentException("Single element byte length must fit in single byte");
    }
    // ensure all field elements are in the same field and have same bit length
    for (FieldElement element : elements) {
      if (!element.getModulus()
          .equals(modulus) || element.getBitLength() != modBitLength) {
        throw new IllegalArgumentException("All elements must have same modulus and bit-length");
      }
    }
    // the per-element byte length will be our header
    byte header = (byte) byteLen;
    // need space for all elements, plus header
    int totalBufferLen = elements.size() * byteLen + 1;
    byte[] buffer = new byte[totalBufferLen];
    byte[] feBytes = FieldElementCollectionUtils.pack(elements, false)
        .toByteArray();
    buffer[0] = header;
    System.arraycopy(feBytes, 0, buffer, 1, feBytes.length);
    return buffer;
  }

  /**
   * Deserializes byte array into list of field elements. <br>
   * Assumes that first byte indicates per element byte length.
   * 
   * @param data data to be deserialized
   * @param modulus modulus defining the field
   * @param modBitLength bit-length of elements
   * @return
   */
  public static List<FieldElement> deserializeList(byte[] data, BigInteger modulus,
      int modBitLength) {
    if (data.length == 0) {
      return new ArrayList<>();
    }
    int perElementLength = (int) data[0];
    if ((data.length - 1) % perElementLength != 0) {
      throw new IllegalArgumentException(
          "Length of byte array must be multiple of per element size");
    }
    byte[] noHeader = Arrays.copyOfRange(data, 1, data.length);
    return FieldElementCollectionUtils.unpack(noHeader, modulus, modBitLength);
  }

}
