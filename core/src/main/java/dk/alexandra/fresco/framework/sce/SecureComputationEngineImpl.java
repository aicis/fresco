package dk.alexandra.fresco.framework.sce;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.BuilderFactory;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.ProtocolEvaluator;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.suite.ProtocolSuite;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.LoggerFactory;

/**
 * Secure Computation Engine - responsible for having the overview of things and setting everything
 * up, e.g., based on properties.
 *
 */
public class SecureComputationEngineImpl<ResourcePoolT extends ResourcePool, Builder extends ProtocolBuilder>
    implements SecureComputationEngine<ResourcePoolT, Builder> {

  private ProtocolEvaluator<ResourcePoolT, Builder> evaluator;
  private ExecutorService executorService;
  private boolean setup;
  private ProtocolSuite<ResourcePoolT, Builder> protocolSuite;
  private static final AtomicInteger threadCounter = new AtomicInteger(1);
  private final static org.slf4j.Logger logger =
      LoggerFactory.getLogger(SecureComputationEngineImpl.class);

  public SecureComputationEngineImpl(ProtocolSuite<ResourcePoolT, Builder> protocolSuite,
      ProtocolEvaluator<ResourcePoolT, Builder> evaluator) {
    this.protocolSuite = protocolSuite;

    this.setup = false;

    this.evaluator = evaluator;
  }

  @Override
  public <OutputT> OutputT runApplication(Application<OutputT, Builder> application,
      ResourcePoolT resourcePool) {
    Future<OutputT> future = startApplication(application, resourcePool);
    try {
      return future.get(10, TimeUnit.MINUTES);
    } catch (InterruptedException | TimeoutException e) {
      throw new RuntimeException("Internal error in waiting", e);
    } catch (ExecutionException e) {
      throw new RuntimeException("Execution exception when running the application", e.getCause());
    }
  }

  public <OutputT> Future<OutputT> startApplication(Application<OutputT, Builder> application,
      ResourcePoolT resourcePool) {
    setup();
    Callable<OutputT> callable = () -> evalApplication(application, resourcePool).out();
    return executorService.submit(callable);
  }

  private <OutputT> DRes<OutputT> evalApplication(Application<OutputT, Builder> application,
      ResourcePoolT resourcePool) throws Exception {
    logger.info(
        "Running application: " + application + " using protocol suite: " + this.protocolSuite);
    try {
      BuilderFactory<Builder> protocolFactory = this.protocolSuite.init(resourcePool);
      Builder builder = protocolFactory.createSequential();
      DRes<OutputT> output = application.buildComputation(builder);

      long then = System.currentTimeMillis();
      this.evaluator.eval(builder.build(), resourcePool);
      long now = System.currentTimeMillis();
      long timeSpend = now - then;
      logger
          .info("The application " + application + " finished evaluation in " + timeSpend + " ms.");
      application.close();
      return output;
    } catch (IOException e) {
      throw new MPCException("Could not run application " + application + " due to errors", e);
    }
  }

  @Override
  public synchronized void setup() {
    if (this.setup) {
      return;
    }
    this.executorService = Executors.newCachedThreadPool(r -> {
      Thread thread = new Thread(r, "SCE-" + threadCounter.getAndIncrement());
      thread.setDaemon(true);
      return thread;
    });
    this.evaluator.setProtocolInvocation(this.protocolSuite);
    this.setup = true;
  }

  @Override
  public synchronized void shutdownSCE() {
    if (this.setup) {
      this.executorService.shutdown();
    }
    this.setup = false;
  }

}
