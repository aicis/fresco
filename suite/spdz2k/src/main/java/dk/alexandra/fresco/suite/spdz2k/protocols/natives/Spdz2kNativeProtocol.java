package dk.alexandra.fresco.suite.spdz2k.protocols.natives;

import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUInt;
import dk.alexandra.fresco.suite.spdz2k.resource.Spdz2kResourcePool;

/**
 * Generic native Spdz2k protocol.
 */
public abstract class Spdz2kNativeProtocol<
    OutputT,
    PlainT extends CompUInt<?, ?, PlainT>> implements
    NativeProtocol<OutputT, Spdz2kResourcePool<PlainT>> {

}
