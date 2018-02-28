package dk.alexandra.fresco.suite.marlin;

import dk.alexandra.fresco.suite.marlin.datatypes.CompUInt96;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUIntConverter96;
import dk.alexandra.fresco.suite.marlin.datatypes.UInt32;
import dk.alexandra.fresco.suite.marlin.datatypes.UInt64;

class Spdz2kProtocolSuite96 extends Spdz2kProtocolSuite<UInt64, UInt32, CompUInt96> {

  Spdz2kProtocolSuite96() {
    super(new CompUIntConverter96());
  }

}
