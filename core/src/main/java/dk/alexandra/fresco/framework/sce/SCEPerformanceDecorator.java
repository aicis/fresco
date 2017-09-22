package dk.alexandra.fresco.framework.sce;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.PerformanceLogger;
import dk.alexandra.fresco.framework.PerformanceLogger.Flag;
import dk.alexandra.fresco.framework.ProtocolEvaluator;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedSequentialEvaluator;
import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.framework.sce.evaluator.SequentialEvaluator;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.suite.ProtocolSuite;
import java.util.concurrent.Future;

public class SCEPerformanceDecorator<ResourcePoolT extends ResourcePool, Builder extends ProtocolBuilder>
    implements SecureComputationEngine<ResourcePoolT, Builder> {

  private SecureComputationEngine<ResourcePoolT, Builder> sce;
  private PerformanceLogger pl;
  private EvaluationStrategy strategy;
  private String protocolSuiteName;

  public SCEPerformanceDecorator(SecureComputationEngine<ResourcePoolT, Builder> sce,
      ProtocolEvaluator<ResourcePoolT, Builder> evaluator,
      ProtocolSuite<ResourcePoolT, Builder> suite, PerformanceLogger pl) {
    super();
    this.sce = sce;
    this.pl = pl;
    if (evaluator instanceof SequentialEvaluator) {
      strategy = EvaluationStrategy.SEQUENTIAL;
    } else if (evaluator instanceof BatchedSequentialEvaluator) {
      strategy = EvaluationStrategy.SEQUENTIAL_BATCHED;
    }
    this.protocolSuiteName = suite.getClass().getName();
  }

  @Override
  public <OutputT> OutputT runApplication(Application<OutputT, Builder> application,
      ResourcePoolT resources) {
    long then = System.currentTimeMillis();
    OutputT res = this.sce.runApplication(application, resources);
    long now = System.currentTimeMillis();
    long timeSpend = now - then;
    if (pl != null && pl.flags.contains(Flag.LOG_RUNTIME)) {
      pl.informRuntime(application, timeSpend, strategy, protocolSuiteName);
    }
    return res;
  }

  @Override
  public <OutputT> Future<OutputT> startApplication(Application<OutputT, Builder> application,
      ResourcePoolT resources) {
    // TODO: If applications are started this way, no running time logging is applied. We need to
    // inject a decorator future which logs the timing before handing over the result.
    return this.sce.startApplication(application, resources);
  }

  @Override
  public void setup() {
    this.sce.setup();
  }

  @Override
  public void shutdownSCE() {
    this.sce.shutdownSCE();
  }

}
