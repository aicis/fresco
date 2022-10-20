package dk.alexandra.fresco.suite.crt.protocols.framework;

import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;

// When native protocols are actually evaluated in an evaluation strategy, it's unknown which of the resource pools, each protocol will use. To fix this,
// we create this wrapper which gives a reference to the relevant resource pool for each single protocol in the {@link ProtocolBuilderNumericWrapper}.
public class NativeProtocolWrapper<OutputT, ResourcePoolT extends ResourcePool> implements NativeProtocol<OutputT, ResourcePoolT> {

    private final NativeProtocol<OutputT, ResourcePoolT> protocol;
    private final ResourcePoolT resourcePool;

    public NativeProtocolWrapper(NativeProtocol<OutputT, ResourcePoolT> protocol, ResourcePoolT resourcePool) {
        this.protocol = protocol;
        this.resourcePool = resourcePool;
    }

    public ResourcePoolT getResourcePool() {
        return resourcePool;
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
