package dk.alexandra.fresco.lib.math.integer.mod;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

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

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder) {
    if (m >= k) {
      return input; // TODO is that ok?
    }
    List<DRes<SInt>> randomBits = new ArrayList<DRes<SInt>>(k + kappa);
    IntStream.range(0, k + kappa).forEach(i -> randomBits.add(builder.numeric()
        .randomBit()));
    BigInteger two = new BigInteger("2");
    DRes<List<DRes<SInt>>> rList = builder.par(par -> {
      List<DRes<SInt>> list = new ArrayList<>(k + kappa);
      for (int i = 0; i < k + kappa; i++) {
        list.add(par.numeric().mult(two.pow(i), randomBits.get(i)));
      }
      return () -> list;
    });
    DRes<SInt> r = builder.advancedNumeric().sum(rList);

    DRes<List<DRes<SInt>>> rPrimeList = builder.par(par -> {
      List<DRes<SInt>> list = new ArrayList<>(m);
      for (int i = 0; i < m; i++) {
        list.add(par.numeric().mult(two.pow(i), randomBits.get(i)));
      }
      return () -> list;
    });
    DRes<SInt> rPrime = builder.advancedNumeric().sum(rPrimeList);

    DRes<SInt> temp1 = builder.numeric().add(input, r);
    DRes<BigInteger> c = builder.numeric().open(builder.numeric().add(two.pow(k
        - 1), temp1));
    return builder.seq( seq -> {
      BigInteger cPrime = c.out().mod(two.pow(m));
      DRes<SInt> u = seq.seq(new BitLessThanOpen(() -> cPrime, rPrimeList));
      DRes<SInt> temp2 = seq.numeric().mult(two.pow(m), u);
      DRes<SInt> temp3 = seq.numeric().sub(cPrime, rPrime);
      DRes<SInt> aPrime = seq.numeric().add(temp2, temp3);
      // builder.numeric().known(randomBits.get(i).two.pow(i))
      // randomBits.forEach(r -> {
      // System.out.println(builder.numeric().open(r).out());
      // });
      return aPrime;
    });

  }

}
