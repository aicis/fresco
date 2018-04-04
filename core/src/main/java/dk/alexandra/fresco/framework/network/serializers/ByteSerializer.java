package dk.alexandra.fresco.framework.network.serializers;

import java.util.ArrayList;
import java.util.List;

/**
 * A generic serializer, allows serialization and deserialization of elements with the
 * corresponding type, T.
 *
 * @param <T> the type to serialize.
 */
public interface ByteSerializer<T> {

  /**
   * Converts an element of type T to an array of bytes
   *
   * @param object the object to convert
   * @return the resulting byte array
   */
  byte[] serialize(T object);

  /**
   * Converts a list of elements of type T to an array of bytes
   *
   * @param objects the objects to convert
   * @return the resulting byte array
   */
  byte[] serialize(List<T> objects);


  /**
   * Reads an element of type T from a byte array.
   *
   * @param bytes the data
   * @return the converted element.
   */
  T deserialize(byte[] bytes);


  /**
   * Reads a list of elements of type T from a byte array.
   *
   * @param bytes the data
   * @return the converted elements.
   */
  List<T> deserializeList(byte[] bytes);

  /**
   * Call {@link #deserialize(byte[])} for each byte array in input.
   */
  default List<T> deserializeList(List<byte[]> bytes) {
    List<T> elements = new ArrayList<>(bytes.size());
    for (byte[] subBytes : bytes) {
      elements.add(deserialize(subBytes));
    }
    return elements;
  }

}
