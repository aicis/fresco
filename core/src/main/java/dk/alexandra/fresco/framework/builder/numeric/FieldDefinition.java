package dk.alexandra.fresco.framework.builder.numeric;

import java.math.BigInteger;

public interface FieldDefinition {

  BigInteger getModulus();

  BigInteger getModulusHalved();

  FieldElement deserialize(byte[] bytes, int offset, int length);

  FieldElement deserialize(byte[] bytes);

  void serialize(FieldElement fieldElement, byte[] bytes, int offset, int length);

  byte[] serialize(FieldElement fieldElement);

  FieldElement createElement(int value);

  FieldElement createElement(String value);

  FieldElement createElement(BigInteger value);
}

