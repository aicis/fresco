package dk.alexandra.fresco.lib.compare.zerotest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;

public class ZeroTestLogRounds implements Computation<SInt, ProtocolBuilderNumeric> {
  // TODO add paper reference
  private final DRes<SInt> input;
  private final int maxBitlength;

  public ZeroTestLogRounds(DRes<SInt> input, int maxBitlength) {
    this.input = input;
    this.maxBitlength = maxBitlength;
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder) {
    final int statisticalSecurity = builder.getBasicNumericContext().getStatisticalSecurityParam();
    return builder.seq(seq -> seq.advancedNumeric().randomBitMask(maxBitlength
        + statisticalSecurity)).seq((seq, r) -> {
          // Use the integer interpretation of r to compute c = 2^maxLength+(input + r)
          DRes<OInt> c = seq.numeric().openAsOInt(seq.numeric().addOpen(seq
              .getOIntArithmetic().twoTo(maxBitlength), seq.numeric().add(
                  input, r.getValue())));
          return () -> new Pair<>(r.getBits(), c);
        }).seq((seq, pair) -> {
          List<DRes<OInt>> cbits = seq.getOIntArithmetic().toBits(pair
              .getSecond().out(), maxBitlength);
          // Reverse the bits of c as they are stored in big endian whereas the
          // composed r values from random bit mask will be in little endian as
          // it is based on a list of bits
          Collections.reverse(cbits);
          return () -> new Pair<>(pair.getFirst().out(), cbits);
        }).par((par, pair) -> {
          List<DRes<SInt>> d = new ArrayList<>(maxBitlength);
          for (int i = 0; i < maxBitlength; i++) {
            DRes<SInt> ri = pair.getFirst().get(i);
            DRes<OInt> ci = pair.getSecond().get(i);
            DRes<SInt> di = par.logical().xorKnown(ci, ri);
            d.add(di);
          }
          return () -> d;
        }).seq((seq, d) -> {
          // return 1 - OR-list(d)
          return seq.numeric().subFromOpen(seq.getOIntArithmetic().one(), seq
              .logical().orOfList(() -> d));
        });
  }
}
