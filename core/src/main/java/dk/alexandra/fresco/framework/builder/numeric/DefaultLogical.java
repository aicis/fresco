package dk.alexandra.fresco.framework.builder.numeric;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation of {@link Logical}, expressing logical operations via arithmetic.
 */
public class DefaultLogical implements Logical {

  protected final ProtocolBuilderNumeric builder;

  protected DefaultLogical(ProtocolBuilderNumeric builder) {
    this.builder = builder;
  }

  @Override
  public DRes<SInt> and(DRes<SInt> bitA, DRes<SInt> bitB) {
    return builder.seq(seq -> seq.numeric().mult(bitA, bitB));
  }

  @Override
  public DRes<SInt> or(DRes<SInt> bitA, DRes<SInt> bitB) {
    // bitA + bitB - bitA * bitB
    return builder.seq(seq -> {
      // mult and add could be in parallel
      DRes<SInt> sum = seq.numeric().add(bitA, bitB);
      DRes<SInt> prod = seq.numeric().mult(bitA, bitB);
      return seq.numeric().sub(sum, prod);
    });
  }

  @Override
  public DRes<SInt> andKnown(DRes<OInt> knownBit, DRes<SInt> secretBit) {
    return builder.seq(seq -> seq.numeric().multByOpen(knownBit, secretBit));
  }

  @Override
  public DRes<SInt> xorKnown(DRes<OInt> knownBit, DRes<SInt> secretBit) {
    // knownBit + secretBit - 2 * knownBit * secretBit
    return builder.seq(seq -> {
      // mult and add could be in parallel
      OInt two = seq.getOIntFactory().two();
      DRes<SInt> sum = seq.numeric().addOpen(knownBit, secretBit);
      DRes<SInt> prod = seq.numeric()
          .multByOpen(two, seq.numeric().multByOpen(knownBit, secretBit));
      return seq.numeric().sub(sum, prod);
    });
  }

  @Override
  public DRes<SInt> not(DRes<SInt> secretBit) {
    // 1 - secretBit
    return builder.seq(seq -> {
      OInt one = seq.getOIntFactory().one();
      return seq.numeric().subFromOpen(one, secretBit);
    });
  }

  @Override
  public DRes<List<DRes<SInt>>> pairWiseAndKnown(DRes<List<DRes<OInt>>> knownBits,
      DRes<List<DRes<SInt>>> secretBits) {
    return builder.par(par -> {
      List<DRes<SInt>> leftOut = secretBits.out();
      List<DRes<OInt>> rightOut = knownBits.out();
      List<DRes<SInt>> andedBits = new ArrayList<>(leftOut.size());
      for (int i = 0; i < leftOut.size(); i++) {
        DRes<SInt> leftBit = leftOut.get(i);
        DRes<OInt> rightBit = rightOut.get(i);
        DRes<SInt> andedBit = par.logical().andKnown(rightBit, leftBit);
        andedBits.add(andedBit);
      }
      return () -> andedBits;
    });
  }

  @Override
  public DRes<List<DRes<SInt>>> pairWiseXorKnown(DRes<List<DRes<OInt>>> knownBits,
      DRes<List<DRes<SInt>>> secretBits) {
    return builder.par(par -> {
      List<DRes<SInt>> leftOut = secretBits.out();
      List<DRes<OInt>> rightOut = knownBits.out();
      List<DRes<SInt>> xoredBits = new ArrayList<>(leftOut.size());
      for (int i = 0; i < leftOut.size(); i++) {
        DRes<SInt> leftBit = leftOut.get(i);
        DRes<OInt> rightBit = rightOut.get(i);
        DRes<SInt> andedBit = par.logical().xorKnown(rightBit, leftBit);
        xoredBits.add(andedBit);
      }
      return () -> xoredBits;
    });
  }

  @Override
  public DRes<SInt> orOfKnownList(DRes<List<DRes<OInt>>> bits) {
    // TODO implement
    throw new UnsupportedOperationException();
  }

  @Override
  public DRes<SInt> orOfList(DRes<List<DRes<SInt>>> bits) {
    // int currentSize = bits.out().size();
    // DRes<List<DRes<SInt>>> partialRes = bits;
    // while (currentSize > 1) {
    // partialRes = builder.par(par -> {
    // List<DRes<SInt>> list = new ArrayList<>();
    // for (int i = 0; i + 1 < partialRes.out().size(); i = i + 2) {
    // DRes<SInt> currentOr = or(partialRes.out().get(i), partialRes.out()
    // .get(i + 1));
    // list.add(currentOr);
    // }
    // if (partialRes.out().size() % 2 == 1) {
    // list.add(partialRes.out().get(partialRes.out().size() - 1));
    // }
    // return () -> list;
    // });
    // currentSize = partialRes.out().size();
    // }
    // return partialRes.out().get(0);
    return null;
  }

  private DRes<List<DRes<SInt>>> partialOr(DRes<List<DRes<SInt>>> bits) {
    return builder.par(par -> {
      List<DRes<SInt>> list = new ArrayList<>();
      for (int i = 0; i + 1 < bits.out().size(); i = i + 2) {
        DRes<SInt> currentOr = or(bits.out().get(i), bits.out().get(i + 1));
        list.add(currentOr);
      }
      if (bits.out().size() % 2 == 1) {
        list.add(bits.out().get(bits.out().size() - 1));
      }
      return () -> list;
    });
  }
}
