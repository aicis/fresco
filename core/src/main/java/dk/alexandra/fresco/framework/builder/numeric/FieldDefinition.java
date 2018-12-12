package dk.alexandra.fresco.framework.builder.numeric;

import java.math.BigInteger;
import java.util.List;

public interface FieldDefinition {

  default BigInteger convertRepresentation(FieldElement value) {
    return convertRepresentation(value.convertToBigInteger());
  }

  default BigInteger convertRepresentation(BigInteger value) {
    BigInteger modulus = getModulus();
    BigInteger actual = value.mod(modulus);
    if (actual.compareTo(getModulusHalved()) > 0) {
      return actual.subtract(modulus);
    } else {
      return actual;
    }
  }

  BigInteger getModulus();

  BigInteger getModulusHalved();

  FieldElement createElement(int value);

  FieldElement createElement(String value);

  FieldElement createElement(BigInteger value);

  FieldElement deserialize(byte[] bytes);

  List<FieldElement> deserializeList(byte[] bytes);

  byte[] serialize(FieldElement fieldElement);

  byte[] serialize(List<FieldElement> fieldElements);
}

