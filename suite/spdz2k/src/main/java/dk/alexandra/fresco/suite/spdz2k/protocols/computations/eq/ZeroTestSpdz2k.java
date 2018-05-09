package dk.alexandra.fresco.suite.spdz2k.protocols.computations.eq;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.OIntArithmetic;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.math.integer.binary.RandomBitMask;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUIntFactory;
import java.util.List;

/**
 * Computes if given value equals 0 (result remains secret).
 */
public class ZeroTestSpdz2k<PlainT extends CompUInt<?, ?, PlainT>> implements
    Computation<SInt, ProtocolBuilderNumeric> {

  private final DRes<SInt> value;
  private final CompUIntFactory<PlainT> factory;
  private final int k;

  public ZeroTestSpdz2k(DRes<SInt> value, CompUIntFactory<PlainT> factory) {
    this.value = value;
    this.factory = factory;
    this.k = factory.getLowBitLength();
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder) {
    OIntArithmetic arithmetic = builder.getOIntArithmetic();
    return builder.seq(seq -> {
      PlainT twoTo2k1 = factory.fromOInt(arithmetic.twoTo(k - 1)).negate();
      DRes<RandomBitMask> mask = seq.advancedNumeric().randomBitMask(k - 2);
      DRes<SInt> r = seq.numeric().multByOpen(twoTo2k1, seq.numeric().randomBit());
      Pair<DRes<RandomBitMask>, DRes<SInt>> pair = new Pair<>(mask, r);
      return () -> pair;
    }).seq((seq, pair) -> {
      RandomBitMask mask = pair.getFirst().out();
      List<DRes<SInt>> bits = mask.getBits().out();
      bits.add(pair.getSecond());
      DRes<SInt> r = seq.numeric().add(mask.getValue(), pair.getSecond());
      DRes<SInt> c = seq.numeric().add(value, r);
      DRes<OInt> cOpen = seq.numeric().openAsOInt(c);
      Pair<List<DRes<SInt>>, DRes<OInt>> res = new Pair<>(bits, cOpen);
      return () -> res;
    }).seq((seq, pair) -> {
      List<DRes<SInt>> rBits = pair.getFirst();
      List<OInt> cBits = arithmetic.toBits(pair.getSecond().out(), k - 1);
      DRes<List<DRes<SInt>>> xored = seq.logical().pairWiseXorKnown(() -> cBits, () -> rBits);
      DRes<SInt> or = seq.logical().orOfList(xored);
      return seq.logical().not(or);
    });
  }
}
