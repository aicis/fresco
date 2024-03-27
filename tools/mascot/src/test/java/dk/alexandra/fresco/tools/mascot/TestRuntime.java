package dk.alexandra.fresco.tools.mascot;

import dk.alexandra.fresco.framework.builder.numeric.field.FieldDefinition;
import dk.alexandra.fresco.framework.util.ExceptionConverter;
import dk.alexandra.fresco.framework.util.Pair;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


public class TestRuntime {

  private Map<Integer, MascotTestContext> contexts;
  private ExecutorService executor;
  private boolean executorInitialized;
  private long timeout;

  /**
   * Creates new test runtime.
   */
  TestRuntime() {
    this.contexts = new HashMap<>();
    this.executor = null;
    this.executorInitialized = false;
    this.timeout = 20L;
  }

  /**
   * Closes the networks on the contexts and shuts down the executor. <br> Call this after test.
   */
  public void shutdown() {
    if (!executorInitialized) {
      throw new IllegalStateException("Executor not initialized, nothing to shut down.");
    }
    executorInitialized = false;
    for (MascotTestContext context : contexts.values()) {
      ExceptionConverter.safe(() -> {
        ((Closeable) context.getNetwork()).close();
        return null;
      }, "Closing network failed");
    }
    executor.shutdown();
    ExceptionConverter.safe(() -> {
      executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
      return null;
    }, "Executor shutdown failed");
  }

  /**
   * Creates a new executor service with fixed-size thread pool.
   *
   * @param noOfParties number of threads in thread pool (one per party)
   */
  private void initializeExecutor(int noOfParties) {
    if (executorInitialized) {
      throw new IllegalStateException("Executor already initialized");
    }
    executorInitialized = true;
    executor = Executors.newFixedThreadPool(noOfParties);
  }

  /**
   * Invokes tasks and unwraps futures. <br> Uses {@link ExceptionConverter#safe(Callable, String)}
   * to convert checked exceptions.
   *
   * @param tasks task to invoke
   * @return results of tasks
   */
  private <T> List<T> safeInvokeAll(List<Callable<T>> tasks) {
    Callable<List<Future<T>>> runAll = () -> executor.invokeAll(tasks, timeout, TimeUnit.SECONDS);
    List<Future<T>> futures = ExceptionConverter.safe(runAll, "Invoke all failed");

    return collectFromFutures(futures);
  }

  /**
   * Utility for collecting all values from the given {link Future}s.
   *
   * <p>Will fail if any of the {@link Future}s fails, and show exceptions from
   * all failed {@link Future}s. This is useful for cases where code running in
   * {@link Future}s communicates with each other, as the first failing {@link
   * Future} may not contain the root cause of the issue.
   *
   * @param futures Futures to collect from.
   * @return Values of futures.
   * @exception RuntimeException Raised if any future fails for any reason:
   * cancellation or otherwise. Contains all failing exceptions as supressed.
   */
  private static <T> List<T> collectFromFutures(Iterable<Future<T>> futures) {
    final List<T> collectedResults = new ArrayList<>();
    final List<Exception> exceptions = new ArrayList<>();
    for (final Future<T> future : futures) {
      try {
        collectedResults.add(future.get());
      } catch (Exception e) {
        exceptions.add(e);
      }
    }

    if (!exceptions.isEmpty()) {
      final RuntimeException err = new RuntimeException(String.format("Failures in %d futures", exceptions.size()));
      exceptions.forEach(err::addSuppressed);
      throw err;
    }

    return collectedResults;
  }

  /**
   * Given a ready executor, creates as Mascot test context for each party.
   */
  public Map<Integer, MascotTestContext> initializeContexts(
      int noOfParties, int instanceId,
      MascotSecurityParameters securityParameters, FieldDefinition fieldDefinition) {
    initializeExecutor(noOfParties);
    List<Callable<Pair<Integer, MascotTestContext>>> initializationTasks = new LinkedList<>();
    for (int partyId = 1; partyId <= noOfParties; partyId++) {
      int finalPartyId = partyId;
      initializationTasks.add(() -> initializeContext(finalPartyId, noOfParties,
          instanceId, securityParameters, fieldDefinition));
    }
    for (Pair<Integer, MascotTestContext> pair : safeInvokeAll(initializationTasks)) {
      contexts.put(pair.getFirst(), pair.getSecond());
    }
    return contexts;
  }

  /**
   * Runs the task defined for each party. <br> Currently assumes that all parties receive the same
   * type of output. This method assumes that tasks are ordered by party.
   *
   * @param tasks tasks to run
   * @return result of tasks
   */
  public <T> List<T> runPerPartyTasks(List<Callable<T>> tasks) {
    if (!executorInitialized) {
      throw new IllegalStateException("Executor not initialized yet");
    }
    return safeInvokeAll(tasks);
  }

  /**
   * Initializes a single context for a party.
   */
  private Pair<Integer, MascotTestContext> initializeContext(int myId, int noOfParties,
      int instanceId, MascotSecurityParameters securityParameters,
      FieldDefinition fieldDefinition) {
    MascotTestContext ctx = new MascotTestContext(myId, noOfParties, instanceId,
        securityParameters, fieldDefinition);
    return new Pair<>(myId, ctx);
  }

  /**
   * Check if executor has been initialized.
   *
   * @return is initialized
   */
  public boolean isExecutorInitialized() {
    return executorInitialized;
  }

}
