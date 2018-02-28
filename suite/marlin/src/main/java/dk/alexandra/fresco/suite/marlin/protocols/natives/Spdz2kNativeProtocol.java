package dk.alexandra.fresco.suite.marlin.protocols.natives;

import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUInt;
import dk.alexandra.fresco.suite.marlin.resource.Spdz2kResourcePool;

public abstract class Spdz2kNativeProtocol<
    OutputT,
    PlainT extends CompUInt<?, ?, PlainT>> implements
    NativeProtocol<OutputT, Spdz2kResourcePool<PlainT>> {

}
