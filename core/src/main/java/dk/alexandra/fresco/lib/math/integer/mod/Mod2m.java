package dk.alexandra.fresco.lib.math.integer.mod;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.math.integer.binary.RandomBitMask;

/**
 * Computes modular reduction of value mod 2^m.
 */
public class Mod2m implements Computation<SInt, ProtocolBuilderNumeric> {

  private final DRes<SInt> input;
  private final int m;
  private final int k;
  private final int kappa;

  /**
   * Constructs new {@link Mod2m}.
   *
   * @param input value to reduce
   * @param m exponent (2^{m})
   * @param k bitlength of the input
   * @param kappa Computational security parameter
   */
  public Mod2m(DRes<SInt> input, int m, int k, int kappa) {
    this.input = input;
    this.m = m;
    this.k = k;
    this.kappa = kappa;
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder) {
    if (m >= k) {
      return input;
    }
    DRes<RandomBitMask> r = builder.advancedNumeric().randomBitMask(k + kappa);
    // Construct a new RandomBitMask consisting of the first m bits of r
    DRes<RandomBitMask> rPrime = builder.seq(seq -> seq.advancedNumeric()
        .randomBitMask(() -> r.out().getBits().out().subList(0, m)));

    return builder.seq(seq -> {
      // Use the integer interpretation of r to compute c = 2^{k-1}+(input + r)
      DRes<OInt> c = seq.numeric().openAsOInt(seq.numeric().addOpen(seq
          .getOIntArithmetic().twoTo(k - 1), seq.numeric().add(input, r.out()
              .getValue())));
      return c;
    }).seq((seq, c) -> {
      DRes<OInt> cPrime = seq.getOIntArithmetic().modTwoTo(c, m);
      DRes<SInt> u = seq.comparison().compareLTBits(cPrime, rPrime.out().getBits());
      // Return cPrime - 2^m * u
      return seq.numeric().add(seq.numeric().multByOpen(seq.getOIntArithmetic()
          .twoTo(m), u),
          seq.numeric().subFromOpen(cPrime, rPrime.out().getValue()));
    });

  }

}
