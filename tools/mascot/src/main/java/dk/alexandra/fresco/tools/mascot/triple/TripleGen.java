package dk.alexandra.fresco.tools.mascot.triple;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import dk.alexandra.fresco.tools.mascot.BaseProtocol;
import dk.alexandra.fresco.tools.mascot.FieldElementGroup;
import dk.alexandra.fresco.tools.mascot.MascotContext;
import dk.alexandra.fresco.tools.mascot.elgen.ElGen;
import dk.alexandra.fresco.tools.mascot.field.AuthenticatedElement;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import dk.alexandra.fresco.tools.mascot.mult.MultiplyLeft;
import dk.alexandra.fresco.tools.mascot.mult.MultiplyRight;
import dk.alexandra.fresco.tools.mascot.utils.BatchArithmetic;
import dk.alexandra.fresco.tools.mascot.utils.sample.DummySampler;
import dk.alexandra.fresco.tools.mascot.utils.sample.Sampler;

public class TripleGen extends BaseProtocol {

  private ElGen elGen;
  private List<MultiplyRight> rightMultipliers;
  private List<MultiplyLeft> leftMultipliers;
  // TODO re-use same sampler for triple gen and el gen?
  private Sampler sampler;
  private int numLeftFactors;
  // TODO move this into parent class/ interface?
  private boolean initialized;

  public TripleGen(MascotContext ctx, FieldElement macKeyShare, int numLeftFactors) {
    super(ctx);
    this.elGen = new ElGen(ctx, macKeyShare);
    this.leftMultipliers = new LinkedList<>();
    this.rightMultipliers = new LinkedList<>();
    Integer myId = ctx.getMyId();
    List<Integer> partyIds = ctx.getPartyIds();
    for (Integer partyId : partyIds) {
      if (!myId.equals(partyId)) {
        rightMultipliers.add(new MultiplyRight(ctx, partyId, numLeftFactors));
        leftMultipliers.add(new MultiplyLeft(ctx, partyId, numLeftFactors));
      }
    }
    this.sampler = new DummySampler(ctx.getRand());
    this.numLeftFactors = numLeftFactors;
    this.initialized = false;
  }

  public void initialize() {
    // shouldn't initialize again
    if (initialized) {
      throw new IllegalStateException("Already initialized");
    }
    // initialize el gen
    elGen.initialize();
    this.initialized = true;
  }

  // probably overdid it with streams here...
  

  List<List<FieldElement>> multiply(List<List<FieldElement>> leftFactorGroups,
      List<FieldElement> rightFactors) {
    // TODO should parallelize
    // TODO make factor group a class
    List<List<List<FieldElement>>> subFactors = new ArrayList<>();
    // left-mult blocks on receive, so run right mults first
    for (MultiplyRight rightMultiplier : rightMultipliers) {
      subFactors.add(rightMultiplier.multiply(rightFactors));
    }
    for (MultiplyLeft leftMultiplier : leftMultipliers) {
      subFactors.add(leftMultiplier.multiply(leftFactorGroups));
    }
    List<List<FieldElement>> localSubFactors = BatchArithmetic.pairWiseMultiply(leftFactorGroups, rightFactors);
    subFactors.add(localSubFactors);
    List<List<FieldElement>> productShares = BatchArithmetic.pairWiseAdd(subFactors);
    return productShares;
  }

  TripleCandidate singleCombine(List<FieldElement> leftFactorGroup, FieldElement rightFactor,
      List<FieldElement> productGroup, List<FieldElement> masks, List<FieldElement> masksSac) {
    FieldElement left = FieldElement.innerProduct(leftFactorGroup, masks);
    FieldElement prod = FieldElement.innerProduct(productGroup, masks);
    FieldElement leftSac = FieldElement.innerProduct(leftFactorGroup, masksSac);
    FieldElement prodSac = FieldElement.innerProduct(productGroup, masksSac);
    return new TripleCandidate(left, rightFactor, prod, leftSac, prodSac);
  }

  List<TripleCandidate> combine(List<List<FieldElement>> leftFactorGroups,
      List<FieldElement> rightFactors, List<List<FieldElement>> productGroups) {
    BigInteger modulus = ctx.getModulus();
    int modBitLength = ctx.getkBitLength();
    int numGroups = productGroups.size();

    List<List<FieldElement>> masks =
        sampler.jointSampleGroups(modulus, modBitLength, numGroups, numLeftFactors);
    List<List<FieldElement>> sacrificeMasks =
        sampler.jointSampleGroups(modulus, modBitLength, numGroups, numLeftFactors);

    List<TripleCandidate> candidates = IntStream.range(0, productGroups.size())
        .mapToObj(idx -> {
          List<FieldElement> lfg = leftFactorGroups.get(idx);
          FieldElement r = rightFactors.get(idx);
          List<FieldElement> pg = productGroups.get(idx);
          List<FieldElement> m = masks.get(idx);
          List<FieldElement> ms = sacrificeMasks.get(idx);
          return singleCombine(lfg, r, pg, m, ms);
        })
        .collect(Collectors.toList());

    return candidates;
  }

  List<TripleCandidate> authenticate(List<TripleCandidate> candidates) {
    List<Integer> partyIds = ctx.getPartyIds();
    Integer myId = ctx.getMyId();
    
    List<FieldElement> flatInputs = candidates.stream()
        .flatMap(c -> c.stream())
        .collect(Collectors.toList());

    List<List<AuthenticatedElement>> allShares = new ArrayList<>();
    List<AuthenticatedElement> myShares = elGen.input(flatInputs);
    allShares.add(myShares);
    for (Integer partyId : partyIds) {
      if (!myId.equals(partyId)) {
        List<AuthenticatedElement> otherShares = elGen.input(partyId, flatInputs.size());
        allShares.add(otherShares);
      }
    }

    return null;
  }

  public void triple(int numTriples) {
    // can't generate triples before initializing
    if (!initialized) {
      throw new IllegalStateException("Need to initialize first");
    }

    BigInteger modulus = ctx.getModulus();
    int modBitLength = ctx.getkBitLength();

    // generate random left factor groups
    List<List<FieldElement>> leftFactorGroups =
        sampler.sampleGroups(modulus, modBitLength, numTriples, numLeftFactors);
    // generate random right factors
    List<FieldElement> rightFactors = sampler.sample(modulus, modBitLength, numTriples);

    // compute product groups
    List<List<FieldElement>> productGroups = multiply(leftFactorGroups, rightFactors);

    // combine into unauthenticated triple candidates
    List<TripleCandidate> candidates = combine(leftFactorGroups, rightFactors, productGroups);
    System.out.println(candidates);
  }

  private class TripleCandidate extends FieldElementGroup {
    /**
     * 
     */
    private static final long serialVersionUID = -4917636316948291312L;

    FieldElement a;
    FieldElement b;
    FieldElement c;
    FieldElement aHat;
    FieldElement cHat;

    TripleCandidate(FieldElement a, FieldElement b, FieldElement c, FieldElement aHat,
        FieldElement cHat) {
      super(Arrays.asList(a, b, c, aHat, cHat));
      this.a = a;
      this.b = b;
      this.c = c;
      this.aHat = aHat;
      this.cHat = cHat;
    }

    @Override
    public String toString() {
      return "Combined [a=" + a + ", b=" + b + ", c=" + c + ", aHat=" + aHat + ", cHat=" + cHat
          + "]";
    }
  }

}
