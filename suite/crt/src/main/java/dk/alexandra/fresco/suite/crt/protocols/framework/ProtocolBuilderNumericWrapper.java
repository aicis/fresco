package dk.alexandra.fresco.suite.crt.protocols.framework;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.builder.numeric.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;

// When native protocols are actually evaluated in an evaluation strategy, it's unknown which of the resource pools, each protocol will use. To fix this,
// we create a wrapper around NativeProcol's which gives each of them reference to the relevant resource pool.
public class ProtocolBuilderNumericWrapper<ResourcePoolT extends ResourcePool> extends ProtocolBuilderNumeric {

    private final ResourcePoolT resourcePool;
    private final ProtocolBuilderNumeric builder;

    public ProtocolBuilderNumericWrapper(ProtocolBuilderNumeric builder, BuilderFactoryNumeric factory, ResourcePoolT resourcePool) {
        super(factory, builder.isParallel());
        this.builder = builder;
        this.resourcePool = resourcePool;
    }

    @Override
    public <T> DRes<T> append(NativeProtocol<T, ?> nativeProtocol) {
        return builder.append(new NativeProtocolWrapper<>((NativeProtocol<T, ResourcePoolT>) nativeProtocol, resourcePool));
    }


}
