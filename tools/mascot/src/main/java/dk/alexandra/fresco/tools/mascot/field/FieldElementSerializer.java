package dk.alexandra.fresco.tools.mascot.field;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import dk.alexandra.fresco.framework.network.serializers.SecureSerializer;

public class FieldElementSerializer implements SecureSerializer<FieldElement> {

  BigInteger modulus;
  int modBitLength;

  public FieldElementSerializer(BigInteger modulus, int modBitLength) {
    this.modulus = modulus;
    this.modBitLength = modBitLength;
  }

  /**
   * Deserializes a single field element.
   * 
   * @param data
   * @return field element
   */
  @Override
  public FieldElement deserialize(byte[] data) {
    return new FieldElement(data, modulus, modBitLength);
  }

  /**
   * Serializes a single field element.
   * 
   * @param obj
   * @return field element
   */
  @Override
  public byte[] serialize(FieldElement obj) {
    return obj.toByteArray();
  }

  /**
   * Serializes a list of field elements (all elements must be in same field).
   * 
   * @param elements elements to be serialized
   * @return
   */
  @Override
  public byte[] serialize(List<FieldElement> elements) {
    // nothing to do for empty list
    if (elements.isEmpty()) {
      return new byte[] {};
    }
    // ensure all field elements are in the same field and have same bit length
    for (FieldElement element : elements) {
      if (!element.getModulus()
          .equals(modulus) || element.getBitLength() != modBitLength) {
        throw new IllegalArgumentException("All elements must have same modulus and bit-length");
      }
    }
    byte[] serialized = FieldElementCollectionUtils.pack(elements, false)
        .toByteArray();
    return serialized;
  }

  /**
   * Deserializes byte array into list of field elements.
   * 
   * @param data data to be deserialized
   * @return
   */
  @Override
  public List<FieldElement> deserializeList(byte[] data) {
    if (data.length == 0) {
      return new ArrayList<>();
    }
    int perElementLength = modBitLength / 8;
    if ((data.length) % perElementLength != 0) {
      throw new IllegalArgumentException(
          "Length of byte array must be multiple of per element size");
    }
    return FieldElementCollectionUtils.unpack(data, modulus, modBitLength);
  }

}
