package dk.alexandra.fresco.tools.helper;

import dk.alexandra.fresco.framework.Party;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.NetworkConfigurationImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class RuntimeForTests {
  private ExecutorService executor;

  public RuntimeForTests() {
    this.executor = null;
    executor = Executors.newFixedThreadPool(2);
  }

  /**
   * Closes the networks on the contexts and shuts down the executor. Call this
   * after test.
   */
  public void shutdown() {
    executor.shutdown();
    try {
      executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  /**
   * Returns a default network configuration.
   *
   * @param myId
   *          The calling party's network ID
   * @param partyIds
   *          The IDs of all parties
   * @return The network configuration
   */
  public static NetworkConfiguration defaultNetworkConfiguration(Integer myId,
      List<Integer> partyIds) {
    Map<Integer, Party> parties = new HashMap<>();
    for (Integer partyId : partyIds) {
      parties.put(partyId, new Party(partyId, "localhost", 8000 + partyId));
    }
    return new NetworkConfigurationImpl(myId, parties);
  }

  /**
   * Runs the task defined for each party. Currently assumes that all parties receive the same type
   * of output. This method assumes that tasks are ordered by party.
   *
   * @param tasks
   *          Tasks to complete
   * @return List of the results given by each of the tasks
   */
  public <T> List<T> runPerPartyTasks(List<Callable<T>> tasks) {
    try {
      List<Future<T>> results = executor.invokeAll(tasks, 15L,
          TimeUnit.SECONDS);
      // this is a bit of a mess...
      @SuppressWarnings("unchecked")
      List<T> unwrappedResults = results.stream().map(future -> {
        try {
          return future.get();
        } catch (CancellationException e) {
          System.err.println("Task cancelled due to time-out");
          e.printStackTrace();
        } catch (InterruptedException e) {
          System.err.println("Task execution failed");
          e.printStackTrace();
        } catch (ExecutionException e) {
          // This is very ugly, but we want to be able to receive the checked
          // exceptions thrown
          return (T) e.getCause();
        }
        return null;
      }).collect(Collectors.toList());
      return unwrappedResults;
    } catch (InterruptedException e) {
      System.err.println("Task execution failed");
      e.printStackTrace();
    }
    return new ArrayList<>();
  }

}
