package dk.alexandra.fresco.lib.real.fixed.utils;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;
import java.util.List;

/**
 * If n is the bitlength of the input and l is an upper bound for the bit length, this protocol
 * computes c = 2^{l-n}.
 * 
 * The protocol is simlar to the norm function in MPyC.
 */
public class NormalizeSInt implements Computation<SInt, ProtocolBuilderNumeric> {

  private DRes<SInt> input;
  private int l;

  public NormalizeSInt(DRes<SInt> input, int l) {
    this.input = input;
    this.l = l;
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.seq(seq -> {
      DRes<List<SInt>> bits = seq.advancedNumeric().toBits(input, l);
      return bits;
    }).seq((seq, bits) -> {
      DRes<SInt> s = seq.comparison().sign(input);
      DRes<Pair<DRes<SInt>, DRes<SInt>>> n =
          new InternalNorm(bits, s)
              .buildComputation(seq);
      return () -> new Pair<>(s, n);
    }).seq((seq, params) -> {
      DRes<SInt> c = seq.numeric().mult(params.getFirst(), params.getSecond().out().getFirst());
      return c;
    });
  }
  
  private class InternalNorm
      implements Computation<Pair<DRes<SInt>, DRes<SInt>>, ProtocolBuilderNumeric> {

    private List<SInt> bits;
    private DRes<SInt> signum;

    public InternalNorm(List<SInt> bits, DRes<SInt> signum) {
      this.bits = bits;
      this.signum = signum;
    }

    @Override
    public DRes<Pair<DRes<SInt>, DRes<SInt>>> buildComputation(ProtocolBuilderNumeric builder) {

      if (bits.size() == 1) {
        DRes<SInt> b = builder.numeric().known(BigInteger.ZERO); //TODO
        DRes<SInt> t = builder.numeric().add(builder.numeric().mult(signum, bits.get(0)), b);
        DRes<SInt> tminus2 = builder.numeric().sub(BigInteger.valueOf(2), t);
        return () -> new Pair<>(tminus2, t);
      }

      return builder.par(par -> {
        DRes<Pair<DRes<SInt>, DRes<SInt>>> x =
            new InternalNorm(bits.subList(0, bits.size() / 2), signum).buildComputation(par);
        DRes<Pair<DRes<SInt>, DRes<SInt>>> y =
            new InternalNorm(bits.subList(bits.size() / 2, bits.size()), signum)
                .buildComputation(par);
        
        return () -> new Pair<>(x, y);
      }).seq((seq, p) -> {
        Pair<DRes<SInt>, DRes<SInt>> x = p.getFirst().out();
        Pair<DRes<SInt>, DRes<SInt>> y = p.getSecond().out();
        DRes<SInt> x0 =
            seq.numeric().mult(BigInteger.ONE.shiftLeft((bits.size() + 1) / 2), x.getFirst());
        
        DRes<SInt> z0 = seq.advancedNumeric().condSelect(y.getSecond(), y.getFirst(), x0);
        DRes<SInt> z1 = seq.advancedNumeric().condSelect(y.getSecond(),
            seq.numeric().known(BigInteger.ONE), x.getSecond());
        
        return () -> new Pair<>(z0, z1);
      });
    }

  }

}
