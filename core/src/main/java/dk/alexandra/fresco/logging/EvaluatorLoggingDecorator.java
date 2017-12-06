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
import org.slf4j.Logger;

public class EvaluatorLoggingDecorator<
    ResourcePoolT extends ResourcePool,
    Builder extends ProtocolBuilder
    >
    implements ProtocolEvaluator<ResourcePoolT, Builder>, PerformanceLogger{

  public static final String ID = "PARTY_ID";
  public static final String SCE_RUNNINGTIMES = "RUNNING_TIMES";
  
  private ProtocolEvaluator<ResourcePoolT, Builder> delegate;
  private List<Long> runtimeLogger = new ArrayList<>();
  
  public EvaluatorLoggingDecorator(ProtocolEvaluator<ResourcePoolT, Builder> delegate) {
    this.delegate = delegate;
  }
  
  @Override
  public void printToLog(Logger log, int myId) {
    log.info("=== P" + myId + ": Running times for evaluations ===");
    if (this.runtimeLogger.isEmpty()) {
      log.info("No applications were run, or they have not completed yet.");
    }
    int i = 1;
    for (Long runningtime : this.runtimeLogger) {
      log.info("Application "+i+" took "+runningtime + "ms to complete.");
      i++;
    }
  }

  @Override
  public void reset() {
    this.runtimeLogger.clear();
  }

  @Override
  public Map<String, Object> getLoggedValues(int myId) {
    Map<String, Object> values = new HashMap<>();
    values.put(ID, myId);
    values.put(SCE_RUNNINGTIMES, runtimeLogger);
    return values;
  }

  @Override
  public void eval(ProtocolProducer protocolProducer, ResourcePoolT resourcePool, Network network) {
    long then = System.currentTimeMillis();
    this.delegate.eval(protocolProducer, resourcePool, network);
    long now = System.currentTimeMillis();
    long runningTime = now-then;
    this.runtimeLogger.add(runningTime);
  }
}
