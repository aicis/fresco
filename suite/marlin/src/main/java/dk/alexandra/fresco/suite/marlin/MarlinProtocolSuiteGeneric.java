package dk.alexandra.fresco.suite.marlin;

import dk.alexandra.fresco.suite.marlin.datatypes.GenericCompUInt;
import dk.alexandra.fresco.suite.marlin.datatypes.GenericComputUIntConverter;

class MarlinProtocolSuiteGeneric extends
    MarlinProtocolSuite<GenericCompUInt, GenericCompUInt, GenericCompUInt> {

  private final int highBitLength;
  private final int lowBitLength;

  MarlinProtocolSuiteGeneric(int highBitLength, int lowBitLength) {
    super(new GenericComputUIntConverter(highBitLength, lowBitLength));
    this.highBitLength = highBitLength;
    this.lowBitLength = lowBitLength;
  }

}
