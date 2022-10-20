package dk.alexandra.fresco.suite.crt.protocols.framework;

import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;

public class NativeProtocolWrapper<OutputT, ResourcePoolT extends ResourcePool> implements NativeProtocol<OutputT, ResourcePoolT> {

    private final NativeProtocol<OutputT, ResourcePoolT> protocol;

    public ResourcePoolT getResourcePool() {
        return resourcePool;
    }

    private final ResourcePoolT resourcePool;

    public NativeProtocolWrapper(NativeProtocol<OutputT, ResourcePoolT> protocol, ResourcePoolT resourcePool) {
        this.protocol = protocol;
        this.resourcePool = resourcePool;
    }

    @Override
    public OutputT out() {
        return protocol.out();
    }

    @Override
    public EvaluationStatus evaluate(int round, ResourcePoolT resourcePool, Network network) {
        return protocol.evaluate(round, this.resourcePool, network);
    }
}
