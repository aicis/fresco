package dk.alexandra.fresco.tools.mascot.triple;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import dk.alexandra.fresco.tools.mascot.MultiPartyProtocol;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import dk.alexandra.fresco.tools.mascot.mult.MultiplyLeft;
import dk.alexandra.fresco.tools.mascot.mult.MultiplyRight;
import dk.alexandra.fresco.tools.mascot.net.ExtendedNetwork;
import dk.alexandra.fresco.tools.mascot.share.ShareGen;
import dk.alexandra.fresco.tools.mascot.utils.sample.DummySampler;
import dk.alexandra.fresco.tools.mascot.utils.sample.Sampler;

public class TripleGen extends MultiPartyProtocol {

  protected int numTriplesSecParam;
  protected ShareGen shareGen;
  protected Sampler sampler;

  public TripleGen(Integer myId, List<Integer> partyIds, BigInteger modulus, int kBitLength,
      int lambdaSecurityParam, int numTriplesSecParam, ExtendedNetwork network,
      ExecutorService executor, Random rand) {
    super(myId, partyIds, modulus, kBitLength, network, executor, rand);
    this.numTriplesSecParam = numTriplesSecParam;
    this.shareGen = new ShareGen(modulus, kBitLength, myId, partyIds, lambdaSecurityParam, network,
        rand, executor);
    this.sampler = new DummySampler(rand);
  }

  public List<FieldElement> multiply(List<FieldElement> leftFactorCandidates,
      FieldElement rightFactor) throws IOException {
    List<FieldElement> productCandidates = leftFactorCandidates.stream()
        .map(leftFactor -> leftFactor.multiply(rightFactor)).collect(Collectors.toList());
    // TODO: parallelize
    for (Integer leftParty : partyIds) {
      for (Integer rightParty : partyIds) {
        System.out.println("Running with left " + leftParty + " and right " + rightParty);
        if (!leftParty.equals(rightParty)) {
          if (myId.equals(leftParty)) {
            // act as left
            System.out.println("Will act as left");
            MultiplyLeft multLeft = new MultiplyLeft(leftParty, rightParty, kBitLength, kBitLength,
                numTriplesSecParam, rand, network, executor, modulus);
            List<FieldElement> subCandidates = multLeft.multiply(leftFactorCandidates);
            for (int i = 0; i < numTriplesSecParam; i++) {
              productCandidates.set(i, productCandidates.get(i).add(subCandidates.get(i)));
            }
          } else if (myId.equals(rightParty)) {
            // act as right
            System.out.println("Will act as right");
            MultiplyRight multRight = new MultiplyRight(rightParty, leftParty, kBitLength,
                kBitLength, numTriplesSecParam, rand, network, executor, modulus);
            List<FieldElement> subCandidates = multRight.multiply(rightFactor);
            for (int i = 0; i < numTriplesSecParam; i++) {
              productCandidates.set(i, productCandidates.get(i).add(subCandidates.get(i)));
            }
          }
        }
      }
    }
    return productCandidates;
  }

  public Combined combine(List<FieldElement> leftFactors, FieldElement rightFactor,
      List<FieldElement> products) {
    List<FieldElement> r = sampler.jointSample(modulus, kBitLength, numTriplesSecParam);
    List<FieldElement> rHat = sampler.jointSample(modulus, kBitLength, numTriplesSecParam);
    
    FieldElement a = FieldElement.innerProduct(leftFactors, r);
    FieldElement aHat = FieldElement.innerProduct(leftFactors, rHat);
    FieldElement c = FieldElement.innerProduct(products, r);
    FieldElement cHat = FieldElement.innerProduct(products, rHat);
    
    return new Combined(a, rightFactor, c, aHat, cHat);
  }

  public List<FieldElement> triple() throws IOException {
    List<FieldElement> leftFactorCandidates =
        sampler.sample(modulus, kBitLength, numTriplesSecParam);

    FieldElement rightFactor = sampler.sample(modulus, kBitLength);

    List<FieldElement> productCandidates = multiply(leftFactorCandidates, rightFactor);

    return productCandidates;
  }

  private class Combined {
    FieldElement a;
    FieldElement b;
    FieldElement c;
    FieldElement aHat;
    FieldElement cHat;

    public Combined(FieldElement a, FieldElement b, FieldElement c, FieldElement aHat,
        FieldElement cHat) {
      super();
      this.a = a;
      this.b = b;
      this.c = c;
      this.aHat = aHat;
      this.cHat = cHat;
    }
  }

}
