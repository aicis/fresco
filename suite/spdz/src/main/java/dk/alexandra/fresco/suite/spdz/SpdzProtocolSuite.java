package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.BuilderFactory;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.configuration.ConfigurationException;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.resources.storage.FilebasedStreamedStorageImpl;
import dk.alexandra.fresco.framework.sce.resources.storage.InMemoryStorage;
import dk.alexandra.fresco.lib.field.integer.BasicNumericContext;
import dk.alexandra.fresco.suite.ProtocolSuite;
import dk.alexandra.fresco.suite.spdz.configuration.PreprocessingStrategy;
import dk.alexandra.fresco.suite.spdz.storage.SpdzStorage;
import dk.alexandra.fresco.suite.spdz.storage.SpdzStorageDummyImpl;
import dk.alexandra.fresco.suite.spdz.storage.SpdzStorageImpl;
import java.security.SecureRandom;
import java.util.Random;

public class SpdzProtocolSuite implements
    ProtocolSuite<SpdzResourcePool, ProtocolBuilderNumeric> {

  private final int maxBitLength;
  private final PreprocessingStrategy preproStrat;
  private final String fuelStationUrl;

  public SpdzProtocolSuite(int maxBitLength, PreprocessingStrategy preproStrat,
      String fuelStationUrl) {
    this.maxBitLength = maxBitLength;
    this.preproStrat = preproStrat;
    this.fuelStationUrl = fuelStationUrl;

  }

  @Override
  public BuilderFactory<ProtocolBuilderNumeric> init(SpdzResourcePool resourcePool) {
    BasicNumericContext spdzFactory =
        new BasicNumericContext(maxBitLength, resourcePool.getModulus(),
            resourcePool);
    return new SpdzBuilder(
        spdzFactory);
  }

  @Override
  public RoundSynchronization<SpdzResourcePool> createRoundSynchronization() {
    return new SpdzRoundSynchronization();
  }

  @Override
  public SpdzResourcePool createResourcePool(int myId, int size, Network network, Random rand,
      SecureRandom secRand) {
    SpdzStorage store;
    switch (preproStrat) {
      case DUMMY:
        store = new SpdzStorageDummyImpl(myId, size);
        break;
      case STATIC:
        store = new SpdzStorageImpl(0, size, myId,
            new FilebasedStreamedStorageImpl(new InMemoryStorage()));
        break;
      case FUELSTATION:
        store = new SpdzStorageImpl(0, size, myId, fuelStationUrl);
        break;
      default:
        throw new ConfigurationException("Unkonwn preprocessing strategy: " + preproStrat);
    }
    return new SpdzResourcePoolImpl(myId, size, network, rand, secRand, store);
  }


}