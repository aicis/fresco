package dk.alexandra.fresco.tools.mascot.mult;

import java.math.BigInteger;
import java.util.Random;
import java.util.concurrent.ExecutorService;

import dk.alexandra.fresco.tools.mascot.TwoPartyProtocol;
import dk.alexandra.fresco.tools.mascot.net.ExtendedNetwork;
import dk.alexandra.fresco.tools.ot.base.DummyROTBatch;
import dk.alexandra.fresco.tools.ot.base.ROTBatch;

public class MultiplyShared extends TwoPartyProtocol {

  protected int lambdaSecurityParam;
  protected int numLeftFactors;
  protected ROTBatch<BigInteger> rot;

  public MultiplyShared(Integer myId, Integer otherId, int kBitLength, int lambdaSecurityParam,
      int numLeftFactors, Random rand, ExtendedNetwork network, ExecutorService executor,
      BigInteger modulus) {
    super(myId, otherId, modulus, kBitLength, network, executor, rand);
    this.lambdaSecurityParam = lambdaSecurityParam;
    this.numLeftFactors = numLeftFactors;
    this.rot = new DummyROTBatch(otherId, network, rand, kBitLength);
  }

}
