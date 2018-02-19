package dk.alexandra.fresco.tools.mascot.elgen;

import dk.alexandra.fresco.framework.util.SecretSharer;
import dk.alexandra.fresco.tools.mascot.arithm.Addable;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import dk.alexandra.fresco.tools.mascot.prg.FieldElementPrg;
import java.math.BigInteger;
import java.util.List;

public class AdditiveSecretSharer implements SecretSharer<FieldElement> {

  private final FieldElementPrg sampler;
  private final BigInteger modulus;

  /**
   * Creates new {@link AdditiveSecretSharer}.
   * 
   * @param sampler source of randomness
   * @param modulus field modulus
   */
  AdditiveSecretSharer(FieldElementPrg sampler, BigInteger modulus) {
    this.sampler = sampler;
    this.modulus = modulus;
  }

  @Override
  public List<FieldElement> share(FieldElement input, int numShares) {
    List<FieldElement> shares = sampler.getNext(modulus, numShares - 1);
    FieldElement sumShares = Addable.sum(shares);
    FieldElement diff = input.subtract(sumShares);
    shares.add(diff);
    return shares;
  }

  @Override
  public FieldElement recombine(List<FieldElement> shares) {
    return Addable.sum(shares);
  }

}
