package dk.alexandra.fresco.suite.crt.protocols;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.crt.CRTNumericContext;
import dk.alexandra.fresco.suite.crt.CRTRingDefinition;
import dk.alexandra.fresco.suite.crt.Util;
import dk.alexandra.fresco.suite.crt.protocols.Projection.Coordinate;
import dk.alexandra.fresco.suite.crt.protocols.framework.CRTComputation;
import java.math.BigInteger;

/** Given (x, y) output (y, 0) */
public class LiftQPProtocol extends
    CRTComputation<SInt> {

  private final DRes<SInt> value;

  public LiftQPProtocol(DRes<SInt> value) {
    this.value = value;
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder, CRTRingDefinition ring,
      CRTNumericContext context) {
    return builder.seq(
        seq -> seq.append(new CorrelatedNoiseProtocol<>())).seq((seq, r) -> {

      DRes<SInt> x2 = new Projection(value, Coordinate.RIGHT).buildComputation(seq);
      DRes<SInt> rPrime = new Projection(r, Coordinate.RIGHT).buildComputation(seq);
      DRes<BigInteger> yPrime = seq.numeric().open(seq.numeric().add(x2, rPrime));

      return Pair.lazy(yPrime, r);
    }).seq((seq, yPrimeAndR) -> {

      Pair<BigInteger, BigInteger> crt = Util.mapToCRT(yPrimeAndR.getFirst().out(), ring.getP(), ring.getQ());
      BigInteger yPrimeModP = crt.getSecond().mod(ring.getP());
      BigInteger toShare = Util.mapToBigInteger(yPrimeModP, BigInteger.ZERO, ring.getP(), ring.getQ());
      return Pair.lazy(seq.numeric().known(toShare), yPrimeAndR.getSecond());

    }).seq((seq, yPrimeAndR) -> {
      DRes<SInt> r = new Projection(yPrimeAndR.getSecond(), Coordinate.LEFT).buildComputation(seq);
      return seq.numeric().sub(yPrimeAndR.getFirst(), r);
    });
  }

}
