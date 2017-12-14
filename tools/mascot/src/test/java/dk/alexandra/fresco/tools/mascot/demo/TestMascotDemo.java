package dk.alexandra.fresco.tools.mascot.demo;

import java.util.concurrent.Callable;

import org.junit.Test;

public class TestMascotDemo {

  @Test
  public void testDemoRuns() throws InterruptedException {
    // only testing that demo runs, correctness of results is tested elsewhere
    Callable<Void> partyOne = () -> {
      MascotDemo.main(new String[] {"1"});
      return null;
    };
    Callable<Void> partyTwo = () -> {
      MascotDemo.main(new String[] {"2"});
      return null;
    };
    // TODO decide what to do about slow test/ how to speed it up
    // ExecutorService executor = Executors.newFixedThreadPool(2);
    // executor.invokeAll(Arrays.asList(partyOne, partyTwo));
  }

}
