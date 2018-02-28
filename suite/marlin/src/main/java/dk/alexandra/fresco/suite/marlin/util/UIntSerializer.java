package dk.alexandra.fresco.suite.marlin.util;

import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUInt;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUIntFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UIntSerializer<T extends CompUInt<?, ?, T>> implements ByteSerializer<T> {

  private final CompUIntFactory<T> factory;
  private final int byteLength;

  public UIntSerializer(CompUIntFactory<T> factory) {
    this.factory = factory;
    this.byteLength = factory.getCompositeBitLength() / 8;
  }

  @Override
  public byte[] serialize(T object) {
    return object.toByteArray();
  }

  @Override
  public byte[] serialize(List<T> objects) {
    byte[] all = new byte[byteLength * objects.size()];
    for (int i = 0; i < objects.size(); i++) {
      byte[] serialized = serialize(objects.get(i));
      System.arraycopy(serialized, 0, all, i * byteLength, byteLength);
    }
    return all;
  }

  @Override
  public T deserialize(byte[] bytes) {
    return factory.createFromBytes(bytes);
  }

  @Override
  public List<T> deserializeList(byte[] bytes) {
    if (bytes.length % byteLength != 0) {
      throw new IllegalArgumentException(
          "Total number of bytes must be a multiple of length of single element");
    }
    int numElements = bytes.length / byteLength;
    List<T> elements = new ArrayList<>(numElements);
    for (int i = 0; i < numElements; i++) {
      elements.add(deserialize(Arrays.copyOfRange(bytes, i * byteLength, (i + 1) * byteLength)));
    }
    return elements;
  }

}
