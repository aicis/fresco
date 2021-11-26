package dk.alexandra.fresco.suite.crt.protocols;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.suite.crt.CRTNumericContext;
import dk.alexandra.fresco.suite.crt.CRTRingDefinition;
import dk.alexandra.fresco.suite.crt.protocols.framework.CRTComputation;
import java.math.BigInteger;

/**
 * Compute the quotient of two secret fixed point numbers.
 */
public class SecretSharedDivisionProtocol extends CRTComputation<SFixed> {

  private final DRes<SFixed> a, b;

  public SecretSharedDivisionProtocol(DRes<SFixed> a, DRes<SFixed> b) {
    this.a = a;
    this.b = b;
  }

  @Override
  public DRes<SFixed> buildComputation(ProtocolBuilderNumeric builder, CRTRingDefinition ring,
      CRTNumericContext context) {
    return builder.seq(seq ->
        // Make sure the SInt parts of a and b are available
        () -> new Pair<>(a.out().getSInt(), b.out().getSInt())
    ).seq((seq, ab) ->
        Pair.lazy(ab,
            new BitLengthProtocol(ab.getSecond(), ring.getP().bitLength() * 2).buildComputation(seq))
    ).pairInPar((par, abAndBitlength) ->
            // Scale divisor to [0.5 , 1]
            new Truncp(par.numeric().mult(abAndBitlength.getFirst().getFirst(),
                abAndBitlength.getSecond().out().getSecond())).buildComputation(par)
        , (par, abAndBitlength) ->
            new Truncp(par.numeric().mult(abAndBitlength.getFirst().getSecond(),
                abAndBitlength.getSecond().out().getSecond())).buildComputation(par)
    ).seq((seq, normalized) -> new State(0, normalized.getFirst(), normalized.getSecond()))
        .whileLoop(state -> state.i < Integer.highestOneBit(ring.getP().bitLength()), (seq, state) -> {
          DRes<SInt> fi = seq.numeric().sub(ring.getP().multiply(BigInteger.valueOf(2)), state.di);
          return seq
              .par(par -> Pair
                  .lazy(par.numeric().mult(fi, state.ni), par.numeric().mult(fi, state.di)))
              .par((par, nd) -> new State(state.i + 1,
                  new Truncp(nd.getFirst()).buildComputation(par),
                  new Truncp(nd.getSecond()).buildComputation(par)));
        }).seq((seq, state) -> new SFixed(state.ni));
  }

  /** State for Goldschmidt iteration in the division protocol */
  private static class State implements DRes<State> {

    private final int i;
    private final DRes<SInt> ni, di;

    State(int i, DRes<SInt> ni, DRes<SInt> di) {
      this.i = i;
      this.ni = ni;
      this.di = di;
    }

    @Override
    public State out() {
      return this;
    }
  }
}
