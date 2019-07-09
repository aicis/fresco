package dk.alexandra.fresco.lib.real.fixed.utils;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
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

      int n = bits.size();
      if (n == 1) {
        
        DRes<SInt> signum = builder.numeric().add(1, builder.numeric().mult(-2, b));
        DRes<SInt> t = builder.numeric().add(builder.numeric().mult(signum, bits.get(0)), b);
        
        DRes<SInt> twominust = builder.numeric().sub(BigInteger.valueOf(2), t);
        DRes<SInt> oneminust = builder.numeric().sub(BigInteger.ONE, t);
        return () -> Arrays.asList(twominust, t, oneminust);
      }

      return builder.par(par -> {

        DRes<List<DRes<SInt>>> x =
            new InternalNorm(bits.subList(0, n / 2), b).buildComputation(par);

        DRes<List<DRes<SInt>>> y =
            new InternalNorm(bits.subList(n / 2, n), b).buildComputation(par);

        return () -> new Pair<>(x, y);
      }).seq((seq, p) -> {

        List<DRes<SInt>> x = p.getFirst().out();
        List<DRes<SInt>> y = p.getSecond().out();
        
        DRes<SInt> x0 = seq.numeric().mult(1 << ((n + 1) / 2), x.get(0));
        DRes<SInt> x2 = seq.numeric().add((n + 1) / 2, x.get(2));

        // TODO: Parallelize or create cond select for lists
        DRes<SInt> z0 = seq.advancedNumeric().condSelect(y.get(1), y.get(0), x0);
        DRes<SInt> z1 = seq.advancedNumeric().condSelect(y.get(1),
            seq.numeric().known(BigInteger.ONE), x.get(1));
        DRes<SInt> z2 = seq.advancedNumeric().condSelect(y.get(1), y.get(2), x2);

        return () -> Arrays.asList(z0, z1, z2);
      });
    }

  }

}
