package dk.alexandra.fresco.suite.marlin.util;

import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.suite.marlin.datatypes.BigUInt;
import dk.alexandra.fresco.suite.marlin.datatypes.BigUIntFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LossyBigUIntSerializer<T extends BigUInt<T>> implements ByteSerializer<T> {

  private final BigUIntFactory<T> factory;
  private final int operationalByteLength;
  private int effectiveByteLength;

  public LossyBigUIntSerializer(BigUIntFactory<T> factory) {
    this.factory = factory;
    this.operationalByteLength = factory.getOperationalBitLength() / 8;
    this.effectiveByteLength = factory.getEffectiveBitLength() / 8;
  }

  @Override
  public void hack(int length) {
    this.effectiveByteLength = length;
  }

  @Override
  public byte[] serialize(T object) {
    return Arrays.copyOfRange(object.toByteArray(), operationalByteLength - effectiveByteLength,
        operationalByteLength);
  }

  @Override
  public byte[] serialize(List<T> objects) {
    byte[] all = new byte[effectiveByteLength * objects.size()];
    for (int i = 0; i < objects.size(); i++) {
      byte[] serialized = serialize(objects.get(i));
      System.arraycopy(serialized, 0, all, i * effectiveByteLength, effectiveByteLength);
    }
    return all;
  }

  @Override
  public T deserialize(byte[] bytes) {
    return factory.createFromBytes(bytes);
  }

  @Override
  public List<T> deserializeList(byte[] bytes) {
    if (bytes.length % effectiveByteLength != 0) {
      throw new IllegalArgumentException(
          "Total number of bytes must be a multiple of length of single element");
    }
    int numElements = bytes.length / effectiveByteLength;
    List<T> elements = new ArrayList<>(numElements);
    for (int i = 0; i < numElements; i++) {
      elements.add(deserialize(
          Arrays.copyOfRange(bytes, i * effectiveByteLength, (i + 1) * effectiveByteLength)));
    }
    return elements;
  }

}
