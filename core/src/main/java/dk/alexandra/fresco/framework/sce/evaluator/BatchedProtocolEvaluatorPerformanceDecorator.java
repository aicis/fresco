package dk.alexandra.fresco.framework.sce.evaluator;

import dk.alexandra.fresco.framework.PerformanceLogger;
import dk.alexandra.fresco.framework.PerformanceLogger.Flag;
import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder;
import dk.alexandra.fresco.framework.network.SCENetwork;
import dk.alexandra.fresco.framework.network.SCENetworkSupplier;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import java.io.IOException;

public class BatchedProtocolEvaluatorPerformanceDecorator<ResourcePoolT extends ResourcePool, Builder extends ProtocolBuilder>
    extends BatchedSequentialEvaluator<ResourcePoolT, Builder> {

  private PerformanceLogger pl;

  public BatchedProtocolEvaluatorPerformanceDecorator(PerformanceLogger pl) {
    super();
    this.pl = pl;
  }

  @Override
  public <sceNetwork extends SCENetwork & SCENetworkSupplier> void processBatch(
      ProtocolCollection<ResourcePoolT> protocols, ResourcePoolT resourcePool, sceNetwork network)
          throws IOException {
    if (pl.flags.contains(Flag.LOG_NATIVE_BATCH)) {
      pl.nativeBatch(protocols.size());
    }
    super.processBatch(protocols, resourcePool, network);
  }
}
