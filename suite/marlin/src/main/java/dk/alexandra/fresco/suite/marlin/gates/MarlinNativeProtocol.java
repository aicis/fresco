package dk.alexandra.fresco.suite.marlin.gates;

import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.suite.marlin.resource.MarlinResourcePool;
import dk.alexandra.fresco.suite.marlin.datatypes.BigUInt;

public abstract class MarlinNativeProtocol<OutputT, T extends BigUInt<T>> implements
    NativeProtocol<OutputT, MarlinResourcePool<T>> {

}
