package dk.alexandra.fresco.logging;

import dk.alexandra.fresco.framework.ProtocolEvaluator;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EvaluatorLoggingDecorator<
    ResourcePoolT extends ResourcePool,
    Builder extends ProtocolBuilder
    >
    implements ProtocolEvaluator<ResourcePoolT>, PerformanceLogger {

  public static final String SCE_RUNNINGTIMES = "Evaluation time for evaluator ";

  private ProtocolEvaluator<ResourcePoolT> delegate;
  private List<Long> runtimeLogger = new ArrayList<>();

  public EvaluatorLoggingDecorator(ProtocolEvaluator<ResourcePoolT> delegate) {
    this.delegate = delegate;
  }

  @Override
  public void reset() {
    this.runtimeLogger.clear();
  }

  @Override
  public Map<String, Long> getLoggedValues() {
    Map<String, Long> values = new HashMap<>();
    for (int i = 0; i < runtimeLogger.size(); i++) {
      values.put(SCE_RUNNINGTIMES + i, runtimeLogger.get(i));
    }
    return values;
  }

  @Override
  public EvaluationStatistics eval(
      ProtocolProducer protocolProducer, ResourcePoolT resourcePool, Network network) {
    long then = System.currentTimeMillis();
    EvaluationStatistics eval = delegate.eval(protocolProducer, resourcePool, network);
    long now = System.currentTimeMillis();
    long runningTime = now - then;
    this.runtimeLogger.add(runningTime);
    return eval;
  }
}
