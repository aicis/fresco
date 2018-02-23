package dk.alexandra.fresco.suite.marlin.protocols.natives;

import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUInt;
import dk.alexandra.fresco.suite.marlin.resource.MarlinResourcePool;

public abstract class MarlinNativeProtocol<
    OutputT,
    T extends CompUInt<?, ?, T>> implements NativeProtocol<OutputT, MarlinResourcePool<T>> {

}
