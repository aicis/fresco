package dk.alexandra.fresco.tools.mascot.cope;

import java.math.BigInteger;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import dk.alexandra.fresco.tools.mascot.net.ExtendedNetwork;

public class Cope {

  protected CopeInputter inputter;
  protected CopeSigner signer;

  public Cope(Integer myId, Integer otherId, int kBitLength, int lambdaSecurityParam, Random rand,
      FieldElement macKeyShare, ExtendedNetwork network, ExecutorService executor, BigInteger modulus) {
    this.inputter = new CopeInputter(myId, otherId, kBitLength, lambdaSecurityParam, rand, network,
        executor, modulus);
    this.signer = new CopeSigner(myId, otherId, kBitLength, lambdaSecurityParam, rand, network,
        executor, modulus, macKeyShare);
  }

  public CopeInputter getInputter() {
    return inputter;
  }

  public CopeSigner getSigner() {
    return signer;
  }

  public void initialize() {
    // Could also run this in separate threads
    if (this.inputter.getMyId() < this.inputter.getOtherId()) {
      inputter.initialize();
      signer.initialize();
    } else {
      signer.initialize();
      inputter.initialize();
    }
  }

  public CompletableFuture<Void> initializeAsynch(Executor executor) {
    return CompletableFuture.runAsync(() -> initialize(), executor);
  }

}
