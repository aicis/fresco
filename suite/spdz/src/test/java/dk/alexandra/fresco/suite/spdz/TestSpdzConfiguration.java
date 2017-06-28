package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.builder.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.configuration.PreprocessingStrategy;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.suite.ProtocolSuite;
import dk.alexandra.fresco.suite.spdz.configuration.SpdzConfiguration;
import java.security.SecureRandom;
import java.util.Random;

class TestSpdzConfiguration implements SpdzConfiguration {

  private final int noOfParties;
  private final PreprocessingStrategy preProStrat;

  TestSpdzConfiguration(int noOfParties, PreprocessingStrategy preProStrat) {
    this.noOfParties = noOfParties;
    this.preProStrat = preProStrat;
  }

  @Override
  public ProtocolSuite<SpdzResourcePool, ProtocolBuilderNumeric> createProtocolSuite(
      int myPlayerId) {
    return new SpdzProtocolSuite(this);
  }

  @Override
  public SpdzResourcePool createResourcePool(int myId, int size, Network network, Random rand,
      SecureRandom secRand) {
    return new SpdzResourcePoolImpl(myId, noOfParties, network, null, rand, secRand, this);
  }

  @Override
  public PreprocessingStrategy getPreprocessingStrategy() {
    return preProStrat;
  }

  @Override
  public String fuelStationBaseUrl() {
    return null;
  }

  @Override
  public int getMaxBitLength() {
    return 150;
  }
}
