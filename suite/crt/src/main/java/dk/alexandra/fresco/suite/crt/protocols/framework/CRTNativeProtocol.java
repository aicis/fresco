package dk.alexandra.fresco.suite.crt.protocols.framework;

import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.builder.numeric.NumericResourcePool;
import dk.alexandra.fresco.suite.crt.datatypes.resource.CRTResourcePool;

public abstract class CRTNativeProtocol<OutputT, ResourcePoolA extends NumericResourcePool, ResourcePoolB extends NumericResourcePool> implements
    NativeProtocol<OutputT, CRTResourcePool<ResourcePoolA, ResourcePoolB>> {

}
