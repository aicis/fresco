package dk.alexandra.fresco.suite.crt.protocols.framework;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.builder.numeric.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.lib.helper.SingleProtocolProducer;

public class ProtocolBuilderNumericWrapper<ResourcePoolT extends ResourcePool> extends ProtocolBuilderNumeric {

    private final ResourcePoolT resourcePool;
    private final ProtocolBuilderNumeric builder;

    public ProtocolBuilderNumericWrapper(ProtocolBuilderNumeric builder, BuilderFactoryNumeric factory, ResourcePoolT resourcePool) {
        super(factory, true);
        this.builder = builder;
        this.resourcePool = resourcePool;
    }

    @Override
    public <T> DRes<T> append(NativeProtocol<T, ?> nativeProtocol) {
        return builder.append(new NativeProtocolWrapper<>((NativeProtocol<T, ResourcePoolT>) nativeProtocol, resourcePool));
    }


}
