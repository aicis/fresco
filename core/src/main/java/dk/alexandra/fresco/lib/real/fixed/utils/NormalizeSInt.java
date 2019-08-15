package dk.alexandra.fresco.lib.real.fixed.utils;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.conditional.ConditionalSelectRow;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

/**
 * If n is the bitlength of the input and l is an upper bound for the bit length, this protocol
 * computes <i>c = 2<sup>l-n</sup></i> and returns the pair <i>(c, l-n)</i>.
 */
public class NormalizeSInt
    implements Computation<Pair<DRes<SInt>, DRes<SInt>>, ProtocolBuilderNumeric> {

  private DRes<SInt> input;
  private int l;

  public NormalizeSInt(DRes<SInt> input, int l) {
    this.input = input;
    this.l = l;
  }

  @Override
  public DRes<Pair<DRes<SInt>, DRes<SInt>>> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.par(par -> {
      DRes<List<SInt>> bits = par.advancedNumeric().toBits(input, l);

      // Sign bit (0 or 1)
      DRes<SInt> signBit = par.comparison().compareLEQ(input, par.numeric().known(-1));

      DRes<SInt> isZero =
          par.comparison().compareZero(input, par.getBasicNumericContext().getMaxBitLength());

      return () -> new Pair<>(bits, new Pair<>(signBit, isZero));      
    }).seq((seq, params) -> {
      
      DRes<SInt> isNonZero = seq.numeric().sub(1, params.getSecond().getSecond());
      
      // Sign (-1, 0 or 1)      
      DRes<SInt> sign = seq.numeric().mult(isNonZero,
          seq.numeric().add(1, seq.numeric().mult(-2, params.getSecond().getFirst())));
      
      DRes<List<DRes<SInt>>> norm =
          new InternalNorm(params.getFirst().out(), params.getSecond().getFirst(), sign)
              .buildComputation(seq);
      
      return () -> new Pair<>(sign, norm);
    }).seq((seq, signAndNorm) -> {
      
      DRes<SInt> c =
          seq.numeric().mult(signAndNorm.getFirst(), signAndNorm.getSecond().out().get(0));
      return () -> new Pair<>(c, signAndNorm.getSecond().out().get(2));
    });
  }

  private class InternalNorm implements Computation<List<DRes<SInt>>, ProtocolBuilderNumeric> {

    private List<SInt> bits;
    private DRes<SInt> b;
    private DRes<SInt> s;

    public InternalNorm(List<SInt> bits, DRes<SInt> signBit, DRes<SInt> sign) {
      this.bits = bits;
      this.b = signBit;
      this.s = sign;
    }

    @Override
    public DRes<List<DRes<SInt>>> buildComputation(ProtocolBuilderNumeric builder) {

      return builder.seq(seq -> {
        int n = bits.size();
        if (n == 1) {
          DRes<SInt> t = seq.numeric().add(seq.numeric().mult(s, bits.get(0)), b);
          DRes<SInt> twoMinusT = seq.numeric().sub(BigInteger.valueOf(2), t);
          DRes<SInt> oneMinusT = seq.numeric().sub(BigInteger.ONE, t);
          return () -> Arrays.asList(twoMinusT, t, oneMinusT);
        } else {
          return seq.par(r1 -> {

            DRes<List<DRes<SInt>>> x =
                new InternalNorm(bits.subList(0, n / 2), b, s).buildComputation(r1);

            DRes<List<DRes<SInt>>> y =
                new InternalNorm(bits.subList(n / 2, n), b, s).buildComputation(r1);

            return () -> new Pair<>(x, y);
          }).seq((r2, p) -> {

            List<DRes<SInt>> x = p.getFirst().out();
            List<DRes<SInt>> y = p.getSecond().out();

            DRes<SInt> x0 = r2.numeric().mult(1 << ((n + 1) / 2), x.get(0));
            DRes<SInt> x2 = r2.numeric().add((n + 1) / 2, x.get(2));

            List<DRes<SInt>> yPrime =
                Arrays.asList(y.get(0), r2.numeric().known(BigInteger.ONE), y.get(2));
            List<DRes<SInt>> xPrime = Arrays.asList(x0, x.get(1), x2);

            DRes<List<DRes<SInt>>> result =
                new ConditionalSelectRow<>(y.get(1), () -> yPrime, () -> xPrime)
                    .buildComputation(r2);

            return result;
          });
        }
      });

    }

  }

}
