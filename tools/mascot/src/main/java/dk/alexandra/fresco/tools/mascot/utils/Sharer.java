package dk.alexandra.fresco.tools.mascot.utils;

import java.math.BigInteger;
import java.util.List;
import java.util.Random;

import dk.alexandra.fresco.tools.mascot.field.FieldElement;

public class Sharer {

  private Sampler sampler;

  public Sharer(Random rand) {
    this.sampler = new Sampler(rand);
  }

  public List<FieldElement> additiveShare(FieldElement input, int numShares) {
    BigInteger modulus = input.getModulus();
    int bitLength = input.getBitLenght();
    List<FieldElement> shares = sampler.sample(modulus, bitLength, numShares - 1);
    FieldElement sumShares = shares.stream().reduce(
        new FieldElement(BigInteger.ZERO, modulus, bitLength), (left, right) -> (left.add(right)));
    FieldElement diff = input.subtract(sumShares);
    shares.add(diff);
    return shares;
  }
}
