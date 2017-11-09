package dk.alexandra.fresco.tools.mascot.net;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import dk.alexandra.fresco.framework.network.KryoNetNetwork;
import dk.alexandra.fresco.framework.util.Pair;

public class ExtendedKryoNetNetworkImpl extends KryoNetNetwork implements ExtendedNetwork {

  private ExecutorService executor;
  // TODO: hack to keep KryoNetNetwork unmodified for now
  private Integer myId;
  private List<Integer> partyIds;

  public ExtendedKryoNetNetworkImpl(ExecutorService executor, Integer myId,
      List<Integer> partyIds) {
    super();
    this.executor = executor;
    this.myId = myId;
    this.partyIds = partyIds;
  }

  public CompletableFuture<Void> sendAsynch(Integer partyId, byte[] data) {
    return CompletableFuture.runAsync(() -> {
      try {
        send(0, partyId, data);
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }, executor);
  }

  public CompletableFuture<Pair<Integer, byte[]>> receiveAsynch(Integer partyId) {
    return CompletableFuture.supplyAsync(() -> {
      try {
        byte[] raw = receive(0, partyId);
        return new Pair<>(partyId, raw);
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
        return null;
      }
    }, executor);
  }

  @Override
  public void sendToAll(byte[] data) {
    List<CompletableFuture<Void>> sendTasks = new ArrayList<>();
    for (Integer partyId : partyIds) {
      if (!partyId.equals(myId)) {
        sendTasks.add(sendAsynch(partyId, data));
      }
    }
    // force synchronization for now
    CompletableFuture.allOf(sendTasks.stream().toArray(i -> new CompletableFuture[i])).join();
  }

  @Override
  public Map<Integer, byte[]> receiveFromAll() {
    List<CompletableFuture<Pair<Integer, byte[]>>> received = new ArrayList<>();
    for (Integer partyId : partyIds) {
      if (!partyId.equals(myId)) {
        received.add(receiveAsynch(partyId));
      }
    }
    // force synchronization for now
    CompletableFuture.allOf(received.stream().toArray(i -> new CompletableFuture[i])).join();
    Map<Integer, byte[]> result = new HashMap<>();
    for (CompletableFuture<Pair<Integer, byte[]>> res : received) {
      try {
        Pair<Integer, byte[]> pair = res.get();
        result.put(pair.getFirst(), pair.getSecond());
      } catch (InterruptedException e) {
        e.printStackTrace();
      } catch (ExecutionException e) {
        e.printStackTrace();
      }
    }
    return result;
  }

}
