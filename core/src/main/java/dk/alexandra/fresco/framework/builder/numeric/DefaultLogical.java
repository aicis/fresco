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
  public DRes<SInt> xor(DRes<SInt> bitA, DRes<SInt> bitB) {
    // knownBit + secretBit - 2 * knownBit * secretBit
    return builder.seq(seq -> {
      // mult and add could be in parallel
      OInt two = seq.getOIntFactory().two();
      DRes<SInt> sum = seq.numeric().add(bitA, bitB);
      DRes<SInt> prod = seq.numeric()
          .mult(two, seq.numeric().mult(bitA, bitB));
      return seq.numeric().sub(sum, prod);
    });
  }

  @Override
  public DRes<SInt> xorKnown(OInt knownBit, DRes<SInt> secretBit) {
    // knownBit + secretBit - 2 * knownBit * secretBit
    return builder.seq(seq -> {
      // mult and add could be in parallel
      OInt two = seq.getOIntFactory().two();
      DRes<SInt> sum = seq.numeric().add(knownBit, secretBit);
      DRes<SInt> prod = seq.numeric()
          .mult(two, seq.numeric().mult(knownBit, secretBit));
      return seq.numeric().sub(sum, prod);
    });
  }

  @Override
  public DRes<SInt> not(DRes<SInt> secretBit) {
    // 1 - secretBit
    return builder.seq(seq -> {
      OInt one = seq.getOIntFactory().one();
      return seq.numeric().sub(one, secretBit);
    });
  }





  @Override
  public DRes<SInt> orOfList(DRes<List<DRes<SInt>>> bits) {
    return builder.seq(seq -> bits)
        .whileLoop((inputs) -> inputs.size() > 1,
            (prevSeq, inputs) -> prevSeq.logical().orNeighbors(inputs))
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
          out.add(par.logical().or(left, currentInput));
          left = null;
        }
      }
      if (left != null) {
        out.add(left);
      }
      return () -> out;
    });
  }
}
