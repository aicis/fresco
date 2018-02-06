package dk.alexandra.fresco.framework.network.serializers;

public class BooleanSerializer {

  /**
   * Serializes a boolean into a byte[] of size 1.
   *
   * @param b the boolean to serialize.
   * @return The byte array representation of the boolean.
   */
  public static byte toBytes(boolean b) {
    if (b) {
      return 0x01;
    } else {
      return 0x00;
    }
  }

  /**
   * De-serializes a boolean from a single byte.
   *
   * @param b The byte containing the serialized boolean.
   * @return The de-serialized boolean.
   */
  public static boolean fromBytes(byte b) {
    return b == 0x01;
  }
}
