package dk.alexandra.fresco.suite.spdz2k;

import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUIntConverterGeneric;
import dk.alexandra.fresco.suite.spdz2k.datatypes.GenericCompUInt;

/**
 * Protocol suite using {@link GenericCompUInt} as the underlying plain-value type.
 */
public class Spdz2kProtocolSuiteGeneric extends
    Spdz2kProtocolSuite<GenericCompUInt, GenericCompUInt, GenericCompUInt> {

  Spdz2kProtocolSuiteGeneric(int highBitLength, int lowBitLength) {
    super(new CompUIntConverterGeneric(highBitLength, lowBitLength));
  }

}
