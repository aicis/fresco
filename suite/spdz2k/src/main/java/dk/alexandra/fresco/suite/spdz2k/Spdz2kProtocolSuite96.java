package dk.alexandra.fresco.suite.spdz2k;

import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUInt96;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUIntConverter96;
import dk.alexandra.fresco.suite.spdz2k.datatypes.UInt32;
import dk.alexandra.fresco.suite.spdz2k.datatypes.UInt64;

/**
 * Protocol suite using {@link CompUInt96} as the underlying plain-value type.
 */
public class Spdz2kProtocolSuite96 extends Spdz2kProtocolSuite<UInt64, UInt32, CompUInt96> {

  Spdz2kProtocolSuite96() {
    super(new CompUIntConverter96());
  }

}
