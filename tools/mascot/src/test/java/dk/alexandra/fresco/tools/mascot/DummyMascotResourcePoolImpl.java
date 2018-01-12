package dk.alexandra.fresco.tools.mascot;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.tools.cointossing.CoinTossing;
import dk.alexandra.fresco.tools.ot.base.DummyOt;
import dk.alexandra.fresco.tools.ot.base.Ot;
import dk.alexandra.fresco.tools.ot.base.RotBatch;
import dk.alexandra.fresco.tools.ot.otextension.BristolRotBatch;
import dk.alexandra.fresco.tools.ot.otextension.OtExtensionResourcePool;
import dk.alexandra.fresco.tools.ot.otextension.OtExtensionResourcePoolImpl;
import dk.alexandra.fresco.tools.ot.otextension.RotList;

import java.math.BigInteger;
import java.util.List;

public class DummyMascotResourcePoolImpl extends MascotResourcePoolImpl {
  public DummyMascotResourcePoolImpl(Integer myId, List<Integer> partyIds,
      int instanceId, Drbg drbg,
      BigInteger modulus, int modBitLength, int lambdaSecurityParam, int prgSeedLength,
      int numLeftFactors) {
    // RotList seedOts = new RotList(drbg, getPrgSeedLength());
    super(myId, partyIds, instanceId, drbg, null, modulus, modBitLength,
        lambdaSecurityParam, prgSeedLength,
        numLeftFactors);
  }

  @Override
  public RotBatch createRot(int otherId, Network network) {
    if (getMyId() == otherId) {
      throw new IllegalArgumentException("Cannot initialize with self");
    }
    RotList seedOts = new RotList(getRandomGenerator(), getPrgSeedLength());
    Ot ot = new DummyOt(otherId, network);
    seedOts.send(ot);
    seedOts.receive(ot);
    CoinTossing ct = new CoinTossing(getMyId(), otherId, getRandomGenerator(),
        network);
    ct.initialize();
    OtExtensionResourcePool otResources = new OtExtensionResourcePoolImpl(
        getMyId(), otherId, getModBitLength(), getLambdaSecurityParam(),
        getInstanceId(), getRandomGenerator(), ct, seedOts);
    return new BristolRotBatch(otResources, network);

  }

}
