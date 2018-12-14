package dk.alexandra.fresco.framework.builder.numeric.field;

import dk.alexandra.fresco.framework.util.StrictBitVector;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

final class FieldUtils {

  private FieldUtils() {
  }

  static int bytesNeededForBits(int bits) {
    return 1 + ((bits - 1) / 8);
  }

  static StrictBitVector convertToBitVector(int byteLength, BigInteger value) {
    return new StrictBitVector(serialize(byteLength, value));
  }

  static byte[] serialize(int byteLength, BigInteger value) {
    return serializeWithOffset(byteLength, value, 0, new byte[byteLength]);
  }

  private static byte[] serializeWithOffset(int byteLength, BigInteger value, int offset,
      byte[] res) {
    byte[] bytes = value.toByteArray();
    int arrayStart = bytes.length > byteLength ? bytes.length - byteLength : 0;
    int resStart = bytes.length > byteLength ? 0 : byteLength - bytes.length;
    int len = Math.min(byteLength, bytes.length);
    System.arraycopy(bytes, arrayStart, res, resStart + offset, len);
    return res;
  }

  static FieldElement deserialize(byte[] bytes, int byteLength,
      Function<BigInteger, FieldElement> creator) {
    return deserializeWithOffset(bytes, 0, byteLength, creator);
  }

  private static FieldElement deserializeWithOffset(byte[] bytes, int offset, int byteLength,
      Function<BigInteger, FieldElement> creator) {
    byte[] actual;
    if (bytes.length == byteLength && offset == 0) {
      actual = bytes;
    } else {
      actual = new byte[byteLength];
      System.arraycopy(bytes, offset, actual, 0, Math.min(byteLength, bytes.length));
    }
    return creator.apply(new BigInteger(1, actual));
  }

  static byte[] serializeList(
      int byteLength,
      List<FieldElement> fieldElements,
      Function<FieldElement, BigInteger> serializer) {
    byte[] bytes = new byte[byteLength * fieldElements.size()];
    for (int i = 0; i < fieldElements.size(); i++) {
      serializeWithOffset(byteLength, serializer.apply(fieldElements.get(i)), i * byteLength,
          bytes);
    }
    return bytes;
  }

  static List<FieldElement> deserializeList(byte[] bytes, int byteLength,
      Function<BigInteger, FieldElement> creator) {
    ArrayList<FieldElement> elements = new ArrayList<>();
    for (int i = 0; i < bytes.length; i += byteLength) {
      elements.add(deserializeWithOffset(bytes, i, byteLength, creator));
    }
    return elements;
  }

  static BigInteger convertRepresentation(BigInteger value, BigInteger modulus,
      BigInteger modulusHalf) {
    if (value.compareTo(modulusHalf) > 0) {
      return value.subtract(modulus);
    } else {
      return value;
    }
  }
}
