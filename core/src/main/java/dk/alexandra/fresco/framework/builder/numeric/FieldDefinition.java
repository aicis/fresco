package dk.alexandra.fresco.framework.builder.numeric;

import java.math.BigInteger;
import java.util.List;

public interface FieldDefinition {

  BigInteger convertRepresentation(FieldElement value);

  BigInteger getModulus();

  FieldElement createElement(int value);

  FieldElement createElement(String value);

  FieldElement createElement(BigInteger value);

  FieldElement deserialize(byte[] bytes);

  List<FieldElement> deserializeList(byte[] bytes);

  byte[] serialize(FieldElement fieldElement);

  byte[] serialize(List<FieldElement> fieldElements);
}

