package dk.alexandra.fresco.tools.mascot;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.tools.ot.base.DummyOt;
import dk.alexandra.fresco.tools.ot.base.Ot;
import dk.alexandra.fresco.tools.ot.base.RotBatch;
import dk.alexandra.fresco.tools.ot.otextension.BristolRotBatch;
import dk.alexandra.fresco.tools.ot.otextension.OtExtensionResourcePool;
import dk.alexandra.fresco.tools.ot.otextension.OtExtensionResourcePoolImpl;

import java.math.BigInteger;
import java.util.List;

public class DummyMascotResourcePoolImpl extends MascotResourcePoolImpl {

  public DummyMascotResourcePoolImpl(Integer myId, List<Integer> partyIds, Drbg drbg,
      BigInteger modulus, int modBitLength, int lambdaSecurityParam, int prgSeedLength,
      int numLeftFactors) {
    super(myId, partyIds, drbg, modulus, modBitLength, lambdaSecurityParam, prgSeedLength,
        numLeftFactors);
  }

  @Override
  public RotBatch createRot(int otherId, Network network) {
    Ot ot = new DummyOt(otherId, network);
    OtExtensionResourcePool otResources = new OtExtensionResourcePoolImpl(getMyId(), otherId,
        getModBitLength(), getLambdaSecurityParam(), getRandomGenerator());
    return new BristolRotBatch(otResources, network, ot);

  }

}
