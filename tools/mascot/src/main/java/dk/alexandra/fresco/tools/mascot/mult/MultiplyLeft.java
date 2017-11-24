package dk.alexandra.fresco.tools.mascot.mult;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.mascot.MascotContext;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;

public class MultiplyLeft extends MultiplyShared {

  public MultiplyLeft(MascotContext ctx, Integer otherId, int numLeftFactors) {
    super(ctx, otherId, numLeftFactors);
  }

  /**
   * Converts field elements to bit vectors and returns concatenation.
   * 
   * @param elements field elements to pack
   * @return
   */
  private StrictBitVector pack(List<List<FieldElement>> elements) {
    StrictBitVector[] bitVecs = elements.stream()
        .flatMap(fel -> fel.stream())
        .map(fe -> fe.toBitVector())
        .toArray(size -> new StrictBitVector[size]);
    return StrictBitVector.concat(bitVecs);
  }

  List<StrictBitVector> generateSeeds(List<List<FieldElement>> leftFactors) {
    // pack the left factor candidates into bit vector so we can process them in one Rot
    StrictBitVector packedFactors = pack(leftFactors);
    // use rot to get choice seeds
    List<StrictBitVector> seeds = rot.receive(packedFactors);
    return seeds;
  }

  List<FieldElement> receiveDiffs(int numDiffs) {
    // TODO: need batch-receive
    List<FieldElement> diffs = new ArrayList<>();
    for (int d = 0; d < numDiffs; d++) {
      diffs.add(new FieldElement(ctx.getNetwork().receive(otherId), ctx.getModulus(),
          ctx.getkBitLength()));
    }
    return diffs;
  }
  
  List<List<FieldElement>> computeProductShares(List<List<FieldElement>> leftFactors,
      List<FieldElement> feSeeds, List<FieldElement> diffs) {
    // we need modulus and bit length
    BigInteger modulus = ctx.getModulus();
    int modBitLength = ctx.getkBitLength();
    
    List<List<FieldElement>> result = new ArrayList<>(leftFactors.size());
    
    int diffIdx = 0;
    for (List<FieldElement> leftFactorGroup : leftFactors) {
      List<FieldElement> resultGroup = new ArrayList<>(leftFactorGroup.size());
      for (FieldElement leftFactor : leftFactorGroup) {
        List<FieldElement> summands = new ArrayList<>(modBitLength);
        for (int b = 0; b < modBitLength; b++) {
          FieldElement feSeed = feSeeds.get(diffIdx);
          FieldElement diff = diffs.get(diffIdx);
          boolean bit = leftFactor.getBit(b);
          FieldElement summand = diff.select(bit).add(feSeed);
          summands.add(summand);
          diffIdx++;
        }
        FieldElement productShare = FieldElement.recombine(summands, modulus, modBitLength);
        resultGroup.add(productShare);
      }
      result.add(resultGroup);
    }

    return result;
  }

  // should be list of lists
  public List<List<FieldElement>> multiply(List<List<FieldElement>> leftFactors) {
    // we need the modulus and the bit length of the modulus
    BigInteger modulus = ctx.getModulus();
    int modBitLength = ctx.getkBitLength();

    // generate seeds to use for multiplication
    List<StrictBitVector> seeds = generateSeeds(leftFactors);
    
    // convert each seed to field element
    List<FieldElement> feSeeds = seeds.stream()
        .map(seed -> new FieldElement(seed.toByteArray(), modulus, modBitLength))
        .collect(Collectors.toList());

    // get diffs from other party
    List<FieldElement> diffs = receiveDiffs(seeds.size());
    
    // compute our shares of the product
    List<List<FieldElement>> productShares = computeProductShares(leftFactors, feSeeds, diffs);    
    return productShares;
  }

}
