package dk.alexandra.fresco.tools.mascot.net;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.network.KryoNetNetwork;

public class ExtendedKryoNetNetworkImpl extends KryoNetNetwork implements ExtendedNetwork {

  private ExecutorService executor;
  private Integer myId;
  private List<Integer> partyIds;

  public ExtendedKryoNetNetworkImpl(ExecutorService executor, Integer myId,
      List<Integer> partyIds, NetworkConfiguration conf) {
    super(conf);
    this.executor = executor;
    this.myId = myId;
    this.partyIds = partyIds;
  }

  public CompletableFuture<Void> sendAsynch(Integer partyId, byte[] data) {
    return CompletableFuture.runAsync(() -> {
      send(partyId, data);
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

}
