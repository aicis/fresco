/*
 * Copyright (c) 2015, 2016 FRESCO (http://github.com/aicis/fresco).
 *
 * This file is part of the FRESCO project.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * FRESCO uses SCAPI - http://crypto.biu.ac.il/SCAPI, Crypto++, Miracl, NTL, and Bouncy Castle.
 * Please see these projects for any further licensing issues.
 *******************************************************************************/
package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.BuilderFactory;
import dk.alexandra.fresco.framework.PerformanceLogger;
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

public class SpdzProtocolSuite implements ProtocolSuite<SpdzResourcePool, ProtocolBuilderNumeric> {

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
        new BasicNumericContext(maxBitLength, resourcePool.getModulus(), resourcePool);
    return new SpdzBuilder(spdzFactory);
  }

  @Override
  public RoundSynchronization<SpdzResourcePool> createRoundSynchronization() {
    return new SpdzRoundSynchronization();
  }

  @Override
  public SpdzResourcePool createResourcePool(int myId, int size, Network network, Random rand,
      SecureRandom secRand, PerformanceLogger pl) {
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
    return new SpdzResourcePoolImpl(myId, size, network, rand, secRand, store, pl);
  }


}
