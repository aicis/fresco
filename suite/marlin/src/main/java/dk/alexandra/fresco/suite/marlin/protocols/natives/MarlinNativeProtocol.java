package dk.alexandra.fresco.suite.marlin.protocols.natives;

import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUInt;
import dk.alexandra.fresco.suite.marlin.datatypes.UInt;
import dk.alexandra.fresco.suite.marlin.resource.MarlinResourcePool;

abstract class MarlinNativeProtocol<OutputT, H extends UInt<H>, L extends UInt<L>, T extends CompUInt<H, L, T>> implements
    NativeProtocol<OutputT, MarlinResourcePool<H, L, T>> {

}
