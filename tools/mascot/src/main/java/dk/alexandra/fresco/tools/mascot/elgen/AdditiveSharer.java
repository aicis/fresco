package dk.alexandra.fresco.tools.mascot.elgen;

import dk.alexandra.fresco.tools.mascot.arithm.ArithmeticCollectionUtils;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import dk.alexandra.fresco.tools.mascot.utils.FieldElementPrg;
import java.math.BigInteger;
import java.util.List;

public class AdditiveSharer implements Sharer {

  private final FieldElementPrg sampler;
  private final BigInteger modulus;
  private final int modBitLength;
  private final ArithmeticCollectionUtils<FieldElement> arithmeticUtils;

  /**
   * Creates new {@link AdditiveSharer}.
   * 
   * @param sampler source of randomness
   * @param modulus field modulus
   * @param modBitLength modulus bit length
   */
  public AdditiveSharer(FieldElementPrg sampler, BigInteger modulus, int modBitLength) {
    this.sampler = sampler;
    this.modulus = modulus;
    this.modBitLength = modBitLength;
    this.arithmeticUtils = new ArithmeticCollectionUtils<>();
  }

  @Override
  public List<FieldElement> share(FieldElement input, int numShares) {
    List<FieldElement> shares = sampler.getNext(modulus, modBitLength, numShares - 1);
    FieldElement sumShares = arithmeticUtils.sum(shares);
    FieldElement diff = input.subtract(sumShares);
    shares.add(diff);
    return shares;
  }

  @Override
  public FieldElement recombine(List<FieldElement> shares) {
    return arithmeticUtils.sum(shares);
  }

}
