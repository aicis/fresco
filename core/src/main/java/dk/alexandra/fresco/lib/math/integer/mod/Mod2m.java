package dk.alexandra.fresco.lib.math.integer.mod;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.compare.gt.BitLessThanOpen;

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
   * @param input
   *          value to reduce
   * @param m
   *          exponent (2^{m})
   * @param k
   *          bitlength of the input
   * @param kappa
   *          Computational security parameter
   */
  public Mod2m(DRes<SInt> input, int m, int k, int kappa) {
    this.input = input;
    this.m = m;
    this.k = k;
    this.kappa = kappa;
  }

  private static DRes<List<DRes<SInt>>> getDeferedList(
      ProtocolBuilderNumeric builder, List<DRes<SInt>> baseList, int amount) {
    BigInteger two = new BigInteger("2");
    return  builder.par(par -> {
      List<DRes<SInt>> list = new ArrayList<>(amount);
      for (int i = 0; i < amount; i++) {
        list.add(par.numeric().mult(two.pow(i), baseList.get(i)));
      }
      return () -> list;
    });
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder) {
    if (m >= k) {
      return input; // TODO is that ok? not semantically correct for negative numbers
    }
    BigInteger two = new BigInteger("2");
    List<DRes<SInt>> randomBits = Stream.generate(() -> builder.numeric()
        .randomBit()).limit(k + kappa).collect(Collectors.toList());
    DRes<List<DRes<SInt>>> rList = getDeferedList(builder, randomBits, k
        + kappa);
    DRes<SInt> r = builder.advancedNumeric().sum(rList);

    DRes<List<DRes<SInt>>> rPrimeList = getDeferedList(builder, randomBits, m);
    DRes<SInt> rPrime = builder.advancedNumeric().sum(rPrimeList);

    // Handle the case that we work with signed integers
    DRes<SInt> temp = builder.numeric().add(input, r);
    // DRes<SInt> temp1 = builder.numeric().add(input, r);
    DRes<BigInteger> c = builder.numeric().open(builder.numeric().add(two.pow(k
        - 1), temp));
    return builder.seq( seq -> {
      BigInteger cPrime = c.out().mod(two.pow(m));
      DRes<SInt> u = seq.seq(new BitLessThanOpen(() -> cPrime, () -> randomBits.subList(0, m)));
      return seq.numeric().add(seq.numeric().mult(two.pow(m), u),
          seq.numeric().sub(cPrime, rPrime));
    });

  }

}
