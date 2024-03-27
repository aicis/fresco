package dk.alexandra.fresco.tools.mascot.elgen;

import dk.alexandra.fresco.framework.builder.numeric.Addable;
import dk.alexandra.fresco.framework.builder.numeric.field.FieldElement;
import dk.alexandra.fresco.framework.util.SecretSharer;
import dk.alexandra.fresco.tools.mascot.prg.FieldElementPrg;
import java.util.List;
import java.util.Objects;

public class AdditiveSecretSharer implements SecretSharer<FieldElement> {

  private final FieldElementPrg sampler;

  /**
   * Creates new {@link AdditiveSecretSharer}.
   *
   * @param sampler source of randomness
   */
  AdditiveSecretSharer(FieldElementPrg sampler) {
    this.sampler = Objects.requireNonNull(sampler);
  }

  @Override
  public List<FieldElement> share(FieldElement input, int numShares) {
    List<FieldElement> shares = sampler.getNext(numShares - 1);
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
