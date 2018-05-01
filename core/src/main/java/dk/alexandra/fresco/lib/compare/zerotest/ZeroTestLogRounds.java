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
    return builder.seq(seq -> seq.advancedNumeric().randomBitMask(maxLength
        + securityParameter)).seq((seq, r) -> {
      // Use the integer interpretation of r to compute c = 2^{k-1}+(input + r)
      DRes<OInt> c = seq.numeric().openAsOInt(seq.numeric().addOpen(seq
          .getOIntArithmetic().twoTo(maxLength - 1), seq.numeric().add(input, r
          .getValue())));
      seq.debug().openAndPrint("r ", r.getValue(), System.out);
      return () -> new Pair<>(r.getBits(), c);
    }).seq((seq, pair) -> {
      System.out.println("c " + pair.getSecond().out());
      List<DRes<OInt>> cbits = seq.getOIntArithmetic().toBits(pair.getSecond()
          .out(), maxLength);
      return () -> new Pair<>(pair.getFirst().out(), cbits);
    }).seq((seq, pair) -> {
      List<DRes<SInt>> d = new ArrayList<>(maxLength);
      DRes<OInt> two = seq.getOIntArithmetic().twoTo(1);
      List<DRes<OInt>> second = pair.getSecond();
      Collections.reverse(second);
      // TODO why -1?
      for (int i = 0; i < maxLength - 1; i++) {
        DRes<SInt> ri = pair.getFirst().get(i);
        DRes<OInt> ci = second.get(i);
        seq.debug().openAndPrint("r" + i + " ci " + ci.out(), ri, System.out);
        DRes<SInt> di = seq.logical().xorKnown(ci, ri);
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
      seq.debug().openAndPrint("label " + seq.getBasicNumericContext().getMyId(), d, System.out);
      return seq.numeric().subFromOpen(seq.getOIntArithmetic().twoTo(0), seq
          .logical().orOfList(() -> d));
    });
  }
}
