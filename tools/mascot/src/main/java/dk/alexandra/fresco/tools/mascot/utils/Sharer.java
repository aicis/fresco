package dk.alexandra.fresco.tools.mascot.utils;

import java.math.BigInteger;
import java.util.List;
import java.util.Random;

import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import dk.alexandra.fresco.tools.mascot.utils.sample.DummySampler;

public class Sharer {

  private DummySampler sampler;

  public Sharer(Random rand) {
    this.sampler = new DummySampler(rand);
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

  public FieldElement additiveRecombine(List<FieldElement> shares) {
    if (shares.isEmpty()) {
      // TODO: throw
      return null;
    }
    FieldElement first = shares.get(0);
    BigInteger modulus = first.getModulus();
    int bitLength = first.getBitLenght();
    return shares.stream().reduce(new FieldElement(BigInteger.ZERO, modulus, bitLength),
        (left, right) -> (left.add(right)));
  }
}
