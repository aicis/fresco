package dk.alexandra.fresco.framework.sce;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.BuilderFactory;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.ProtocolEvaluator;
import dk.alexandra.fresco.framework.ProtocolEvaluator.EvaluationStatistics;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.suite.ProtocolSuite;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Secure Computation Engine - responsible for having the overview of things and setting everything
 * up, e.g., based on properties.
 */
public class SecureComputationEngineImpl
    <ResourcePoolT extends ResourcePool, BuilderT extends ProtocolBuilder>
    implements SecureComputationEngine<ResourcePoolT, BuilderT> {

  private ProtocolEvaluator<ResourcePoolT> evaluator;
  private ExecutorService executorService;
  private boolean setup;
  private ProtocolSuite<ResourcePoolT, BuilderT> protocolSuite;
  private static final AtomicInteger threadCounter = new AtomicInteger(1);
  private static final Logger logger = LoggerFactory.getLogger(SecureComputationEngineImpl.class);

  /**
   * Creates a new {@link SecureComputationEngineImpl}.
   *
   * @param protocolSuite {@link ProtocolSuite} to use to evaluate the secure computation. Not nullable.
   * @param evaluator {@link ProtocolEvaluator} to run secure evaluation. Not nullable.
   */
  public SecureComputationEngineImpl(ProtocolSuite<ResourcePoolT, BuilderT> protocolSuite,
      ProtocolEvaluator<ResourcePoolT> evaluator) {
    this.protocolSuite = Objects.requireNonNull(protocolSuite);
    this.evaluator = Objects.requireNonNull(evaluator);
    this.setup = false;
  }

  @Override
  public <OutputT> OutputT runApplication(Application<OutputT, BuilderT> application,
      ResourcePoolT resourcePool, Network network, Duration timeout) {
    Future<OutputT> future = startApplication(application, resourcePool, network);
    try {
      return future.get(timeout.toNanos(), TimeUnit.NANOSECONDS);
    } catch (InterruptedException | TimeoutException e) {
      throw new RuntimeException("Internal error in waiting", e);
    } catch (ExecutionException e) {
      throw new RuntimeException("Execution exception when running the application", e.getCause());
    }
  }

  @Override
  public <OutputT> Future<OutputT> startApplication(Application<OutputT, BuilderT> application,
      ResourcePoolT resourcePool, Network network) {
    setup();
    Callable<OutputT> callable = () -> evalApplication(application, resourcePool, network).out();
    return executorService.submit(callable);
  }

  private <OutputT> DRes<OutputT> evalApplication(Application<OutputT, BuilderT> application,
      ResourcePoolT resourcePool, Network network) {
    logger.info(
        "Running application: " + application + " using protocol suite: " + this.protocolSuite);
    BuilderFactory<BuilderT> protocolFactory = this.protocolSuite.init(resourcePool);
    BuilderT builder = protocolFactory.createSequential();
    final DRes<OutputT> output = application.buildComputation(builder);
    long then = System.currentTimeMillis();
    EvaluationStatistics eval = this.evaluator.eval(builder.build(), resourcePool, network);

    logger.debug("Evaluator done."
        + " Evaluated a total of " + eval.getNativeProtocols()
        + " native protocols in " + eval.getBatches() + " batches.");

    long now = System.currentTimeMillis();
    long timeSpent = now - then;
    logger.info("The application {} finished evaluation in {} ms.", application, timeSpent);
    application.close();
    return output;
  }

  @Override
  public synchronized void setup() {
    if (!this.setup) {
      this.executorService = Executors.newCachedThreadPool(r -> {
        Thread thread = new Thread(r, "SCE-" + threadCounter.getAndIncrement());
        thread.setDaemon(true);
        return thread;
      });
      this.setup = true;
    }
  }

  @Override
  public synchronized void close() {
    if (this.setup) {
      this.executorService.shutdown();
    }
    this.setup = false;
  }

}
