package dk.alexandra.fresco.suite.marlin;

import dk.alexandra.fresco.suite.marlin.datatypes.GenericCompUInt;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUIntConverterGeneric;

class Spdz2kProtocolSuiteGeneric extends
    Spdz2kProtocolSuite<GenericCompUInt, GenericCompUInt, GenericCompUInt> {

  private final int highBitLength;
  private final int lowBitLength;

  Spdz2kProtocolSuiteGeneric(int highBitLength, int lowBitLength) {
    super(new CompUIntConverterGeneric(highBitLength, lowBitLength));
    this.highBitLength = highBitLength;
    this.lowBitLength = lowBitLength;
  }

}
