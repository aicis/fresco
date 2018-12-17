package dk.alexandra.fresco.framework.builder.numeric.field;

import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import java.math.BigInteger;

public interface FieldDefinition extends ByteSerializer<FieldElement> {

  FieldElement createElement(int value);

  FieldElement createElement(String value);

  FieldElement createElement(BigInteger value);

  BigInteger getModulus();

  int getBitLength();

  StrictBitVector convertToBitVector(FieldElement fieldElement);

  BigInteger convertToUnsigned(FieldElement value);

  BigInteger convertToSigned(BigInteger signed);
}

