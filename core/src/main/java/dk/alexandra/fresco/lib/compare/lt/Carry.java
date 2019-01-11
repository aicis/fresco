package dk.alexandra.fresco.lib.compare.lt;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.ComputationParallel;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.util.SIntPair;
import dk.alexandra.fresco.framework.value.SInt;
import java.util.ArrayList;
import java.util.List;

public class Carry implements Computation<List<SIntPair>, ProtocolBuilderNumeric> {

  private final List<SIntPair> pairs;

  public Carry(List<SIntPair> pairs) {
    this.pairs = pairs;
  }

  @Override
  public DRes<List<SIntPair>> buildComputation(ProtocolBuilderNumeric builder) {
    if (pairs.size() == 1) {
      return () -> pairs;
    }
    final boolean isOdd = pairs.size() % 2 != 0;
    final int lastFullPairIdx = pairs.size() / 2;
    return (builder.par(par -> {
      // TODO pre-initialize to correct size
      List<DRes<SInt>> pFactorsLeft = new ArrayList<>();
      List<DRes<SInt>> pFactorsRight = new ArrayList<>();
      List<DRes<SInt>> qFactorsLeft = new ArrayList<>();
      List<DRes<SInt>> qFactorsRight = new ArrayList<>();

      for (int i = 0; i < lastFullPairIdx; i++) {
        SIntPair left = pairs.get(2 * i + 1);
        SIntPair right = pairs.get(2 * i);
        DRes<SInt> p1 = left.getFirst();
        DRes<SInt> g1 = left.getSecond();
        DRes<SInt> p2 = right.getFirst();

        pFactorsLeft.add(p1);
        pFactorsRight.add(p2);

        qFactorsLeft.add(p2);
        qFactorsRight.add(g1);
      }

      DRes<List<DRes<SInt>>> ps = par.logical().pairWiseAnd(
          () -> pFactorsLeft,
          () -> pFactorsRight
      );
      DRes<List<DRes<SInt>>> qs = par.logical().pairWiseAnd(
          () -> qFactorsLeft,
          () -> qFactorsRight
      );
      Pair<DRes<List<DRes<SInt>>>, DRes<List<DRes<SInt>>>> resPair = new Pair<>(ps, qs);
      return () -> resPair;
    })).par((par, res) -> {
      List<SIntPair> nextRoundInner = new ArrayList<>(pairs.size() / 2);
      List<DRes<SInt>> ps = res.getFirst().out();
      List<DRes<SInt>> qs = res.getSecond().out();
      for (int i = 0; i < lastFullPairIdx; i++) {
        DRes<SInt> oldQ = qs.get(i);
        DRes<SInt> g2 = pairs.get(2 * i)
            .getSecond();
        DRes<SInt> q = par.logical().halfOr(oldQ, g2);
        nextRoundInner.add(new SIntPair(ps.get(i), q));
      }
      if (isOdd) {
        nextRoundInner.add(pairs.get(pairs.size() - 1));
      }
      return () -> nextRoundInner;
    });

  }

}
