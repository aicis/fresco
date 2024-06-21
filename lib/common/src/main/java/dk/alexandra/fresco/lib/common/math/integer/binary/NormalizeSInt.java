package dk.alexandra.fresco.lib.common.math.integer.binary;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.compare.lt.LessThanZero;
import dk.alexandra.fresco.lib.common.math.AdvancedNumeric;
import dk.alexandra.fresco.lib.common.math.integer.conditional.ConditionalSelectRow;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * If n is the bitlength of the input and l is an upper bound for the bit length, this protocol
 * computes <i>c = 2<sup>l-n</sup></i> and returns the pair <i>(c, l-n)</i>. The input has to be
 * non-zero. If the bit length of the input is larger than l, the computation returns (1, 0).
 */
public class NormalizeSInt
    implements Computation<Pair<DRes<SInt>, DRes<SInt>>, ProtocolBuilderNumeric> {

  private final DRes<SInt> input;
  private final int l;

  public NormalizeSInt(DRes<SInt> input, int l) {
    this.input = input;
    this.l = l;
  }

  @Override
  public DRes<Pair<DRes<SInt>, DRes<SInt>>> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.par(par -> {
      AdvancedNumeric advancedNumeric = AdvancedNumeric.using(par);
      DRes<List<DRes<SInt>>> bits = advancedNumeric.toBits(input, l);

      // Sign bit (0 or 1)
      DRes<SInt> signBit =
          new LessThanZero(input, par.getBasicNumericContext().getMaxBitLength())
              .buildComputation(par);

      return Pair.lazy(bits, signBit);
    }).seq((seq, params) -> {
      
      // Sign (-1 or 1)      
      DRes<SInt> sign = seq.numeric().add(1, seq.numeric().mult(-2, params.getSecond()));
      
      DRes<List<DRes<SInt>>> norm =
          new InternalNorm(
              params.getFirst().out().stream().map(DRes::out).collect(Collectors.toList()),
              params.getSecond(), sign).buildComputation(seq);
      
      return () -> new Pair<>(sign, norm);
    }).seq((seq, signAndNorm) -> {
      
      DRes<SInt> c =
          seq.numeric().mult(signAndNorm.getFirst(), signAndNorm.getSecond().out().get(0));
      return () -> new Pair<>(c, signAndNorm.getSecond().out().get(2));
    });
  }

  private static class InternalNorm implements Computation<List<DRes<SInt>>, ProtocolBuilderNumeric> {

    private final List<SInt> bits;
    private final DRes<SInt> b;
    private final DRes<SInt> s;

    private InternalNorm(List<SInt> bits, DRes<SInt> signBit, DRes<SInt> sign) {
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
          return DRes.of(Arrays.asList(twoMinusT, t, oneMinusT));
        } else {
          return seq.par(r1 -> {

            DRes<List<DRes<SInt>>> x =
                r1.seq(new InternalNorm(bits.subList(0, n / 2), b, s));

            DRes<List<DRes<SInt>>> y =
                r1.seq(new InternalNorm(bits.subList(n / 2, n), b, s));

            return Pair.lazy(x, y);
          }).seq((r2, p) -> {

            List<DRes<SInt>> x = p.getFirst().out();
            List<DRes<SInt>> y = p.getSecond().out();

            DRes<SInt> x0 = r2.numeric().mult(BigInteger.ONE.shiftLeft((n + 1) / 2), x.get(0));
            DRes<SInt> x2 = r2.numeric().add((n + 1) / 2, x.get(2));

            List<DRes<SInt>> yPrime =
                Arrays.asList(y.get(0), r2.numeric().known(BigInteger.ONE), y.get(2));
            List<DRes<SInt>> xPrime = Arrays.asList(x0, x.get(1), x2);

            return r2.seq(new ConditionalSelectRow<>(y.get(1), DRes.of(yPrime), DRes.of(xPrime)));
          });
        }
      });

    }

  }

}
