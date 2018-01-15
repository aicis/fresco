package dk.alexandra.fresco.tools.mascot.triple;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.tools.mascot.BaseProtocol;
import dk.alexandra.fresco.tools.mascot.MascotResourcePool;
import dk.alexandra.fresco.tools.mascot.arithm.ArithmeticCollectionUtils;
import dk.alexandra.fresco.tools.mascot.elgen.ElementGeneration;
import dk.alexandra.fresco.tools.mascot.field.AuthenticatedElement;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import dk.alexandra.fresco.tools.mascot.field.MultTriple;
import dk.alexandra.fresco.tools.mascot.mult.MultiplyLeft;
import dk.alexandra.fresco.tools.mascot.mult.MultiplyRight;
import dk.alexandra.fresco.tools.mascot.utils.FieldElementPrg;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Actively-secure protocol for computing authenticated, secret-shared multiplication triples based
 * on the MASCOT protocol (https://eprint.iacr.org/2016/505.pdf).<br>
 * In particular, produces random, authenticated, secret-shared triples of the form a, b, c such
 * that a * b = c.
 */
public class TripleGeneration extends BaseProtocol {

  private final ElementGeneration elementGeneration;
  private final Map<Integer, MultiplyRight> rightMultipliers;
  private final Map<Integer, MultiplyLeft> leftMultipliers;
  private final FieldElementPrg jointSampler;

  /**
   * Creates new triple generation protocol.
   */
  public TripleGeneration(MascotResourcePool resourcePool, Network network,
      ElementGeneration elementGeneration, FieldElementPrg jointSampler) {
    super(resourcePool, network);
    this.leftMultipliers = new HashMap<>();
    this.rightMultipliers = new HashMap<>();
    for (Integer partyId : getPartyIds()) {
      if (partyId != getMyId()) {
        if (getMyId() < partyId) {
          rightMultipliers.put(partyId, new MultiplyRight(resourcePool, network,
              partyId));
          leftMultipliers.put(partyId, new MultiplyLeft(resourcePool, network,
              partyId));
        } else {
          leftMultipliers.put(partyId, new MultiplyLeft(resourcePool, network,
              partyId));
          rightMultipliers.put(partyId, new MultiplyRight(resourcePool, network,
              partyId));
        }
      }
    }
    this.elementGeneration = elementGeneration;
    this.jointSampler = jointSampler;
  }

  TripleGeneration(MascotResourcePool resourcePool, Network network, FieldElementPrg jointSampler,
      FieldElement macKeyShare) {
    this(resourcePool, network,
        new ElementGeneration(resourcePool, network, macKeyShare, jointSampler), jointSampler);
  }

  List<UnauthTriple> toUnauthTriple(List<FieldElement> left, List<FieldElement> right,
      List<FieldElement> prods) {
    Stream<UnauthTriple> stream = IntStream.range(0, right.size()).mapToObj(idx -> {
      int groupStart = idx * getNumCandidatesPerTriple();
      int groupEnd = (idx + 1) * getNumCandidatesPerTriple();
      return new UnauthTriple(left.subList(groupStart, groupEnd), right.get(idx),
          prods.subList(groupStart, groupEnd));
    });
    return stream.collect(Collectors.toList());
  }

  List<FieldElement> multiply(List<FieldElement> leftFactorGroups,
      List<FieldElement> rightFactors) {
    // "stretch" right factors, so we have one right factor for each left factor
    List<FieldElement> stretched =
        getFieldElementUtils().stretch(rightFactors, getNumCandidatesPerTriple());

    // for each value we will have two sub-factors for each other party
    List<List<FieldElement>> subFactors = new ArrayList<>();

    for (Integer partyId : getPartyIds()) {
      if (!partyId.equals(getMyId())) {
        MultiplyLeft leftMult = leftMultipliers.get(partyId);
        MultiplyRight rightMult = rightMultipliers.get(partyId);
        if (getMyId() < partyId) {
          subFactors.add(rightMult.multiply(stretched));
          subFactors.add(leftMult.multiply(leftFactorGroups));
        } else {
          subFactors.add(leftMult.multiply(leftFactorGroups));
          subFactors.add(rightMult.multiply(stretched));
        }
      }
    }

    // own part of the product
    List<FieldElement> localSubFactors =
        getFieldElementUtils().pairWiseMultiply(leftFactorGroups, stretched);
    subFactors.add(localSubFactors);

    // combine all sub-factors into product shares
    List<FieldElement> productShares = getFieldElementUtils().pairwiseSum(subFactors);
    return productShares;
  }

  List<UnauthCand> combine(List<UnauthTriple> triples) {
    int numTriples = triples.size();

    List<List<FieldElement>> masks = jointSampler.getNext(getModulus(), getModBitLength(),
        numTriples, getNumCandidatesPerTriple());

    List<List<FieldElement>> sacrificeMasks = jointSampler.getNext(getModulus(), getModBitLength(),
        numTriples, getNumCandidatesPerTriple());

    List<UnauthCand> candidates = IntStream.range(0, numTriples).mapToObj(idx -> {
      UnauthTriple triple = triples.get(idx);
      List<FieldElement> m = masks.get(idx);
      List<FieldElement> ms = sacrificeMasks.get(idx);
      return triple.toCandidate(m, ms);
    }).collect(Collectors.toList());

    return candidates;
  }

  List<AuthCand> toAuthenticatedCand(List<AuthenticatedElement> list, int partSize) {
    int numParts = list.size() / partSize;
    return IntStream.range(0, numParts).mapToObj(idx -> {
      List<AuthenticatedElement> batch = list.subList(idx * partSize, (idx + 1) * partSize);
      return new AuthCand(batch);
    }).collect(Collectors.toList());
  }

  List<AuthCand> authenticate(List<UnauthCand> candidates) {
    List<FieldElement> flatInputs =
        candidates.stream().flatMap(TripleCandidate::stream).collect(Collectors.toList());

    List<List<AuthenticatedElement>> shares = new ArrayList<>();
    for (Integer partyId : getPartyIds()) {
      if (partyId.equals(getMyId())) {
        shares.add(elementGeneration.input(flatInputs));
      } else {
        shares.add(elementGeneration.input(partyId, flatInputs.size()));
      }
    }

    List<AuthenticatedElement> combined =
        new ArithmeticCollectionUtils<AuthenticatedElement>().pairwiseSum(shares);
    return toAuthenticatedCand(combined, 5);
  }

  List<AuthenticatedElement> computeRhos(List<AuthCand> candidates, List<FieldElement> masks) {
    List<AuthenticatedElement> rhos = IntStream.range(0, candidates.size()).mapToObj(idx -> {
      AuthCand cand = candidates.get(idx);
      FieldElement mask = masks.get(idx);
      return cand.computeRho(mask);
    }).collect(Collectors.toList());
    return rhos;
  }

  List<AuthenticatedElement> computeSigmas(List<AuthCand> candidates, List<FieldElement> masks,
      List<FieldElement> openRhos) {
    List<AuthenticatedElement> sigmas = IntStream.range(0, candidates.size()).mapToObj(idx -> {
      AuthCand cand = candidates.get(idx);
      FieldElement mask = masks.get(idx);
      FieldElement openRho = openRhos.get(idx);
      return cand.computeSigma(openRho, mask);
    }).collect(Collectors.toList());
    return sigmas;
  }

  List<MultTriple> toMultTriples(List<AuthCand> candidates) {
    return candidates.stream().map(AuthCand::toTriple).collect(Collectors.toList());
  }

  List<MultTriple> sacrifice(List<AuthCand> candidates) {
    List<FieldElement> masks =
        jointSampler.getNext(getModulus(), getModBitLength(), candidates.size());

    // compute masked values we will open and use in mac-check
    List<AuthenticatedElement> rhos = computeRhos(candidates, masks);

    // open masked values
    List<FieldElement> openRhos = elementGeneration.open(rhos);

    // compute macs
    List<AuthenticatedElement> sigmas = computeSigmas(candidates, masks, openRhos);

    // put rhos and sigmas together
    rhos.addAll(sigmas);

    // pad open rhos with zeroes, one for each sigma
    List<FieldElement> paddedRhos = getFieldElementUtils().padWith(openRhos,
        new FieldElement(0, getModulus(), getModBitLength()), sigmas.size());

    // run mac-check
    // TODO check if we can avoid re-masking
    elementGeneration.check(rhos, paddedRhos);

    // convert candidates to valid triples and return
    return toMultTriples(candidates);
  }

  /**
   * Generates numTriples multiplication triples in a batch.
   *
   * @param numTriples number of triples to generate
   * @return valid multiplication triples
   */
  public List<MultTriple> triple(int numTriples) {
    // generate random left factor groups
    List<FieldElement> leftFactorGroups = getLocalSampler().getNext(getModulus(), getModBitLength(),
        numTriples * getNumCandidatesPerTriple());

    // generate random right factors
    List<FieldElement> rightFactors =
        getLocalSampler().getNext(getModulus(), getModBitLength(), numTriples);

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

  private final class UnauthTriple {
    private final List<FieldElement> leftFactors;
    private final FieldElement rightFactor;
    private final List<FieldElement> product;

    public UnauthTriple(List<FieldElement> leftFactors, FieldElement rightFactor,
        List<FieldElement> product) {
      super();
      this.leftFactors = leftFactors;
      this.rightFactor = rightFactor;
      this.product = product;
    }

    UnauthCand toCandidate(List<FieldElement> masks, List<FieldElement> sacrificeMasks) {
      FieldElement left = getFieldElementUtils().innerProduct(leftFactors, masks);
      FieldElement prod = getFieldElementUtils().innerProduct(product, masks);
      FieldElement leftSac = getFieldElementUtils().innerProduct(leftFactors, sacrificeMasks);
      FieldElement prodSac = getFieldElementUtils().innerProduct(product, sacrificeMasks);
      return new UnauthCand(left, rightFactor, prod, leftSac, prodSac);
    }
  }

  private class TripleCandidate<T> {
    protected final T leftFactor;
    protected final T rightFactor;
    protected final T product;
    protected final T leftFactorHat;
    protected final T productHat;

    TripleCandidate(T leftFactor, T rightFactor, T product, T leftFactorHat, T productHat) {
      this.leftFactor = leftFactor;
      this.rightFactor = rightFactor;
      this.product = product;
      this.leftFactorHat = leftFactorHat;
      this.productHat = productHat;
    }

    TripleCandidate(List<T> ordered) {
      this(ordered.get(0), ordered.get(1), ordered.get(2), ordered.get(3), ordered.get(4));
    }

    Stream<T> stream() {
      return Stream.of(leftFactor, rightFactor, product, leftFactorHat, productHat);
    }
  }

  private final class UnauthCand extends TripleCandidate<FieldElement> {
    UnauthCand(FieldElement leftFactor, FieldElement rightFactor, FieldElement product,
        FieldElement leftFactorHat, FieldElement productHat) {
      super(leftFactor, rightFactor, product, leftFactorHat, productHat);
    }
  }

  private final class AuthCand extends TripleCandidate<AuthenticatedElement> {
    public AuthCand(List<AuthenticatedElement> ordered) {
      super(ordered);
    }

    public AuthenticatedElement computeRho(FieldElement mask) {
      return leftFactor.multiply(mask).subtract(leftFactorHat);
    }

    public AuthenticatedElement computeSigma(FieldElement openRho, FieldElement mask) {
      return product.multiply(mask).subtract(productHat).subtract(rightFactor.multiply(openRho));
    }

    public MultTriple toTriple() {
      return new MultTriple(leftFactor, rightFactor, product);
    }
  }

}
