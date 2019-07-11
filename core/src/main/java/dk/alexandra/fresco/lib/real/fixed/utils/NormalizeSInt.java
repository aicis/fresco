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
 * computes c = 2^{l-n}.
 * 
 * The protocol is simlar to the norm function in MPyC.
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
    return builder.seq(seq -> {
      return seq.advancedNumeric().toBits(input, l);
    }).seq((seq, bits) -> {

      // Sign bit
      DRes<SInt> b = seq.comparison().compareLEQ(input, seq.numeric().known(0));

      // Signum
      DRes<SInt> s = seq.numeric().add(1, seq.numeric().mult(-2, b));

      DRes<List<DRes<SInt>>> n = new InternalNorm(bits, b).buildComputation(seq);
      return () -> new Pair<>(s, n);
    }).seq((seq, params) -> {
      DRes<SInt> c = seq.numeric().mult(params.getFirst(), params.getSecond().out().get(0));
      return () -> new Pair<>(c, params.getSecond().out().get(2));
    });
  }

  private class InternalNorm implements Computation<List<DRes<SInt>>, ProtocolBuilderNumeric> {

    private List<SInt> bits;
    private DRes<SInt> b;

    public InternalNorm(List<SInt> bits, DRes<SInt> signBit) {
      this.bits = bits;
      this.b = signBit;
    }

    @Override
    public DRes<List<DRes<SInt>>> buildComputation(ProtocolBuilderNumeric builder) {

      return builder.seq(seq -> {
        int n = bits.size();
        if (n == 1) {
          DRes<SInt> signum = seq.numeric().add(1, seq.numeric().mult(-2, b));
          DRes<SInt> t = seq.numeric().add(seq.numeric().mult(signum, bits.get(0)), b);

          DRes<SInt> twominust = seq.numeric().sub(BigInteger.valueOf(2), t);
          DRes<SInt> oneminust = seq.numeric().sub(BigInteger.ONE, t);
          return () -> Arrays.asList(twominust, t, oneminust);
        } else {
          return seq.par(r1 -> {

            DRes<List<DRes<SInt>>> x =
                new InternalNorm(bits.subList(0, n / 2), b).buildComputation(r1);

            DRes<List<DRes<SInt>>> y =
                new InternalNorm(bits.subList(n / 2, n), b).buildComputation(r1);

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
                new ConditionalSelectRow<>(y.get(1), () -> yPrime, () -> xPrime).buildComputation(r2);
            
            return result;
          });
        }
      });

    }

  }

}
