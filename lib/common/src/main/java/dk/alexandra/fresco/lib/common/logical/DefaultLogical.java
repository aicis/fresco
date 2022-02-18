package dk.alexandra.fresco.lib.common.logical;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * Default implementation of {@link Logical}, expressing logical operations via arithmetic.
 */
public class DefaultLogical implements Logical {

  protected final ProtocolBuilderNumeric builder;

  DefaultLogical(ProtocolBuilderNumeric builder) {
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
  public DRes<SInt> halfOr(DRes<SInt> bitA, DRes<SInt> bitB) {
    return builder.numeric().add(bitA, bitB);
  }

  @Override
  public DRes<SInt> xor(DRes<SInt> bitA, DRes<SInt> bitB) {
    // knownBit + secretBit - 2 * knownBit * secretBit
    return builder.seq(seq -> {
      // mult and add could be in parallel
      DRes<SInt> sum = seq.numeric().add(bitA, bitB);
      DRes<SInt> prod = seq.numeric()
          .mult(BigInteger.valueOf(2), seq.numeric().mult(bitA, bitB));
      return seq.numeric().sub(sum, prod);
    });
  }

  @Override
  public DRes<SInt> andKnown(BigInteger knownBit, DRes<SInt> secretBit) {
    return builder.seq(seq -> seq.numeric().mult(knownBit, secretBit));
  }

  @Override
  public DRes<SInt> xorKnown(BigInteger knownBit, DRes<SInt> secretBit) {
    // knownBit + secretBit - 2 * knownBit * secretBit
    return builder.seq(seq -> {
      // mult and add could be in parallel
      DRes<SInt> sum = seq.numeric().add(knownBit, secretBit);
      DRes<SInt> prod = seq.numeric()
          .mult(BigInteger.valueOf(2), seq.numeric().mult(knownBit, secretBit));
      return seq.numeric().sub(sum, prod);
    });
  }

  @Override
  public DRes<SInt> not(DRes<SInt> secretBit) {
    // 1 - secretBit
    return builder.seq(seq -> seq.numeric().sub(BigInteger.ONE, secretBit));
  }

  @Override
  public DRes<BigInteger> openAsBit(DRes<SInt> secretBit) {
    return builder.numeric().open(secretBit);
  }

  @Override
  public DRes<List<DRes<BigInteger>>> openAsBits(DRes<List<DRes<SInt>>> secretBits) {
    return builder.par(par -> {
      List<DRes<BigInteger>> openList =
          secretBits.out().stream().map(closed -> Logical.using(par).openAsBit(closed))
              .collect(Collectors.toList());
      return DRes.of(openList);
    });
  }

  @Override
  public DRes<List<DRes<SInt>>> batchedNot(DRes<List<DRes<SInt>>> bits) {
    return builder.par(par -> {
      List<DRes<SInt>> negated =
          bits.out().stream().map(closed -> Logical.using(par).not(closed))
              .collect(Collectors.toList());
      return DRes.of(negated);
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
    return DRes.of(resultBits);
  }

  private DRes<List<DRes<SInt>>> pairWiseKnown(
      List<BigInteger> knownOut,
      DRes<List<DRes<SInt>>> secretBits,
      BiFunction<BigInteger, DRes<SInt>, DRes<SInt>> op) {
    List<DRes<SInt>> secretOut = secretBits.out();
    List<DRes<SInt>> resultBits = new ArrayList<>(secretOut.size());
    for (int i = 0; i < secretOut.size(); i++) {
      DRes<SInt> secretBit = secretOut.get(i);
      BigInteger knownBit = knownOut.get(i);
      DRes<SInt> resultBit = op.apply(knownBit, secretBit);
      resultBits.add(resultBit);
    }
    return DRes.of(resultBits);
  }

  @Override
  public DRes<List<DRes<SInt>>> pairWiseXorKnown(List<BigInteger> knownBits,
      DRes<List<DRes<SInt>>> secretBits) {
    return builder.par(par -> {
      BiFunction<BigInteger, DRes<SInt>, DRes<SInt>> f = (left, right) -> Logical.using(par)
          .xorKnown(left, right);
      return pairWiseKnown(knownBits, secretBits, f);
    });
  }

  @Override
  public DRes<List<DRes<SInt>>> pairWiseAndKnown(List<BigInteger> knownBits,
      DRes<List<DRes<SInt>>> secretBits) {
    return builder.par(par -> {
      BiFunction<BigInteger, DRes<SInt>, DRes<SInt>> f = (left, right) -> Logical.using(par)
          .andKnown(left, right);
      return pairWiseKnown(knownBits, secretBits, f);
    });
  }

  @Override
  public DRes<List<DRes<SInt>>> pairWiseAnd(DRes<List<DRes<SInt>>> bitsA,
      DRes<List<DRes<SInt>>> bitsB) {
    return builder.par(par -> {
      BiFunction<DRes<SInt>, DRes<SInt>, DRes<SInt>> f = (left, right) -> Logical.using(par)
          .and(left, right);
      return pairWise(bitsA, bitsB, f);
    });
  }

  @Override
  public DRes<List<DRes<SInt>>> pairWiseOr(DRes<List<DRes<SInt>>> bitsA,
      DRes<List<DRes<SInt>>> bitsB) {
    return builder.par(par -> {
      BiFunction<DRes<SInt>, DRes<SInt>, DRes<SInt>> f = (left, right) -> Logical.using(par)
          .or(left, right);
      return pairWise(bitsA, bitsB, f);
    });
  }

  @Override
  public DRes<List<DRes<SInt>>> pairWiseXor(DRes<List<DRes<SInt>>> bitsA,
      DRes<List<DRes<SInt>>> bitsB) {
    return builder.par(par -> {
      BiFunction<DRes<SInt>, DRes<SInt>, DRes<SInt>> f = (left, right) -> Logical.using(par)
          .xor(left, right);
      return pairWise(bitsA, bitsB, f);
    });
  }

  @Override
  public DRes<SInt> orOfList(DRes<List<DRes<SInt>>> bits) {
    return builder.seq(seq -> bits)
        .whileLoop((inputs) -> inputs.size() > 1,
            (prevSeq, inputs) -> Logical.using(prevSeq).orNeighbors(inputs))
        // end while
        .seq((builder, out) -> out.get(0));
  }

  @Override
  public DRes<List<DRes<SInt>>> orNeighbors(List<DRes<SInt>> bits) {
    return builder.par(par -> {
      List<DRes<SInt>> out = new ArrayList<>();
      DRes<SInt> left = null;
      for (DRes<SInt> currentInput : bits) {
        if (left == null) {
          left = currentInput;
        } else {
          out.add(Logical.using(par).or(left, currentInput));
          left = null;
        }
      }
      if (left != null) {
        out.add(left);
      }
      return DRes.of(out);
    });
  }
}