package dk.alexandra.fresco.suite.spdz2k;

import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUInt128;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUIntConverter128;
import dk.alexandra.fresco.suite.spdz2k.datatypes.UInt64;

public class Spdz2kProtocolSuite128 extends Spdz2kProtocolSuite<UInt64, UInt64, CompUInt128> {

  public Spdz2kProtocolSuite128() {
    super(new CompUIntConverter128());
  }

}
