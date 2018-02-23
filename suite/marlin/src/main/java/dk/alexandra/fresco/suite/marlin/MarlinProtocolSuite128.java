package dk.alexandra.fresco.suite.marlin;

import dk.alexandra.fresco.suite.marlin.datatypes.CompUInt128;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUIntConverter128;
import dk.alexandra.fresco.suite.marlin.datatypes.UInt64;

public class MarlinProtocolSuite128 extends MarlinProtocolSuite<UInt64, UInt64, CompUInt128> {

  public MarlinProtocolSuite128() {
    super(new CompUIntConverter128());
  }

}
