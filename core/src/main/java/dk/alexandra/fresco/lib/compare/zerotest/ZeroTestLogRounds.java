package dk.alexandra.fresco.lib.compare.zerotest;

import java.util.ArrayList;
import java.util.List;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.math.integer.binary.RandomBitMask;

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
    DRes<RandomBitMask> r = builder.advancedNumeric().randomBitMask(maxLength
        + securityParameter);
    return builder.seq(seq -> {
      // Use the integer interpretation of r to compute c = 2^{k-1}+(left + r)
      DRes<OInt> c = seq.numeric().openAsOInt(seq.numeric().addOpen(seq
          .getOIntArithmetic().twoTo(maxLength - 1), seq.numeric().add(input, r
              .out()
              .getValue())));
      return c;
    }).par((par, c) -> {
      List<DRes<SInt>> d = new ArrayList<>(maxLength);
      DRes<OInt> two = par.getOIntArithmetic().twoTo(1);
      List<DRes<OInt>> cbits = par.getOIntArithmetic().toBits(c.out(),
          maxLength);
      for (int i = 0; i < maxLength; i++) {
        DRes<SInt> ri = r.out().getBits().out().get(i);
        // DRes<SInt> temp = par.numeric().multByOpen(two, par.numeric().multByOpen(cbits.get(i), ri));
        // DRes<SInt> temp2 = par.numeric().addOpen(cbits.get(i), ri);
        DRes<SInt> di = par.numeric().sub(par.numeric().addOpen(cbits.get(i),
            ri), par.numeric().multByOpen(two, par.numeric().multByOpen(cbits
                .get(i), ri)));
        d.add(di);
      }
      return par.numeric().subFromOpen(par.getOIntArithmetic().twoTo(0), par
          .logical().orOfList(() -> d));
    });
  }
}
