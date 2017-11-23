package dk.alexandra.fresco.tools.mascot.mult;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import dk.alexandra.fresco.framework.util.BitVector;
import dk.alexandra.fresco.tools.mascot.MascotContext;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;

public class MultiplyLeft extends MultiplyShared {

  public MultiplyLeft(MascotContext ctx, Integer otherId, int numLeftFactors) {
    super(ctx, otherId, numLeftFactors);
  }

  protected List<BitVector> generateSeeds(List<FieldElement> leftFactors) {
    // TODO: the ROTs should be batched into one
    List<BitVector> seeds = new ArrayList<>();
    for (FieldElement factor : leftFactors) {
      List<BitVector> temp = rot.receive(factor.toBigInteger(), ctx.getkBitLength());
      seeds.addAll(temp);
    }
    return seeds;
  }

  protected List<FieldElement> receiveDiffs(int numDiffs) {
    // TODO: need batch-receive
    List<FieldElement> diffs = new ArrayList<>();
    for (int d = 0; d < numDiffs; d++) {
      diffs.add(new FieldElement(ctx.getNetwork().receive(otherId), ctx.getModulus(),
          ctx.getkBitLength()));
    }
    return diffs;
  }

  public List<FieldElement> multiply(List<FieldElement> leftFactors) {
    List<BigInteger> seeds = generateSeeds(leftFactors);
    List<FieldElement> seedElements =
        seeds.stream().map(seed -> new FieldElement(seed, ctx.getModulus(), ctx.getkBitLength()))
            .collect(Collectors.toList());
    List<FieldElement> diffs = receiveDiffs(seeds.size());
    // TODO: clean up
    List<FieldElement> productShares = new ArrayList<>();
    int absIdx = 0;
    for (FieldElement leftFactor : leftFactors) {
      List<FieldElement> qValues = new ArrayList<>();
      for (int k = 0; k < ctx.getkBitLength(); k++) {
        FieldElement seedElement = seedElements.get(absIdx);
        boolean bit = leftFactor.getBit(k);
        FieldElement qValue = diffs.get(absIdx).select(bit).add(seedElement);
        qValues.add(qValue);
        absIdx++;
      }
      productShares.add(FieldElement.recombine(qValues, ctx.getModulus(), ctx.getkBitLength()));
    }
    return productShares;
  }

}
