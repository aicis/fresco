package dk.alexandra.fresco.tools.mascot.elgen;

import java.math.BigInteger;
import java.util.List;

import dk.alexandra.fresco.tools.mascot.arithm.CollectionUtils;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import dk.alexandra.fresco.tools.mascot.utils.FieldElementPrg;

public class ShareUtils {

  private FieldElementPrg sampler;

  public ShareUtils(FieldElementPrg sampler) {
    this.sampler = sampler;
  }

  public List<FieldElement> additiveShare(FieldElement input, int numShares, BigInteger modulus, int bitLength) {
    List<FieldElement> shares = sampler.getNext(modulus, bitLength, numShares - 1);
    FieldElement sumShares = CollectionUtils.sum(shares);
    FieldElement diff = input.subtract(sumShares);
    shares.add(diff);
    return shares;
  }

  public FieldElement additiveRecombine(List<FieldElement> shares) {
    return CollectionUtils.sum(shares);
  }
  
}
