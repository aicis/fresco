package dk.alexandra.fresco.framework.builder.numeric.field;

import dk.alexandra.fresco.framework.util.StrictBitVector;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

final class FieldUtils {

  private final int modulusLength;
  private final Function<BigInteger, FieldElement> creator;
  private final Function<FieldElement, BigInteger> toBigInteger;

  FieldUtils(int modulusBitLength, Function<BigInteger, FieldElement> creator,
      Function<FieldElement, BigInteger> toBigInteger) {
    this.modulusLength = 1 + ((modulusBitLength - 1) / 8);
    this.creator = creator;
    this.toBigInteger = toBigInteger;
  }

  StrictBitVector convertToBitVector(FieldElement value) {
    return new StrictBitVector(serialize(value));
  }

  byte[] serialize(FieldElement value) {
    return serializeWithOffset(value, 0, new byte[modulusLength]);
  }

  private byte[] serializeWithOffset(FieldElement value, int offset, byte[] res) {
    byte[] bytes = toBigInteger.apply(value).toByteArray();
    int arrayStart = bytes.length > modulusLength ? bytes.length - modulusLength : 0;
    int resStart = bytes.length > modulusLength ? 0 : modulusLength - bytes.length;
    int len = Math.min(modulusLength, bytes.length);
    System.arraycopy(bytes, arrayStart, res, resStart + offset, len);
    return res;
  }

  FieldElement deserialize(byte[] bytes) {
    return deserializeWithOffset(bytes, 0);
  }

  private FieldElement deserializeWithOffset(byte[] bytes, int offset) {
    byte[] actual;
    if (bytes.length == modulusLength) {
      actual = bytes;
    } else {
      actual = new byte[modulusLength];
      System.arraycopy(bytes, offset, actual, 0, modulusLength);
    }
    return creator.apply(new BigInteger(1, actual));
  }

  byte[] serializeList(List<FieldElement> fieldElements) {
    byte[] bytes = new byte[modulusLength * fieldElements.size()];
    for (int i = 0; i < fieldElements.size(); i++) {
      serializeWithOffset(fieldElements.get(i), i * modulusLength, bytes);
    }
    return bytes;
  }

  List<FieldElement> deserializeList(byte[] bytes) {
    ArrayList<FieldElement> elements = new ArrayList<>();
    for (int i = 0; i < bytes.length; i += modulusLength) {
      elements.add(deserializeWithOffset(bytes, i));
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
