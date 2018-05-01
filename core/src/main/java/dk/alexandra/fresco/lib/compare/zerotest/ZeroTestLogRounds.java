package dk.alexandra.fresco.lib.compare.zerotest;

import java.util.ArrayList;
import java.util.List;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;

public class ZeroTestLogRounds implements
    Computation<SInt, ProtocolBuilderNumeric> {

  private final int maxLength;
  private final int securityParameter;
  private final DRes<SInt> input;

  public ZeroTestLogRounds(int maxLength, DRes<SInt> input,
      int securityParameter) {
    this.maxLength = maxLength;
    this.securityParameter = securityParameter;
    this.input = input;
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.seq( seq -> seq.advancedNumeric().randomBitMask(maxLength
        + securityParameter)).seq((seq, r) -> {
      // Use the integer interpretation of r to compute c = 2^{k-1}+(input + r)
      DRes<OInt> c = seq.numeric().openAsOInt(seq.numeric().addOpen(seq
          .getOIntArithmetic().twoTo(maxLength - 1), seq.numeric().add(input, r
                      .getValue())));
          return () -> new Pair<>(r.getBits(), c);
    }).seq((seq, pair) -> {
      List<DRes<OInt>> cbits = seq.getOIntArithmetic().toBits(pair.getSecond()
          .out(), maxLength);
      return () -> new Pair<>(pair.getFirst().out(), cbits);
    }).seq((seq, pair) -> {
      List<DRes<SInt>> d = new ArrayList<>(maxLength);
      // DRes<OInt> two = seq.getOIntArithmetic().twoTo(1);
      for (int i = 0; i < maxLength; i++) {
        DRes<SInt> ri = pair.getFirst().get(i);
        DRes<OInt> ci = pair.getSecond().get(i);
        DRes<SInt> di = seq.logical().xorKnown(ci, ri);
        // DRes<SInt> producti = seq.numeric().multByOpen(two, seq.numeric()
        // .multByOpen(ci, ri));
        // DRes<SInt> sumi = seq.numeric().addOpen(ci, ri);
        // DRes<SInt> di = seq.numeric().sub(sumi, producti);
        d.add(di);
      }
      return () -> d;// new Pair<>(tempList1, tempList2);
      // }).par((par, pair) -> {
//      List<DRes<SInt>> d = new ArrayList<>(maxLength);
//      for (int i = 0; i < maxLength; i++) {
//        DRes<SInt> di = par.numeric().sub(pair.getSecond().get(i), pair
//            .getFirst().get(i));
//        d.add(di);
//      }
//      return () -> d;
    }).seq((seq, d) -> {
      return seq.numeric().subFromOpen(seq.getOIntArithmetic().twoTo(0), seq
          .logical().orOfList(() -> d));
      });
  }
}
