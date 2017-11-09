package dk.alexandra.fresco.tools.mascot.cope;

import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import dk.alexandra.fresco.framework.network.Network;
import java.math.BigInteger;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class Cope {

  protected Integer myID;
  protected Integer otherID;
  protected CopeInputter inputter;
  protected CopeSigner signer;

  public Cope(int myID, int otherID, int kBitLength, int lambdaSecurityParam, Random rand,
      FieldElement macKeyShare, Network network, BigInteger prime) {
    this.myID = myID;
    this.otherID = otherID;
    this.inputter =
        new CopeInputter(otherID, kBitLength, lambdaSecurityParam, rand, network, prime);
    this.signer =
        new CopeSigner(otherID, kBitLength, lambdaSecurityParam, rand, macKeyShare, network, prime);
  }

  public CopeInputter getInputter() {
    return inputter;
  }

  public CopeSigner getSigner() {
    return signer;
  }

  public void initialize() {
    // Could also run this in separate threads
    if (myID < otherID) {
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
