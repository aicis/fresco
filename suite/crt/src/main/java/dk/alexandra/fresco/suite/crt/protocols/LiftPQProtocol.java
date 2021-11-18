package dk.alexandra.fresco.suite.crt.protocols;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.crt.CRTNumericContext;
import dk.alexandra.fresco.suite.crt.CRTRingDefinition;
import dk.alexandra.fresco.suite.crt.datatypes.CRTSInt;
import dk.alexandra.fresco.suite.crt.protocols.Projection.Coordinate;
import dk.alexandra.fresco.suite.crt.protocols.framework.CRTComputation;
import java.math.BigInteger;

/** Given (x,y) output (0, x) */
public class LiftPQProtocol extends
    CRTComputation<SInt> {

  private final DRes<SInt> value;

  public LiftPQProtocol(DRes<SInt> value) {
    this.value = value;
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder, CRTRingDefinition ring,
      CRTNumericContext context) {
    return builder.seq(
        seq -> seq.append(new CorrelatedNoiseProtocol<>())).seq((seq, r) -> {
      DRes<SInt> x1 = ((CRTSInt) value.out()).getLeft();
      DRes<SInt> r1 = ((CRTSInt) r.out()).getRight();
      return Pair.lazy(
          context.mixedAdd(x1, r1).buildComputation(seq), r);
    }).par((seq, xPrimeAndR) -> {

      BigInteger xPrime = xPrimeAndR.getFirst().out();
      BigInteger deltaPrime = xPrime.divide(ring.getP());
      SInt r = xPrimeAndR.getSecond();
      State state = new State().addR(r).addDeltaPrime(deltaPrime);

      DRes<SInt> xp = new Projection(value, Coordinate.LEFT).buildComputation(seq);
      DRes<SInt> rp = new Projection(r, Coordinate.LEFT).buildComputation(seq);

      return Pair.lazy(new Pair<>(xp, rp), state);
    }).seq((seq, xpRpAndState) -> {
      DRes<SInt> xp = xpRpAndState.getFirst().getFirst();
      DRes<SInt> rp = xpRpAndState.getFirst().getSecond();
      DRes<BigInteger> y = seq.numeric().open(seq.numeric().add(xp, rp));
      return Pair.lazy(y, xpRpAndState.getSecond());
    }).seq((seq, yAndState) -> {
      BigInteger yOpen = yAndState.getFirst().out()
          .mod(ring.getP());
      State state = yAndState.getSecond().addY(yOpen);
      return state.addYq(seq.numeric().known(state.y));
    }).par((par, state) -> {
      DRes<SInt> yq = new Projection(state.yq, Coordinate.RIGHT).buildComputation(par);
      DRes<SInt> rq = new Projection(state.r, Coordinate.RIGHT).buildComputation(par);
      return Pair.lazy(new Pair<>(yq, rq), state);
    }).seq((seq, yqRqAndState) -> {
      State state = yqRqAndState.getSecond();
      return seq.numeric().add(state.deltaPrime
              .multiply(ring.getP()),
          seq.numeric()
              .sub(yqRqAndState.getFirst().getFirst(), yqRqAndState.getFirst().getSecond()));
    });
  }

  private static class State implements DRes<State> {

    private BigInteger deltaPrime;
    private DRes<SInt> r;
    private BigInteger y;
    private DRes<SInt> yq;

    public State addY(BigInteger y) {
      this.y = y;
      return this;
    }

    public State addDeltaPrime(BigInteger deltaPrime) {
      this.deltaPrime = deltaPrime;
      return this;
    }

    public State addR(DRes<SInt> r) {
      this.r = r;
      return this;
    }

    @Override
    public State out() {
      return this;
    }

    public State addYq(DRes<SInt> yq) {
      this.yq = yq;
      return this;
    }

  }
}
