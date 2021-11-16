package dk.alexandra.fresco.suite.crt.protocols;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.crt.CRTRingDefinition;
import dk.alexandra.fresco.suite.crt.datatypes.CRTSInt;
import dk.alexandra.fresco.suite.crt.protocols.Projection.Coordinate;
import java.math.BigInteger;

public class LiftPQProtocol implements
    Computation<SInt, ProtocolBuilderNumeric> {

  private final DRes<SInt> value;

  public LiftPQProtocol(DRes<SInt> value) {
    this.value = value;
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.seq(
        seq -> seq.append(new CorrelatedNoiseProtocol<>())).seq((seq, r) -> Pair.lazy(
        new MixedAddProtocol(
            ((CRTSInt) value.out()).getLeft(), ((CRTSInt) r.out()).getRight())
            .buildComputation(seq), r)).par((seq, xPrimeAndR) -> {

      BigInteger p = ((CRTRingDefinition) seq.getBasicNumericContext().getFieldDefinition()).getP();
      BigInteger xPrime = xPrimeAndR.getFirst().out();
      BigInteger deltaPrime = xPrime.divide(p);
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
          .mod(((CRTRingDefinition) seq.getBasicNumericContext().getFieldDefinition()).getP());
      State state = yAndState.getSecond().addY(yOpen);
      return state.addYq(seq.numeric().known(state.y));
    }).par((par, state) -> {
      DRes<SInt> yq = new Projection(state.yq, Coordinate.RIGHT).buildComputation(par);
      DRes<SInt> rq = new Projection(state.r, Coordinate.RIGHT).buildComputation(par);
      return Pair.lazy(new Pair<>(yq, rq), state);
    }).seq((seq, yqRqAndState) -> {
      State state = yqRqAndState.getSecond();
      return seq.numeric().add(state.deltaPrime
              .multiply(((CRTRingDefinition) seq.getBasicNumericContext().getFieldDefinition()).getP()),
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
