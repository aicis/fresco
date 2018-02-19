package dk.alexandra.fresco.suite.marlin.protocols.natives;

import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.suite.marlin.resource.MarlinResourcePool;
import dk.alexandra.fresco.suite.marlin.datatypes.CompositeUInt;

abstract class MarlinNativeProtocol<OutputT, T extends CompositeUInt<T>> implements
    NativeProtocol<OutputT, MarlinResourcePool<T>> {
}
