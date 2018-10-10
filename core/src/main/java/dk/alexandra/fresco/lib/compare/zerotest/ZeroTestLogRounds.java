package dk.alexandra.fresco.lib.compare.zerotest;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;

/**
 * Log round protocol for checking if a secret value equals 0, with secret result. <p>The
 * implementation is based on Protocol ... in ...</p>
 */
public class ZeroTestLogRounds implements Computation<SInt, ProtocolBuilderNumeric> {

  private final DRes<SInt> input;
  private final int maxBitLength;

  /**
   * Constructs new {@link ZeroTestLogRounds}.
   *
   * @param input secret value to compare to 0
   * @param maxBitLength bit length of maximum representable value
   */
  public ZeroTestLogRounds(DRes<SInt> input, int maxBitLength) {
    this.input = input;
    this.maxBitLength = maxBitLength;
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder) {
    final int statisticalSecurity = builder.getBasicNumericContext().getStatisticalSecurityParam();
    return null;
//    return builder.seq(seq -> seq.advancedNumeric().randomBitMask(maxBitLength
//        + statisticalSecurity)).seq((seq, r) -> {
//      // Use the integer interpretation of r to compute c = 2^maxLength+(input + r)
//      DRes<OInt> c = seq.numeric().openAsOInt(seq.numeric().addOpen(seq
//          .getOIntArithmetic().twoTo(maxBitLength), seq.numeric().add(
//          input, r.getValue())));
//      final Pair<DRes<List<DRes<SInt>>>, DRes<OInt>> bitsAndC = new Pair<>(r.getBits(), c);
//      return () -> bitsAndC;
//    }).seq((seq, pair) -> {
//      final List<DRes<SInt>> first = pair.getFirst().out();
//      List<OInt> cbits = seq.getOIntArithmetic().toBits(pair.getSecond().out(), maxBitLength);
//      // Reverse the bits of c as they are stored in big endian whereas the
//      // composed r values from random bit mask will be in little endian as
//      // it is based on a list of bits
//      Collections.reverse(cbits);
//      return () -> new Pair<>(first, cbits);
//    }).par((par, pair) -> {
//      List<DRes<SInt>> d = new ArrayList<>(maxBitLength);
//      for (int i = 0; i < maxBitLength; i++) {
//        DRes<SInt> ri = pair.getFirst().get(i);
//        DRes<OInt> ci = pair.getSecond().get(i);
//        DRes<SInt> di = par.logical().xorKnown(ci, ri);
//        d.add(di);
//      }
//      return () -> d;
//    }).seq((seq, d) -> {
//      // return 1 - OR-list(d)
//      return seq.numeric().subFromOpen(seq.getOIntArithmetic().one(), seq
//          .logical().orOfList(() -> d));
//    });
  }
}
