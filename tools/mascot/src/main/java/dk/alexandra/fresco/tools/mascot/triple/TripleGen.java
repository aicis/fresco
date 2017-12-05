package dk.alexandra.fresco.tools.mascot.triple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.tools.mascot.MascotContext;
import dk.alexandra.fresco.tools.mascot.MultiPartyProtocol;
import dk.alexandra.fresco.tools.mascot.arithm.CollectionUtils;
import dk.alexandra.fresco.tools.mascot.elgen.ElGen;
import dk.alexandra.fresco.tools.mascot.field.AuthenticatedElement;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import dk.alexandra.fresco.tools.mascot.field.FieldElementCollectionUtils;
import dk.alexandra.fresco.tools.mascot.field.MultTriple;
import dk.alexandra.fresco.tools.mascot.mult.MultiplyLeft;
import dk.alexandra.fresco.tools.mascot.mult.MultiplyRight;
import dk.alexandra.fresco.tools.mascot.utils.sample.DummyJointSampler;
import dk.alexandra.fresco.tools.mascot.utils.sample.DummySampler;
import dk.alexandra.fresco.tools.mascot.utils.sample.Sampler;

public class TripleGen extends MultiPartyProtocol {

  private ElGen elGen;
  private List<MultiplyRight> rightMultipliers;
  private List<MultiplyLeft> leftMultipliers;
  private Sampler localSampler;
  private Sampler jointSampler;
  private int numLeftFactors;
  // TODO move this into parent class/ interface?
  private boolean initialized;

