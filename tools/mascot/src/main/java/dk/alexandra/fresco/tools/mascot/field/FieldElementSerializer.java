package dk.alexandra.fresco.tools.mascot.field;

import dk.alexandra.fresco.framework.builder.numeric.Modulus;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;


public class FieldElementSerializer implements ByteSerializer<MascotFieldElement> {

  private final Modulus modulus;
  private final int modBitLength;
  private final FieldElementUtils fieldElementUtils;

  /**
   * Creates new {@link FieldElementSerializer}.
   *
   * @param modulus modulus of field elements
   */
  public FieldElementSerializer(Modulus modulus) {
    this.modulus = modulus;
    this.modBitLength = modulus.getBigInteger().bitLength();
    this.fieldElementUtils = new FieldElementUtils(modulus);
  }

  /**
   * Deserializes a single field element.
   *
   * @param data serialized element
   * @return deserialized field element
   */
  @Override
  public MascotFieldElement deserialize(byte[] data) {
    return new MascotFieldElement(data, modulus);
  }

  /**
   * Serializes a single field element.
   *
   * @param obj field element to serialize
   * @return serialized field element
   */
  @Override
  public byte[] serialize(MascotFieldElement obj) {
    if (!obj.getModulus().equals(modulus)) {
      throw new IllegalArgumentException("All elements must have same modulus");
    }
    return obj.toByteArray();
  }

  /**
   * Serializes a list of field elements (all elements must be in same field).
   *
   * @param elements elements to be serialized
   * @return serialized field elements
   */
  @Override
  public byte[] serialize(List<MascotFieldElement> elements) {
    // nothing to do for empty list
    if (elements.isEmpty()) {
      return new byte[]{};
    }
    // ensure all field elements are in the same field and have same bit length
    for (MascotFieldElement element : elements) {
      if (!element.getModulus().equals(modulus)) {
        throw new IllegalArgumentException("All elements must have same modulus");
      }
    }
    return fieldElementUtils.pack(elements, false).toByteArray();
  }

  /**
   * Deserializes byte array into list of field elements.
   *
   * @param data data to be deserialized
   * @return deserialized field elements
   */
  @Override
  public List<MascotFieldElement> deserializeList(byte[] data) {
    if (data.length == 0) {
      return new ArrayList<>();
    }
    int perElementLength = modBitLength / 8;
    if ((data.length) % perElementLength != 0) {
      throw new IllegalArgumentException(
          "Length of byte array must be multiple of per element size");
    }
    return fieldElementUtils.unpack(data);
  }

}
