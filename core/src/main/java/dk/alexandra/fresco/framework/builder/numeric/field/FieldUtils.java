package dk.alexandra.fresco.framework.builder.numeric.field;

import dk.alexandra.fresco.framework.util.StrictBitVector;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

final class FieldUtils {

  private FieldUtils() {
  }

  static void ensureDivisible(BigInteger modulus) {
    if (!modulus.remainder(BigInteger.valueOf(8)).equals(BigInteger.ZERO)) {
      throw new IllegalArgumentException("modulus must be divisible by 8");
    }
  }

  static StrictBitVector convertToBitVector(BigInteger modulus, byte[] bytes) {
    int byteLength = modulus.bitLength() / 8;
    byte[] res = new byte[byteLength];
    int arrayStart = bytes.length > byteLength ? bytes.length - byteLength : 0;
    int resStart = bytes.length > byteLength ? 0 : byteLength - bytes.length;
    int len = Math.min(byteLength, bytes.length);
    System.arraycopy(bytes, arrayStart, res, resStart, len);
    return new StrictBitVector(res);
  }

  static byte[] serialize(int modulusLength, List<FieldElement> fieldElements,
      Function<FieldElement, byte[]> serializer) {
    byte[] bytes = new byte[modulusLength * fieldElements.size()];
    for (int i = 0; i < fieldElements.size(); i++) {
      byte[] input = serializer.apply(fieldElements.get(i));
      int destPos = modulusLength - input.length + i * modulusLength;
      System.arraycopy(input, 0, bytes, destPos, input.length);
    }
    return bytes;
  }

  static List<FieldElement> deserializeList(byte[] bytes, int modulusLength,
      Function<byte[], FieldElement> creator) {
    ArrayList<FieldElement> elements = new ArrayList<>();
    for (int i = 0; i < bytes.length; i += modulusLength) {
      byte[] copy = new byte[modulusLength];
      System.arraycopy(bytes, i, copy, 0, modulusLength);
      elements.add(creator.apply(copy));
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
