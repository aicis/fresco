package dk.alexandra.fresco.suite.crt.protocols.framework;

import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.builder.numeric.NumericResourcePool;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.evaluator.BatchEvaluationStrategy;
import dk.alexandra.fresco.framework.sce.evaluator.NetworkBatchDecorator;
import dk.alexandra.fresco.suite.crt.datatypes.resource.CRTResourcePool;

import java.util.Iterator;

public class CRTBatchedStrategy <ResourcePoolA extends NumericResourcePool, ResourcePoolB extends NumericResourcePool> implements
        BatchEvaluationStrategy<CRTResourcePool<ResourcePoolA, ResourcePoolB>> {

    @Override
    public void processBatch(ProtocolCollection<CRTResourcePool<ResourcePoolA, ResourcePoolB>> nativeProtocols, CRTResourcePool<ResourcePoolA, ResourcePoolB> resourcePool, NetworkBatchDecorator network) {
        int round = 0;
        while (nativeProtocols.size() > 0) {
            evaluateCurrentRound(nativeProtocols, network, resourcePool, round);
            network.flush();
            round++;
        }
    }

    private void evaluateCurrentRound(
            ProtocolCollection<CRTResourcePool<ResourcePoolA, ResourcePoolB>> protocols, Network sceNetwork,
            CRTResourcePool<ResourcePoolA, ResourcePoolB> rp, int round) {
        Iterator<NativeProtocol<?, CRTResourcePool<ResourcePoolA, ResourcePoolB>>> iterator = protocols.iterator();
        while (iterator.hasNext()) {
            NativeProtocol<?, CRTResourcePool<ResourcePoolA, ResourcePoolB>> protocol = iterator.next();
            NativeProtocol.EvaluationStatus status = protocol.evaluate(round, rp, sceNetwork);
            if (status.equals(NativeProtocol.EvaluationStatus.IS_DONE)) {
                iterator.remove();
            }
        }
    }
}