  public TripleGen(MascotContext ctx, FieldElement macKeyShare, int numLeftFactors) {
    super(ctx);
    this.elGen = new ElGen(ctx, macKeyShare);
    this.leftMultipliers = new LinkedList<>();
    this.rightMultipliers = new LinkedList<>();
    for (Integer partyId : partyIds) {
      if (!myId.equals(partyId)) {
        rightMultipliers.add(new MultiplyRight(ctx, partyId));
        leftMultipliers.add(new MultiplyLeft(ctx, partyId));
      }
    }
    this.localSampler = new DummySampler(ctx.getRand());
    this.jointSampler = new DummyJointSampler();
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

  List<UnauthTriple> toUnauthTriple(List<FieldElement> left, List<FieldElement> right,
      List<FieldElement> prods) {
    Stream<UnauthTriple> stream = IntStream.range(0, right.size())
        .mapToObj(idx -> {
          int groupStart = idx * numLeftFactors;
          int groupEnd = (idx + 1) * numLeftFactors;
          return new UnauthTriple(left.subList(groupStart, groupEnd), right.get(idx),
              prods.subList(groupStart, groupEnd));
        });
    return stream.collect(Collectors.toList());
  }

  List<FieldElement> multiply(List<FieldElement> leftFactorGroups,
      List<FieldElement> rightFactors) {
    // "stretch" right factors, so we have one right factor for each left factor
    List<FieldElement> stretched =
        FieldElementCollectionUtils.stretch(rightFactors, numLeftFactors);

    // for each value we will have two sub-factors for each other party
    List<List<FieldElement>> subFactors = new ArrayList<>();
    // left-mult blocks on receive, so run right mults first
    for (MultiplyRight rightMultiplier : rightMultipliers) {
      subFactors.add(rightMultiplier.multiply(stretched));
    }
    for (MultiplyLeft leftMultiplier : leftMultipliers) {
      subFactors.add(leftMultiplier.multiply(leftFactorGroups));
    }

    // own part of the product
    List<FieldElement> localSubFactors =
        FieldElementCollectionUtils.pairWiseMultiply(leftFactorGroups, stretched);
    subFactors.add(localSubFactors);

    // combine all sub-factors into product shares
    List<FieldElement> productShares = CollectionUtils.pairWiseSum(subFactors);
    return productShares;
  }

  List<UnauthCand> combine(List<UnauthTriple> triples) {
    int numTriples = triples.size();

    List<List<FieldElement>> masks =
        jointSampler.sample(modulus, modBitLength, numTriples, numLeftFactors);

    List<List<FieldElement>> sacrificeMasks =
        jointSampler.sample(modulus, modBitLength, numTriples, numLeftFactors);

    List<UnauthCand> candidates = IntStream.range(0, numTriples)
        .mapToObj(idx -> {
          UnauthTriple triple = triples.get(idx);
          List<FieldElement> m = masks.get(idx);
          List<FieldElement> ms = sacrificeMasks.get(idx);
          return triple.toCandidate(m, ms);
        })
        .collect(Collectors.toList());

    return candidates;
  }

  List<AuthCand> toAuthenticatedCand(List<AuthenticatedElement> list, int partSize) {
    if (list.size() % partSize != 0) {
      throw new IllegalArgumentException("Size of list must be multiple of partition size");
    }
    int numParts = list.size() / partSize;
    return IntStream.range(0, numParts)
        .mapToObj(idx -> {
          List<AuthenticatedElement> batch = list.subList(idx * partSize, (idx + 1) * partSize);
          return new AuthCand(batch);
        })
        .collect(Collectors.toList());
  }

  List<AuthCand> authenticate(List<UnauthCand> candidates) {
    List<FieldElement> flatInputs = candidates.stream()
        .flatMap(c -> c.stream())
        .collect(Collectors.toList());

    List<List<AuthenticatedElement>> shares = new ArrayList<>();
    for (Integer partyId : partyIds) {
      if (myId.equals(partyId)) {
        shares.add(elGen.input(flatInputs));
      } else {
        shares.add(elGen.input(partyId, flatInputs.size()));
      }
    }

    List<AuthenticatedElement> combined = CollectionUtils.pairWiseSum(shares);
    return toAuthenticatedCand(combined, 5);
  }

  List<AuthenticatedElement> computeRhos(List<AuthCand> candidates, List<FieldElement> masks) {
    List<AuthenticatedElement> rhos = IntStream.range(0, candidates.size())
        .mapToObj(idx -> {
          AuthCand cand = candidates.get(idx);
          FieldElement mask = masks.get(idx);
          return cand.computeRho(mask);
        })
        .collect(Collectors.toList());
    return rhos;
  }

  List<AuthenticatedElement> computeSigmas(List<AuthCand> candidates, List<FieldElement> masks,
      List<FieldElement> openRhos) {
    List<AuthenticatedElement> sigmas = IntStream.range(0, candidates.size())
        .mapToObj(idx -> {
          AuthCand cand = candidates.get(idx);
          FieldElement mask = masks.get(idx);
          FieldElement openRho = openRhos.get(idx);
          return cand.computeSigma(openRho, mask);
        })
        .collect(Collectors.toList());
    return sigmas;
  }

  List<MultTriple> toMultTriples(List<AuthCand> candidates) {
    return candidates.stream()
        .map(cand -> cand.toTriple())
        .collect(Collectors.toList());
  }

  List<MultTriple> sacrifice(List<AuthCand> candidates) {
    List<FieldElement> masks = jointSampler.sample(modulus, modBitLength, candidates.size());

    // compute masked values we will open and use in mac-check
    List<AuthenticatedElement> rhos = computeRhos(candidates, masks);

    // open masked values
    List<FieldElement> openRhos = elGen.open(rhos);

    // compute macs
    List<AuthenticatedElement> sigmas = computeSigmas(candidates, masks, openRhos);

    // put rhos and sigmas together
    rhos.addAll(sigmas);

    // pad open rhos with zeroes, one for each sigma
    FieldElementCollectionUtils.padWith(openRhos, new FieldElement(0, modulus, modBitLength),
        sigmas.size());

    // run mac-check
    try {
      // TODO check if we can avoid re-masking
      elGen.check(rhos, openRhos);
    } catch (MPCException e) {
      // TODO handle
      System.out.println("here");
      e.printStackTrace();
      throw e;
    }
    return toMultTriples(candidates);
  }

  public List<MultTriple> triple(int numTriples) {
    // can't generate triples before initializing
    if (!initialized) {
      throw new IllegalStateException("Need to initialize first");
    }

    // generate random left factor groups
    List<FieldElement> leftFactorGroups =
        localSampler.sample(modulus, modBitLength, numTriples * numLeftFactors);

    // generate random right factors
    List<FieldElement> rightFactors = localSampler.sample(modulus, modBitLength, numTriples);

    // compute product groups
    List<FieldElement> productGroups = multiply(leftFactorGroups, rightFactors);

    // combine into unauthenticated triples
    List<UnauthTriple> unauthTriples =
        toUnauthTriple(leftFactorGroups, rightFactors, productGroups);

    // combine unauthenticated triples into unauthenticated triple candidates
    List<UnauthCand> candidates = combine(unauthTriples);

    // use el-gen to input candidates and combine them to the authenticated candidates
    List<AuthCand> authenticated = authenticate(candidates);

    // for each candidate, run sacrifice and get valid triple
    List<MultTriple> triples = sacrifice(authenticated);

    // return valid triples
    return triples;
  }

  private class UnauthTriple {
    List<FieldElement> leftFactors;
    FieldElement rightFactor;
    List<FieldElement> product;

    public UnauthTriple(List<FieldElement> leftFactors, FieldElement rightFactor,
        List<FieldElement> product) {
      super();
      this.leftFactors = leftFactors;
      this.rightFactor = rightFactor;
      this.product = product;
    }

    UnauthCand toCandidate(List<FieldElement> masks, List<FieldElement> sacrificeMasks) {
      FieldElement left = FieldElementCollectionUtils.innerProduct(leftFactors, masks);
      FieldElement prod = FieldElementCollectionUtils.innerProduct(product, masks);
      FieldElement leftSac = FieldElementCollectionUtils.innerProduct(leftFactors, sacrificeMasks);
      FieldElement prodSac = FieldElementCollectionUtils.innerProduct(product, sacrificeMasks);
      return new UnauthCand(left, rightFactor, prod, leftSac, prodSac);
    }
  }

  private class TripleCandidate<T> {

    T a;
    T b;
    T c;
    T aHat;
    T cHat;

    TripleCandidate(T a, T b, T c, T aHat, T cHat) {
      this.a = a;
      this.b = b;
      this.c = c;
      this.aHat = aHat;
      this.cHat = cHat;
    }

    TripleCandidate(List<T> ordered) {
      this(ordered.get(0), ordered.get(1), ordered.get(2), ordered.get(3), ordered.get(4));
    }

    Stream<T> stream() {
      return Arrays.asList(a, b, c, aHat, cHat)
          .stream();
    }

    @Override
    public String toString() {
      return "TripleCandidate [a=" + a + ", b=" + b + ", c=" + c + ", aHat=" + aHat + ", cHat="
          + cHat + "]";
    }

  }

  private class UnauthCand extends TripleCandidate<FieldElement> {

    UnauthCand(FieldElement a, FieldElement b, FieldElement c, FieldElement aHat,
        FieldElement cHat) {
      super(a, b, c, aHat, cHat);
    }

  }

  private class AuthCand extends TripleCandidate<AuthenticatedElement> {

    AuthCand(AuthenticatedElement a, AuthenticatedElement b, AuthenticatedElement c,
        AuthenticatedElement aHat, AuthenticatedElement cHat) {
      super(a, b, c, aHat, cHat);
    }

    public AuthCand(List<AuthenticatedElement> ordered) {
      super(ordered);
    }

    public AuthenticatedElement computeRho(FieldElement mask) {
      return a.multiply(mask)
          .subtract(aHat);
    }

    public AuthenticatedElement computeSigma(FieldElement openRho, FieldElement mask) {
      return c.multiply(mask)
          .subtract(cHat)
          .subtract(b.multiply(openRho));
    }

    public MultTriple toTriple() {
      return new MultTriple(a, b, c);
    }

  }
}
