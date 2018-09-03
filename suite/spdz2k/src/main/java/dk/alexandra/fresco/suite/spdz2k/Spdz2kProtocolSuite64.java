package dk.alexandra.fresco.suite.spdz2k;

import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUInt64;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUIntConverter64;
import dk.alexandra.fresco.suite.spdz2k.datatypes.UInt32;

/**
 * Protocol suite using {@link CompUInt64} as the underlying plain-value type.
 */
public class Spdz2kProtocolSuite64 extends Spdz2kProtocolSuite<UInt32, UInt32, CompUInt64> {

  public Spdz2kProtocolSuite64(boolean useBooleanMode) {
    super(new CompUIntConverter64(), useBooleanMode);
  }

  public Spdz2kProtocolSuite64() {
    this(false);
  }

}
