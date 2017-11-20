package dk.alexandra.fresco.tools.mascot.cope;

import java.math.BigInteger;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import dk.alexandra.fresco.tools.mascot.net.ExtendedNetwork;

public class CopeBiDirectional {

  protected CopeInputter inputter;
  protected CopeSigner signer;

  public CopeBiDirectional(Integer myId, Integer otherId, int kBitLength, int lambdaSecurityParam, Random rand,
      FieldElement macKeyShare, ExtendedNetwork network, ExecutorService executor, BigInteger modulus) {
    this.inputter = new CopeInputter(myId, otherId, kBitLength, lambdaSecurityParam, rand, network,
        executor, modulus);
    this.signer = new CopeSigner(myId, otherId, kBitLength, lambdaSecurityParam, rand, network,
        executor, modulus, macKeyShare);
  }

  public FieldElement inputterExtend(FieldElement inputElement) {
    return inputter.extend(inputElement);
  }
  
  public List<FieldElement> inputterExtend(List<FieldElement> inputElements) {
    return inputter.extend(inputElements);
  }
  
  public FieldElement signerExtend() {
    return signer.extend();
  }
  
  public List<FieldElement> signerExtend(int numInputs) {
    return signer.extend(numInputs);
  }
  
  public void initializeBoth() {
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
    return CompletableFuture.runAsync(() -> initializeBoth(), executor);
  }

}
