package dk.alexandra.fresco.tools.mascot.cope;

import java.math.BigInteger;
import java.util.Random;
import java.util.concurrent.ExecutorService;

import dk.alexandra.fresco.tools.mascot.TwoPartyProtocol;
import dk.alexandra.fresco.tools.mascot.net.ExtendedNetwork;
import dk.alexandra.fresco.tools.mascot.utils.DummyPRF;
import dk.alexandra.fresco.tools.mascot.utils.PRF;
import dk.alexandra.fresco.tools.ot.base.DummyOTBatch;
import dk.alexandra.fresco.tools.ot.base.OTBatch;

public class CopeShared extends TwoPartyProtocol {

  protected int lambdaSecurityParam;
  protected BigInteger counter;
  protected OTBatch<BigInteger> ot;
  protected boolean initialized;
  protected PRF prf;

  public CopeShared(Integer myId, Integer otherId, int kBitLength, int lambdaSecurityParam, Random rand,
      ExtendedNetwork network, ExecutorService executor, BigInteger modulus) {
    super(myId, otherId, modulus, kBitLength, network, executor, rand);
    this.lambdaSecurityParam = lambdaSecurityParam;
    this.counter = BigInteger.valueOf(0);
    this.ot = new DummyOTBatch(otherId, network);
    this.initialized = false;
    this.prf = new DummyPRF();
  }

  public int getLambdaSecurityParam() {
    return lambdaSecurityParam;
  }

  public void setLambdaSecurityParam(int lambdaSecurityParam) {
    this.lambdaSecurityParam = lambdaSecurityParam;
  }

}
