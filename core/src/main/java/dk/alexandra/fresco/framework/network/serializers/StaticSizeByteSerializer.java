package dk.alexandra.fresco.framework.network.serializers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class StaticSizeByteSerializer<T> implements ByteSerializer<T> {

  /**
   * @return Returns the amount of bytes in a serialization of an element
   */
  abstract public int getElementSize();

  @Override
  public byte[] serialize(List<T> objects) {
    byte[] res = new byte[getElementSize() * objects.size()];
    int currentPos = 0;
    for (T currentObj : objects) {
      byte[] currentSerialized = serialize(currentObj);
      System.arraycopy(currentSerialized, 0, res, currentPos, currentSerialized.length);
      currentPos += currentSerialized.length;
    }
    return res;
  }

  @Override
  public List<T> deserializeList(byte[] bytes) {
    int amount = bytes.length / getElementSize();
    List<T> res = new ArrayList<>(amount);
    for (int i = 0; i < amount; i++) {
      Object currentObj = deserialize(
          Arrays.copyOfRange(bytes, i * getElementSize(), (i + 1) * getElementSize()));
      res.add((T) currentObj);
    }
    return res;
  }
}
