package dk.alexandra.fresco.framework.sce.resources;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.resources.storage.StreamedStorage;
import dk.alexandra.fresco.framework.sce.resources.threads.VMThreadPool;
import java.security.SecureRandom;
import java.util.Random;

public class SpdzResourcePool extends ResourcePoolImpl {

  public SpdzResourcePool(int myId, int noOfPlayers,
      Network network,
      StreamedStorage streamedStorage,
      Random random, SecureRandom secRand,
      VMThreadPool vmThreadPool) {
    super(myId, noOfPlayers, network, streamedStorage, random, secRand);
  }
}
