package dk.alexandra.fresco.lib.common.compare.zerotest;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.MathUtils;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.logical.Logical;
import dk.alexandra.fresco.lib.common.math.AdvancedNumeric;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

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
    return builder.seq(seq -> AdvancedNumeric.using(seq).additiveMask(maxBitlength
        + statisticalSecurity)).seq((seq, r) -> {
      // Use the integer interpretation of r to compute c = 2^maxLength+(input + r)
      DRes<BigInteger> c = seq.numeric().open(seq.numeric().add(BigInteger.ONE.shiftLeft(maxBitlength), seq.numeric().add(
          input, r.random)));
      return Pair.lazy(r.bits, c);
    }).seq((seq, pair) -> {
      List<BigInteger> cbits = MathUtils.toBits(pair.getSecond().out(), maxBitlength);
      // Reverse the bits of c as they are stored in big endian whereas the
      // composed r values from random bit mask will be in little endian as
      // it is based on a list of bits
      Collections.reverse(cbits);
      List<DRes<SInt>> secretBits = pair.getFirst().subList(0, maxBitlength);
      DRes<List<DRes<SInt>>> d = seq
          .par(par -> Logical.using(par).pairWiseXorKnown(cbits, () -> secretBits));
      // return 1 - OR-list(d)
      return seq.numeric().sub(BigInteger.ONE, Logical.using(seq).orOfList(d));
    });
  }
}