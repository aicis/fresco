package dk.alexandra.fresco.framework.builder.numeric.field;

import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import java.math.BigInteger;

public interface FieldDefinition extends ByteSerializer<FieldElement> {

  BigInteger convertToUnsigned(FieldElement value);

  BigInteger convertToSigned(BigInteger signed);

  BigInteger getModulus();

  int getBitLength();

  FieldElement createElement(int value);

  FieldElement createElement(String value);

  FieldElement createElement(BigInteger value);

  StrictBitVector convertToBitVector(FieldElement fieldElement);
}

