package dk.alexandra.fresco.lib.compare.zerotest;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Log round protocol for checking if a secret value equals 0, with secret result. <p>The
 * implementation is based on Protocol 3.7 in "Improved Primitives for Secure Multiparty Integer
 * Computation" by Catrina and de Hoogh (<a href="https://www1.cs.fau.de/filepool/publications/octavian_securescm/smcint-scn10.pdf">https://www1.cs.fau.de/filepool/publications/octavian_securescm/smcint-scn10.pdf</a>).</p>
 * <p>The algorithm is logarithmic in the number of bits determined by {@code maxBitLength}.</p>
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
    return builder.seq(seq -> seq.advancedNumeric().randomBitMask(maxBitLength
        + statisticalSecurity)).seq((seq, randomMask) -> {
      // Use the integer interpretation of r to compute c = 2^maxLength+(input + r)
      final OInt twoPower = seq.getOIntArithmetic().twoTo(maxBitLength);
      final DRes<SInt> randomValue = randomMask.getValue();
      final DRes<SInt> masked = seq.numeric().add(input, randomValue);
      DRes<OInt> c = seq.numeric().openAsOInt(seq.numeric().add(twoPower, masked));
      final List<DRes<SInt>> bits = randomMask.getBits();
      final Pair<List<DRes<SInt>>, DRes<OInt>> bitsAndC = new Pair<>(bits, c);
      return () -> bitsAndC;
    }).seq((seq, pair) -> {
      final List<DRes<SInt>> first = pair.getFirst();
      List<OInt> cbits = seq.getOIntArithmetic().toBits(pair.getSecond().out(), maxBitLength);
      // Reverse the bits of c as they are stored in big endian whereas the
      // composed r values from random bit mask will be in little endian as
      // it is based on a list of bits
      Collections.reverse(cbits);
      final Pair<List<DRes<SInt>>, List<OInt>> resPair = new Pair<>(first, cbits);
      return () -> resPair;
    }).par((par, pair) -> {
      List<DRes<SInt>> d = new ArrayList<>(maxBitLength);
      for (int i = 0; i < maxBitLength; i++) {
        DRes<SInt> ri = pair.getFirst().get(i);
        OInt ci = pair.getSecond().get(i);
        DRes<SInt> di = par.logical().xorKnown(ci, ri);
        d.add(di);
      }
      return () -> d;
    }).seq((seq, d) -> {
      // return 1 - OR-list(d)
      return seq.numeric().sub(seq.getOIntArithmetic().one(), seq
          .logical().orOfList(() -> d));
    });
  }
}
