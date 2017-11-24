package dk.alexandra.fresco.tools.mascot;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.alexandra.fresco.framework.util.Pair;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class TestRuntime {

  private final static Logger logger = LoggerFactory.getLogger(TestRuntime.class);

  private Map<Integer, MascotContext> contexts;
  private ExecutorService executor;
  private boolean executorInitialized;

  public TestRuntime() {
    this.contexts = new HashMap<>();
    this.executor = null;
    this.executorInitialized = false;
  }

  /**
   * Closes the networks on the contexts and shuts down the executor. Call this after test.
   * 
   * @param executor
   * @param contexts
   */
  public void shutdown() {
    if (!executorInitialized) {
      throw new IllegalStateException("Executor not initialized, nothing to shutd down.");
    }
    executorInitialized = false;
    try {
      for (MascotContext context : contexts.values()) {
        ((Closeable) context.getNetwork()).close();
      }
    } catch (IOException e) {
      logger.error("Network shutdown failed");
      e.printStackTrace();
    }
    executor.shutdown();
    try {
      executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    } catch (InterruptedException e) {
      logger.error("Executor shutdown failed");
      e.printStackTrace();
    }
  }

  /**
   * Creates a new executor service with fixed-size thread pool.
   * 
   * @param numParties number of threads in thread pool (one per party)
   * @return
   */
  private void initalizeExecutor(int numParties) {
    if (executorInitialized) {
      throw new IllegalStateException("Executor already initialized");
    }
    executorInitialized = true;
    executor = Executors.newFixedThreadPool(numParties);
  }

  /**
   * Given a ready executor, creates as Mascot context for each party.
   * 
   * @param executor The executor that will run each party in a thread
   * @param partyIds The parties
   * @return
   */
  public Map<Integer, MascotContext> initializeContexts(List<Integer> partyIds) {
    initalizeExecutor(partyIds.size());
    try {
      List<Callable<Pair<Integer, MascotContext>>> initializationTasks = new LinkedList<>();
      for (Integer partyId : partyIds) {
        initializationTasks.add(() -> initializeContext(partyId, partyIds));
      }
      for (Future<Pair<Integer, MascotContext>> pair : executor.invokeAll(initializationTasks)) {
        Pair<Integer, MascotContext> unwrapped = pair.get();
        contexts.put(unwrapped.getFirst(), unwrapped.getSecond());
      }
      return contexts;
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (ExecutionException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return new HashMap<>();
  }

  /**
   * Runs the task defined for each party.
   * 
   * Currently assumes that all parties receive the same type of output.
   * 
   * @param tasks
   * @return
   */
  public <T> Map<Integer, T> runPerPartyTasks(Map<Integer, Callable<T>> tasks) {
    throw new NotImplementedException();
  }

  /**
   * Runs the task defined for each party.
   * 
   * Currently assumes that all parties receive the same type of output.
   * 
   * This method assumes that tasks are ordered by party.
   * 
   * @param tasks
   * @return
   */
  public <T> List<T> runPerPartyTasks(List<Callable<T>> tasks) {
    if (!executorInitialized) {
      throw new IllegalStateException("Executor not initialized yet");
    }
    List<Future<T>> results;
    try {
      results = executor.invokeAll(tasks);
      // this is a bit of a mess...
      List<T> unwrappedResults = results.stream().map(future -> {
        try {
          return future.get();
        } catch (InterruptedException | ExecutionException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        return null;
      }).collect(Collectors.toList());
      return unwrappedResults;
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Initializes a single context for a party.
   * 
   * @param myId
   * @param partyIds
   * @return
   */
  private Pair<Integer, MascotContext> initializeContext(Integer myId, List<Integer> partyIds) {
    MascotContext ctx = MascotContext.defaultContext(myId, new LinkedList<>(partyIds));
    return new Pair<>(myId, ctx);
  }

}