package dk.alexandra.fresco.suite.marlin;

import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.suite.ProtocolSuite.RoundSynchronization;
import dk.alexandra.fresco.suite.marlin.resource.MarlinResourcePool;

public class MarlinRoundSynchronization implements RoundSynchronization<MarlinResourcePool> {

  @Override
  public void beforeBatch(ProtocolCollection<MarlinResourcePool> nativeProtocols,
      MarlinResourcePool resourcePool, Network network) {
  }

  @Override
  public void finishedBatch(int gatesEvaluated, MarlinResourcePool resourcePool, Network network) {
  }

  @Override
  public void finishedEval(MarlinResourcePool resourcePool, Network network) {
  }

}
