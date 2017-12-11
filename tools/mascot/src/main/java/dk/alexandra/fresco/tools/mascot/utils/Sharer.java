package dk.alexandra.fresco.tools.mascot.utils;

import java.math.BigInteger;
import java.util.List;

import dk.alexandra.fresco.tools.mascot.field.FieldElement;

public class Sharer {

  private FieldElementPrg sampler;

  public Sharer(FieldElementPrg sampler) {
    this.sampler = sampler;
  }

  public List<FieldElement> additiveShare(FieldElement input, int numShares) {
    BigInteger modulus = input.getModulus();
    int bitLength = input.getBitLength();
    List<FieldElement> shares = sampler.getNext(modulus, bitLength, numShares - 1);
    FieldElement sumShares = shares.stream()
        .reduce(new FieldElement(BigInteger.ZERO, modulus, bitLength),
            (left, right) -> (left.add(right)));
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
    int bitLength = first.getBitLength();
    return shares.stream()
        .reduce(new FieldElement(BigInteger.ZERO, modulus, bitLength),
            (left, right) -> (left.add(right)));
  }
}
