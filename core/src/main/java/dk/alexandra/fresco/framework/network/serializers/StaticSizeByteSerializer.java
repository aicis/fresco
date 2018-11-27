package dk.alexandra.fresco.framework.network.serializers;

public interface StaticSizeByteSerializer<T> extends ByteSerializer<T> {

  /**
   * @return Returns the amount of bytes in a serialization of an element
   */
  int getElementSize();
}
