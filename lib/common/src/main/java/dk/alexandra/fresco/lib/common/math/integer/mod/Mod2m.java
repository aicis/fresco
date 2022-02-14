package dk.alexandra.fresco.lib.common.math.integer.mod;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.compare.Comparison;
import dk.alexandra.fresco.lib.common.math.AdvancedNumeric;
import dk.alexandra.fresco.lib.common.math.AdvancedNumeric.RandomAdditiveMask;
import java.math.BigInteger;
import java.util.List;

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
   * @param kappa Statistical security parameter
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
    final DRes<RandomAdditiveMask> r = AdvancedNumeric.using(builder).additiveMask(k + kappa);
    return builder.seq(seq -> {
      // Construct a new RandomBitMask consisting of the first m bits of r
      final List<DRes<SInt>> rPrimeBits = r.out().bits.subList(0, m);
      DRes<SInt> rPrimeValue = AdvancedNumeric.using(seq).bitsToInteger(rPrimeBits);
      RandomAdditiveMask rPrime = new RandomAdditiveMask(rPrimeBits, rPrimeValue);
      // Use the integer interpretation of r to compute c = 2^{k-1}+(input + r)
      DRes<BigInteger> c = seq.numeric().open(seq.numeric().add(BigInteger.ONE.shiftLeft(k - 1),
          seq.numeric().add(input, r.out().value)));
      return Pair.lazy(rPrime, c);
    }).seq((seq, pair) -> {
      RandomAdditiveMask rPrime = pair.getFirst();
      DRes<BigInteger> c = pair.getSecond();
      BigInteger cPrime = c.out().mod(BigInteger.ONE.shiftLeft(m));
      DRes<SInt> u = Comparison.using(seq).compareLTBits(cPrime, DRes.of(rPrime.bits));
      // Return cPrime - 2^m * u
      return seq.numeric().add(seq.numeric().mult(BigInteger.ONE.shiftLeft(m), u),
          seq.numeric().sub(cPrime, rPrime.value));
    });
  }
}