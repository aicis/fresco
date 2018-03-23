package dk.alexandra.fresco.suite.spdz2k.util;

import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUIntFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Serializer for {@link CompUInt} instances.
 */
public class UIntSerializer<PlainT extends CompUInt<?, ?, PlainT>> implements
    ByteSerializer<PlainT> {

  private final CompUIntFactory<PlainT> factory;
  private final int byteLength;

  /**
   * Creates new {@link UIntSerializer}.
   *
   * @param factory factory for creating {@link PlainT} instances.
   */
  public UIntSerializer(CompUIntFactory<PlainT> factory) {
    this.factory = factory;
    this.byteLength = factory.getCompositeBitLength() / 8;
  }

  @Override
  public byte[] serialize(PlainT object) {
    return object.toByteArray();
  }

  @Override
  public byte[] serialize(List<PlainT> objects) {
    byte[] all = new byte[byteLength * objects.size()];
    for (int i = 0; i < objects.size(); i++) {
      byte[] serialized = serialize(objects.get(i));
      System.arraycopy(serialized, 0, all, i * byteLength, byteLength);
    }
    return all;
  }

  @Override
  public PlainT deserialize(byte[] bytes) {
    return factory.createFromBytes(bytes);
  }

  @Override
  public List<PlainT> deserializeList(byte[] bytes) {
    if (bytes.length % byteLength != 0) {
      throw new IllegalArgumentException(
          "Total number of bytes must be a multiple of length of single element");
    }
    int numElements = bytes.length / byteLength;
    List<PlainT> elements = new ArrayList<>(numElements);
    for (int i = 0; i < numElements; i++) {
      elements.add(deserialize(Arrays.copyOfRange(bytes, i * byteLength, (i + 1) * byteLength)));
    }
    return elements;
  }

}
