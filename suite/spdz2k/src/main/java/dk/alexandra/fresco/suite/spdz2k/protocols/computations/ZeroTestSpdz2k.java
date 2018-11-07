package dk.alexandra.fresco.suite.spdz2k.protocols.computations;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.Logical;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.OIntArithmetic;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUIntFactory;
import java.util.ArrayList;
import java.util.Collections;
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
    return builder.seq(seq -> seq.advancedNumeric().randomBitMask(k - 1))
        .seq((seq, mask) -> {
          Numeric numeric = seq.numeric();
          PlainT twoTo2k1 = factory.fromOInt(arithmetic.twoTo(k - 1)).negate();
          DRes<SInt> extraBit = numeric.randomBit();
          List<DRes<SInt>> bits = mask.getBits();
          bits.add(extraBit);
          Collections.reverse(bits);
          DRes<SInt> r = numeric.add(
              mask.getValue(),
              numeric.mult(twoTo2k1, extraBit)
          );
          DRes<SInt> c = numeric.add(value, r);
          DRes<OInt> cOpen = numeric.openAsOInt(c);
          Pair<List<DRes<SInt>>, DRes<OInt>> res = new Pair<>(bits, cOpen);
          return () -> res;
        }).seq((seq, pair) -> {
          Logical logical = seq.logical();
          List<DRes<SInt>> rBits = pair.getFirst();
          List<OInt> cBits = arithmetic.toBits(pair.getSecond().out(), k);
          DRes<List<DRes<SInt>>> xored = seq.par(par -> {
            List<DRes<SInt>> xoredInner = new ArrayList<>(cBits.size());
            for (int i = 0; i < cBits.size(); i++) {
              xoredInner.add(par.logical().xorKnown(cBits.get(i), rBits.get(i)));
            }
            return () -> xoredInner;
          });
          DRes<SInt> or = logical.orOfList(xored);
          return logical.not(or);
        });
  }
}
