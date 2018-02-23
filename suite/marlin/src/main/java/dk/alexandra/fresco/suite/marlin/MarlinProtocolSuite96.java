package dk.alexandra.fresco.suite.marlin;

import dk.alexandra.fresco.suite.marlin.datatypes.CompUInt96;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUIntConverter96;
import dk.alexandra.fresco.suite.marlin.datatypes.UInt32;
import dk.alexandra.fresco.suite.marlin.datatypes.UInt64;

class MarlinProtocolSuite96 extends MarlinProtocolSuite<UInt64, UInt32, CompUInt96> {

  MarlinProtocolSuite96() {
    super(new CompUIntConverter96());
  }

}
