package dk.alexandra.fresco.suite.otextension;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import dk.alexandra.fresco.framework.network.Network;

public class COTeReceiver extends COTeShared {
  // The values receives from the random seed OTs
  private List<BigInteger> chosenSeeds;
  // The random messages choices for the random seed OTs
  private List<Boolean> otChoices;

  public COTeReceiver(int otherID, int kBitLength, int lambdaSecurityParam,
      Random rand, Network network) {
    super(otherID, kBitLength, lambdaSecurityParam, rand, network);
    this.chosenSeeds = new ArrayList<>(kBitLength);
    this.otChoices = new ArrayList<>(kBitLength);
  }

  public void initialize() {
    if (initialized) {
      throw new IllegalStateException("Already initialized");
    }
    // Pick random choices and complete OTs
    for (int i = 0; i < kBitLength; i++) {
      Boolean choiceBit = rand.nextBoolean();
      otChoices.add(choiceBit);
      chosenSeeds.add(ot.receive(choiceBit));
    }
    initialized = true;
  }
}
