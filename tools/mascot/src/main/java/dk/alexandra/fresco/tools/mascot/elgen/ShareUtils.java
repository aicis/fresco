package dk.alexandra.fresco.tools.mascot.elgen;

import dk.alexandra.fresco.tools.mascot.arithm.CollectionUtils;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import dk.alexandra.fresco.tools.mascot.utils.FieldElementPrg;
import java.math.BigInteger;
import java.util.List;


public class ShareUtils {

  private FieldElementPrg sampler;

  public ShareUtils(FieldElementPrg sampler) {
    this.sampler = sampler;
  }

  /**
   * Creates additive secret shares of input field element.
   * 
   * @param input field element to secret-share
   * @param numShares number of shares to generate
   * @param modulus field modulus
   * @param bitLength modulus bit length
   * @return secret shares
   */
  public List<FieldElement> additiveShare(FieldElement input, int numShares, BigInteger modulus,
      int bitLength) {
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
