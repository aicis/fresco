package dk.alexandra.fresco.tools.mascot;

import dk.alexandra.fresco.framework.util.ExceptionConverter;
import dk.alexandra.fresco.framework.util.Pair;
import java.io.Closeable;
import java.math.BigInteger;
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
  public TestRuntime() {
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
    ExceptionConverter.safe(()-> {
      executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
      return null;
    }, "Executor shutdown failed");
  }

  /**
   * Creates a new executor service with fixed-size thread pool.
   *
   * @param numParties number of threads in thread pool (one per party)
   */
  private void initalizeExecutor(int numParties) {
    if (executorInitialized) {
      throw new IllegalStateException("Executor already initialized");
    }
    executorInitialized = true;
    executor = Executors.newFixedThreadPool(numParties);
  }

  /**
   * Invokes tasks and unwraps futures. <br> Uses {@link ExceptionConverter#safe(Callable, String)}
   * to convert checked exceptions.
   *
   * @param tasks task to invoke
   * @return results of tasks
   */
  private <T> List<T> safeInvokeAll(List<Callable<T>> tasks) {
    Callable<List<Future<T>>> runAll = () -> {
      return executor.invokeAll(tasks, timeout, TimeUnit.SECONDS);
    };
    List<Future<T>> futures = ExceptionConverter.safe(runAll, "Invoke all failed");
    return futures.stream().map(future -> {
      return ExceptionConverter.safe(() -> future.get(), "Party task failed");
    }).collect(Collectors.toList());
  }

  /**
   * Given a ready executor, creates as Mascot test context for each party.
   *
   * @param partyIds the parties
   * @param modulus the modulus
   * @param modBitLength length of modulus
   * @param lambdaSecurityParam lambda security
   * @param numLeftFactors num sacrifice factors
   * @param prgSeedLength prg seed bit length
   * @return map of initialized contexts
   */
  public Map<Integer, MascotTestContext> initializeContexts(
      List<Integer> partyIds, int instanceId, BigInteger modulus,
      int modBitLength, int lambdaSecurityParam, int numLeftFactors,
      int prgSeedLength) {
    initalizeExecutor(partyIds.size());
    List<Callable<Pair<Integer, MascotTestContext>>> initializationTasks = new LinkedList<>();
    for (Integer partyId : partyIds) {
      initializationTasks.add(() -> initializeContext(partyId, partyIds,
          instanceId, modulus, modBitLength,
          lambdaSecurityParam, numLeftFactors, prgSeedLength));
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
   *
   * @param myId this party's id
   * @param partyIds the parties
   * @param modulus the modulus
   * @param modBitLength length of modulus
   * @param lambdaSecurityParam lambda security
   * @param numLeftFactors num sacrifice factors
   * @param prgSeedLength prg seed bit length
   * @return map of initialized contexts
   */
  private Pair<Integer, MascotTestContext> initializeContext(Integer myId, List<Integer> partyIds,
      int instanceId, BigInteger modulus, int modBitLength,
      int lambdaSecurityParam, int numLeftFactors, int prgSeedLength) {
    MascotTestContext ctx = new MascotTestContext(myId, new LinkedList<>(
        partyIds), instanceId, modulus,
        modBitLength, lambdaSecurityParam, numLeftFactors, prgSeedLength);
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
