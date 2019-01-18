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
      final Pair<DRes<List<DRes<SInt>>>, DRes<OInt>> bitsAndC = new Pair<>(r.getBits(), c);
      return () -> bitsAndC;
    }).seq((seq, pair) -> {
      List<OInt> cbits = seq.getOIntArithmetic().toBits(pair.getSecond().out(), maxBitlength);
      // Reverse the bits of c as they are stored in big endian whereas the
      // composed r values from random bit mask will be in little endian as
      // it is based on a list of bits
      Collections.reverse(cbits);
      DRes<List<DRes<SInt>>> d = seq
          .par(par -> par.logical().pairWiseXorKnown(() -> cbits, pair.getFirst()));
      // return 1 - OR-list(d)
      return seq.numeric().subFromOpen(seq.getOIntArithmetic().one(), seq
          .logical().orOfList(d));
    });
  }
}
