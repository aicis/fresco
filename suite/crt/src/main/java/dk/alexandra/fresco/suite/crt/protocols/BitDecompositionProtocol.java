package dk.alexandra.fresco.suite.crt.protocols;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BitDecompositionProtocol implements
    Computation<List<DRes<SInt>>, ProtocolBuilderNumeric> {

  private final int M;
  private final DRes<SInt> x;

  public BitDecompositionProtocol(DRes<SInt> x, int M) {
    this.x = x;
    this.M = M;
  }

  @Override
  public DRes<List<DRes<SInt>>> buildComputation(ProtocolBuilderNumeric builder) {
    if (M == 1) {
      return DRes.of(Collections.singletonList(x));
    }

    int e = M / 2;
    return builder.seq(seq -> {
      DRes<SInt> u = new DivisionProtocol(x, BigInteger.valueOf(2).pow(e)).buildComputation(seq);
      DRes<SInt> l = seq.numeric().sub(x, seq.numeric().mult(BigInteger.valueOf(2).pow(e), u));
      return Pair.lazy(l, u);
    }).pairInPar((seq, lu) ->
            new BitDecompositionProtocol(lu.getFirst(), M - e).buildComputation(seq),
        (seq, lu) -> new BitDecompositionProtocol(lu.getSecond(), e).buildComputation(seq))
        .seq((seq, lu) -> {
          List<DRes<SInt>> bits = new ArrayList<>();
          bits.addAll(lu.getFirst());
          bits.addAll(lu.getSecond());
          return DRes.of(bits);
        });

  }
}
