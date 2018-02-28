package dk.alexandra.fresco.suite.marlin;

import dk.alexandra.fresco.suite.marlin.datatypes.CompUInt128;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUIntConverter128;
import dk.alexandra.fresco.suite.marlin.datatypes.UInt64;

public class Spdz2kProtocolSuite128 extends Spdz2kProtocolSuite<UInt64, UInt64, CompUInt128> {

  public Spdz2kProtocolSuite128() {
    super(new CompUIntConverter128());
  }

}
