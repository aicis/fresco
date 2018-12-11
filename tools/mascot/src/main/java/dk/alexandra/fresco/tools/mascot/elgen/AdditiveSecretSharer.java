package dk.alexandra.fresco.tools.mascot.elgen;

import dk.alexandra.fresco.framework.util.SecretSharer;
import dk.alexandra.fresco.tools.mascot.arithm.Addable;
import dk.alexandra.fresco.tools.mascot.field.MascotFieldElement;
import dk.alexandra.fresco.tools.mascot.prg.FieldElementPrg;
import java.math.BigInteger;
import java.util.List;

public class AdditiveSecretSharer implements SecretSharer<MascotFieldElement> {

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
  public List<MascotFieldElement> share(MascotFieldElement input, int numShares) {
    List<MascotFieldElement> shares = sampler.getNext(modulus, numShares - 1);
    MascotFieldElement sumShares = Addable.sum(shares);
    MascotFieldElement diff = input.subtract(sumShares);
    shares.add(diff);
    return shares;
  }

  @Override
  public MascotFieldElement recombine(List<MascotFieldElement> shares) {
    return Addable.sum(shares);
  }

}
