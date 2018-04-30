package dk.alexandra.fresco.framework.builder.numeric;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

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
  public DRes<OInt> openAsBit(DRes<SInt> secretBit) {
    return builder.numeric().openAsOInt(secretBit);
  }

  @Override
  public DRes<List<DRes<OInt>>> openAsBits(DRes<List<DRes<SInt>>> secretBits) {
    return builder.par(par -> {
      List<DRes<OInt>> openList =
          secretBits.out().stream().map(closed -> par.logical().openAsBit(closed))
              .collect(Collectors.toList());
      return () -> openList;
    });
  }

  private DRes<List<DRes<SInt>>> pairWise(
      DRes<List<DRes<SInt>>> bitsA,
      DRes<List<DRes<SInt>>> bitsB,
      BiFunction<DRes<SInt>, DRes<SInt>, DRes<SInt>> op) {
    List<DRes<SInt>> leftOut = bitsB.out();
    List<DRes<SInt>> rightOut = bitsA.out();
    List<DRes<SInt>> resultBits = new ArrayList<>(leftOut.size());
    for (int i = 0; i < leftOut.size(); i++) {
      DRes<SInt> leftBit = leftOut.get(i);
      DRes<SInt> rightBit = rightOut.get(i);
      DRes<SInt> resultBit = op.apply(leftBit, rightBit);
      resultBits.add(resultBit);
    }
    return () -> resultBits;
  }

  private DRes<List<DRes<SInt>>> pairWiseKnown(
      DRes<List<DRes<OInt>>> knownBits,
      DRes<List<DRes<SInt>>> secretBits,
      BiFunction<DRes<OInt>, DRes<SInt>, DRes<SInt>> op) {
    List<DRes<OInt>> knownOut = knownBits.out();
    List<DRes<SInt>> secretOut = secretBits.out();
    List<DRes<SInt>> resultBits = new ArrayList<>(secretOut.size());
    for (int i = 0; i < secretOut.size(); i++) {
      DRes<SInt> secretBit = secretOut.get(i);
      DRes<OInt> knownBit = knownOut.get(i);
      DRes<SInt> resultBit = op.apply(knownBit, secretBit);
      resultBits.add(resultBit);
    }
    return () -> resultBits;
  }

  @Override
  public DRes<List<DRes<SInt>>> pairWiseXorKnown(DRes<List<DRes<OInt>>> knownBits,
      DRes<List<DRes<SInt>>> secretBits) {
    return builder.par(par -> {
      BiFunction<DRes<OInt>, DRes<SInt>, DRes<SInt>> f = (left, right) -> par.logical()
          .xorKnown(left, right);
      return pairWiseKnown(knownBits, secretBits, f);
    });
  }

  @Override
  public DRes<List<DRes<SInt>>> pairWiseAndKnown(DRes<List<DRes<OInt>>> knownBits,
      DRes<List<DRes<SInt>>> secretBits) {
    return builder.par(par -> {
      BiFunction<DRes<OInt>, DRes<SInt>, DRes<SInt>> f = (left, right) -> par.logical()
          .andKnown(left, right);
      return pairWiseKnown(knownBits, secretBits, f);
    });
  }

  @Override
  public DRes<List<DRes<SInt>>> pairWiseAnd(DRes<List<DRes<SInt>>> bitsA,
      DRes<List<DRes<SInt>>> bitsB) {
    return builder.par(par -> {
      BiFunction<DRes<SInt>, DRes<SInt>, DRes<SInt>> f = (left, right) -> par.logical()
          .and(left, right);
      return pairWise(bitsA, bitsB, f);
    });
  }

  @Override
  public DRes<List<DRes<SInt>>> pairWiseOr(DRes<List<DRes<SInt>>> bitsA,
      DRes<List<DRes<SInt>>> bitsB) {
    return builder.par(par -> {
      BiFunction<DRes<SInt>, DRes<SInt>, DRes<SInt>> f = (left, right) -> par.logical()
          .or(left, right);
      return pairWise(bitsA, bitsB, f);
    });
  }

  @Override
  public DRes<SInt> orOfList(DRes<List<DRes<SInt>>> bits) {
    return builder.seq(seq -> bits).whileLoop((inputs) -> inputs
        .size() > 1,
        (prevSeq, inputs) -> prevSeq.par(par -> {
          List<DRes<SInt>> out = new ArrayList<>();
          DRes<SInt> left = null;
          for (DRes<SInt> currentInput : inputs) {
            if (left == null) {
              left = currentInput;
            } else {
              out.add(par.logical().or(left, currentInput));
              left = null;
            }
          }
          if (left != null) {
            out.add(left);
          }
          return () -> out;
        })).seq((builder, currentInput) -> currentInput.get(0));
  }
}