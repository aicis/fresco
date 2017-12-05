package dk.alexandra.fresco.tools.mascot.mult;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.mascot.MascotContext;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import dk.alexandra.fresco.tools.mascot.field.FieldElementSerializer;
import dk.alexandra.fresco.tools.mascot.utils.DummyPrg;
import dk.alexandra.fresco.tools.ot.base.FailedOtException;
import dk.alexandra.fresco.tools.ot.base.MaliciousOtException;

public class MultiplyLeft extends MultiplyShared {

  public MultiplyLeft(MascotContext ctx, Integer otherId, int numLeftFactors) {
    super(ctx, otherId, numLeftFactors);
  }

  public MultiplyLeft(MascotContext ctx, Integer otherId) {
    this(ctx, otherId, 1);
  }

  /**
   * Converts field elements to bit vectors and returns concatenation.
   * 
   * @param elements field elements to pack
   * @return
   */
  private StrictBitVector pack(List<FieldElement> elements) {
    StrictBitVector[] bitVecs = elements.stream()
        .map(fe -> fe.toBitVector())
        .toArray(size -> new StrictBitVector[size]);
    return StrictBitVector.concat(true, bitVecs);
  }

  public List<StrictBitVector> generateSeeds(List<FieldElement> leftFactors) {
    StrictBitVector packedFactors = pack(leftFactors);
    // use rot to get choice seeds
    List<StrictBitVector> seeds = new ArrayList<>();
    try {
      seeds = rot.receive(packedFactors);
    } catch (MaliciousOtException | FailedOtException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return seeds;
  }
  
  public List<StrictBitVector> generateSeeds(FieldElement leftFactors) {
    return generateSeeds(Arrays.asList(leftFactors));
  }

  public List<FieldElement> receiveDiffs(int numDiffs) {
    byte[] raw = ctx.getNetwork()
        .receive(otherId);
    List<FieldElement> diffs = FieldElementSerializer.deserializeList(raw);
    return diffs;
  }

  public List<FieldElement> computeProductShares(List<FieldElement> leftFactors,
      List<FieldElement> feSeeds, List<FieldElement> diffs) {
    // we need modulus and bit length
    BigInteger modulus = ctx.getModulus();
    int modBitLength = ctx.getkBitLength();

    List<FieldElement> result = new ArrayList<>(leftFactors.size());

    int diffIdx = 0;

    for (FieldElement leftFactor : leftFactors) {
      List<FieldElement> summands = new ArrayList<>(modBitLength);
      for (int b = 0; b < modBitLength; b++) {
        FieldElement feSeed = feSeeds.get(diffIdx);
        FieldElement diff = diffs.get(diffIdx);
        boolean bit = leftFactor.getBit(b);
        FieldElement summand = diff.select(bit)
            .add(feSeed);
        summands.add(summand);
        diffIdx++;
      }
      FieldElement productShare = FieldElement.recombine(summands, modulus, modBitLength);
      result.add(productShare);
    }

    return result;
  }

  List<FieldElement> seedsToFieldElements(List<StrictBitVector> seeds, BigInteger modulus,
      int modBitLength) {
    // TODO there should be a better way to do this
    return seeds.stream()
        .map(seed -> {
          return new DummyPrg(seed).getNext(modulus, modBitLength);
        })
        .collect(Collectors.toList());
  }

  public List<FieldElement> multiply(List<FieldElement> leftFactors) {
    // we need the modulus and the bit length of the modulus
    BigInteger modulus = ctx.getModulus();
    int modBitLength = ctx.getkBitLength();

    // generate seeds to use for multiplication
    List<StrictBitVector> seeds = generateSeeds(leftFactors);

    // convert each seed to field element
    List<FieldElement> feSeeds = seedsToFieldElements(seeds, modulus, modBitLength);

    // get diffs from other party
    List<FieldElement> diffs = receiveDiffs(seeds.size());

    // compute our shares of the product and return
    return computeProductShares(leftFactors, feSeeds, diffs);
  }

}
