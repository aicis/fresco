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

    return builder.par(par -> {
      List<DRes<SInt>> results = new ArrayList<>();
      results.add(x);
      for (int i = 1; i < M + 1; i++) {
        DRes<SInt> d = new DivisionProtocol(x, BigInteger.valueOf(2).pow(i)).buildComputation(par);
        results.add(d);
      }
      return DRes.of(results);
    }).par((par, d) -> {
      List<DRes<SInt>> bits = new ArrayList<>();
      for (int i = 1; i < M + 1; i++) {
        int finalI = i;
        DRes<SInt> b = par.seq(seq ->
          seq.numeric().sub(d.get(finalI-1), seq.numeric().mult(2, d.get(finalI))));
        bits.add(b);
      }
      return DRes.of(bits);
    });

  }
}
