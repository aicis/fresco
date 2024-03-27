package dk.alexandra.fresco.tools.mascot.triple;

import dk.alexandra.fresco.framework.builder.numeric.Addable;
import dk.alexandra.fresco.framework.builder.numeric.field.FieldElement;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.tools.mascot.MascotResourcePool;
import dk.alexandra.fresco.tools.mascot.elgen.ElementGeneration;
import dk.alexandra.fresco.tools.mascot.field.AuthenticatedElement;
import dk.alexandra.fresco.tools.mascot.field.FieldElementUtils;
import dk.alexandra.fresco.tools.mascot.field.MultiplicationTriple;
import dk.alexandra.fresco.tools.mascot.prg.FieldElementPrg;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Actively-secure protocol for computing authenticated, secret-shared multiplication triples based
 * on the MASCOT protocol (<a href="https://eprint.iacr.org/2016/505.pdf">https://eprint.iacr.org/2016/505.pdf</a>).
 *
 * <p>In particular, produces random, authenticated, secret-shared triples of the form a, b, c such
 * that <i>a * b = c</i>. This protocol is refered to as <i>&Pi;<sub>Triple</sub></i> and listed as
 * <i>Protocol 4</i> in the MASCOT paper</p>
 */
public class TripleGeneration {

  private final ElementGeneration elementGeneration;
  private final Map<Integer, MultiplyRight> rightMultipliers;
  private final Map<Integer, MultiplyLeft> leftMultipliers;
  private final FieldElementPrg jointSampler;
  private final MascotResourcePool resourcePool;
  private final FieldElementUtils fieldElementUtils;

  /**
   * Creates new triple generation protocol.
   */
  public TripleGeneration(MascotResourcePool resourcePool, Network network,
      ElementGeneration elementGeneration, FieldElementPrg jointSampler) {
    this.resourcePool = Objects.requireNonNull(resourcePool);
    this.fieldElementUtils = new FieldElementUtils(resourcePool.getFieldDefinition());
    this.leftMultipliers = new HashMap<>();
    this.rightMultipliers = new HashMap<>();
    initializeMultipliers(resourcePool, network);
    this.elementGeneration = Objects.requireNonNull(elementGeneration);
    this.jointSampler = Objects.requireNonNull(jointSampler);
  }

  private void initializeMultipliers(MascotResourcePool resourcePool, Network network) {
    for (int partyId = 1; partyId <= resourcePool.getNoOfParties(); partyId++) {
      if (partyId != resourcePool.getMyId()) {
        if (resourcePool.getMyId() < partyId) {
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
  }

  TripleGeneration(MascotResourcePool resourcePool, Network network, FieldElementPrg jointSampler,
      FieldElement macKeyShare) {
    this(resourcePool, network,
        new ElementGeneration(resourcePool, network, macKeyShare, jointSampler), jointSampler);
  }

  /**
   * Generates numTriples multiplication triples in a batch. <p>Implements Protocol 4 (all steps).
   * Note that while the paper describes a protocol for generating a single triple, this
   * implementation produces a batch of multiplication triples.</p>
   *
   * @param numTriples number of triples to generate
   * @return valid multiplication triples
   */
  public List<MultiplicationTriple> triple(int numTriples) {
    // generate random left factor groups
    List<FieldElement> leftFactorGroups = resourcePool.getLocalSampler()
        .getNext(numTriples * resourcePool.getNumCandidatesPerTriple());
    // generate random right factors
    List<FieldElement> rightFactors = resourcePool.getLocalSampler().getNext(numTriples);
    // compute product groups
    List<FieldElement> productGroups = multiply(leftFactorGroups, rightFactors);
    // combine into unauthenticated triples
    List<UnauthenticatedTriple> unauthTriples =
        toUnauthenticatedTriple(leftFactorGroups, rightFactors, productGroups);
    // combine unauthenticated triples into unauthenticated triple candidates
    List<UnauthenticatedCandidate> candidates = combine(unauthTriples);
    // use el-gen to input candidates and combine them to the authenticated candidates
    List<AuthenticatedCandidate> authenticated = authenticate(candidates);
    // for each candidate, run sacrifice and get valid triple
    return sacrifice(authenticated);
  }

  /**
   * Computes the unauthenticated, secret-shared products of leftFactorGroups and rightFactors.
   * <p>Implements batched version of Multiply sub-protocol of Protocol 4.</p> <p>Note that for each
   * right factor, there are multiple left factors (the number of left factors depends on the
   * security parameter {@link MascotResourcePool#getNumCandidatesPerTriple()}).</p> <p>For
   * instance, given two right factors <i>r<sub>0</sub></i>, <i>r<sub>1</sub></i> and corresponding
   * left factor groups (each group of size 2) <i>l<sub>0,0</sub></i>, <i>l<sub>0,1</sub></i>,
   * <i>l<sub>1,0</sub></i>, <i>l<sub>1,1</sub></i></p>, the result will be <i>r<sub>0</sub></i> *
   * <i> l<sub>0,0</sub></i>, <i>r<sub>0</sub></i> * <i>l<sub>0,1</sub></i>, <i>r<sub>1</sub></i> *
   * <i>l<sub>1,0</sub></i>, <i>r<sub>1</sub></i> * <i>l<sub>1,1</sub></i></p>
   *
   * @param leftFactorGroups the left factors going into product evaluation. Note that these
   *     are referred to as groups since there are multiple (numLeftFactors) left factors per right
   *     factor.
   * @param rightFactors the right factors going into product evaluation.
   * @return unauthenticated product shares
   */
  List<FieldElement> multiply(List<FieldElement> leftFactorGroups,
      List<FieldElement> rightFactors) {
    // step 1 of protocol occurred before this method
    // "stretch" right factors, so we have one right factor for each left factor
    List<FieldElement> stretched =
        fieldElementUtils
            .stretch(rightFactors, resourcePool.getNumCandidatesPerTriple());

    // step 2 of protocol
    // for each value we will have two sub-factors for each other party
    List<List<FieldElement>> subFactors = new ArrayList<>();
    for (int partyId = 1; partyId <= resourcePool.getNoOfParties(); partyId++) {
      if (partyId != resourcePool.getMyId()) {
        MultiplyLeft leftMult = leftMultipliers.get(partyId);
        MultiplyRight rightMult = rightMultipliers.get(partyId);
        if (resourcePool.getMyId() < partyId) {
          subFactors.add(rightMult.multiply(stretched));
          subFactors.add(leftMult.multiply(leftFactorGroups));
        } else {
          subFactors.add(leftMult.multiply(leftFactorGroups));
          subFactors.add(rightMult.multiply(stretched));
        }
      }
    }

    // step 3 or protocol
    // own part of the product
    List<FieldElement> localSubFactors =
        fieldElementUtils.pairWiseMultiply(leftFactorGroups, stretched);
    subFactors.add(localSubFactors);

    // combine all sub-factors into product shares
    return Addable.sumRows(subFactors);
  }

  /**
   * Implements batched version of Combine sub-protocol of Protocol 4.
   */
  private List<UnauthenticatedCandidate> combine(List<UnauthenticatedTriple> triples) {
    // step 1 of protocol
    int numTriples = triples.size();

    List<List<FieldElement>> masks = jointSampler
        .getNext(numTriples, resourcePool.getNumCandidatesPerTriple());

    List<List<FieldElement>> sacrificeMasks = jointSampler
        .getNext(numTriples, resourcePool.getNumCandidatesPerTriple());

    // step 2 of protocol
    return IntStream.range(0, numTriples)
        .mapToObj(idx -> {
          UnauthenticatedTriple triple = triples.get(idx);
          List<FieldElement> m = masks.get(idx);
          List<FieldElement> ms = sacrificeMasks.get(idx);
          return triple.toCandidate(m, ms);
        })
        .collect(Collectors.toList());
  }

  /**
   * Implements batched version of Authenticate sub-protocol of Protocol 4.
   */
  private List<AuthenticatedCandidate> authenticate(List<UnauthenticatedCandidate> candidates) {
    List<FieldElement> flatInputs = candidates.parallelStream()
        .flatMap(TripleCandidate::stream)
        .collect(Collectors.toList());

    List<List<AuthenticatedElement>> shares = new ArrayList<>();
    for (int partyId = 1; partyId <= resourcePool.getNoOfParties(); partyId++) {
      if (partyId == resourcePool.getMyId()) {
        shares.add(elementGeneration.input(flatInputs));
      } else {
        shares.add(elementGeneration.input(partyId, flatInputs.size()));
      }
    }

    List<AuthenticatedElement> combined = Addable.sumRows(shares);
    return toAuthenticatedCandidate(combined, 5);
  }

  /**
   * Implements batched version of Sacrifice sub-protocol of Protocol 4.
   */
  private List<MultiplicationTriple> sacrifice(List<AuthenticatedCandidate> candidates) {
    // step 1 or protocol
    List<FieldElement> randomCoefficients = jointSampler.getNext(candidates.size());

    // step 2
    // compute masked values we will open and use in mac-check
    List<AuthenticatedElement> rhos = computeRhos(candidates, randomCoefficients);

    // step 3
    // open masked values
    List<FieldElement> openRhos = elementGeneration.open(rhos);

    // step 4
    // compute macs
    List<AuthenticatedElement> sigmas = computeSigmas(candidates, randomCoefficients, openRhos);

    // step 5
    // put rhos and sigmas together
    rhos.addAll(sigmas);
    // pad open rhos with zeroes, one for each sigma
    List<FieldElement> paddedRhos = fieldElementUtils
        .padWith(openRhos, resourcePool.getFieldDefinition().createElement(0), sigmas.size());
    // run mac-check
    elementGeneration.check(rhos, paddedRhos);

    // convert candidates to valid triples and return
    return toMultTriples(candidates);
  }

  private List<UnauthenticatedTriple> toUnauthenticatedTriple(List<FieldElement> left,
      List<FieldElement> right,
      List<FieldElement> products) {
    Stream<UnauthenticatedTriple> stream = IntStream.range(0, right.size()).mapToObj(idx -> {
      int groupStart = idx * resourcePool.getNumCandidatesPerTriple();
      int groupEnd = (idx + 1) * resourcePool.getNumCandidatesPerTriple();
      return new UnauthenticatedTriple(left.subList(groupStart, groupEnd), right.get(idx),
          products.subList(groupStart, groupEnd));
    });
    return stream.collect(Collectors.toList());
  }

  private List<AuthenticatedCandidate> toAuthenticatedCandidate(List<AuthenticatedElement> list,
      int partSize) {
    int numParts = list.size() / partSize;
    return IntStream.range(0, numParts).mapToObj(idx -> {
      List<AuthenticatedElement> batch = list.subList(idx * partSize, (idx + 1) * partSize);
      return new AuthenticatedCandidate(batch);
    }).collect(Collectors.toList());
  }

  private List<AuthenticatedElement> computeRhos(List<AuthenticatedCandidate> candidates,
      List<FieldElement> masks) {
    return IntStream.range(0, candidates.size()).mapToObj(idx -> {
      AuthenticatedCandidate cand = candidates.get(idx);
      FieldElement mask = masks.get(idx);
      return cand.computeRho(mask);
    }).collect(Collectors.toList());
  }

  private List<AuthenticatedElement> computeSigmas(List<AuthenticatedCandidate> candidates,
      List<FieldElement> masks,
      List<FieldElement> openRhos) {
    return IntStream.range(0, candidates.size()).mapToObj(idx -> {
      AuthenticatedCandidate cand = candidates.get(idx);
      FieldElement mask = masks.get(idx);
      FieldElement openRho = openRhos.get(idx);
      return cand.computeSigma(openRho, mask);
    }).collect(Collectors.toList());
  }

  private List<MultiplicationTriple> toMultTriples(List<AuthenticatedCandidate> candidates) {
    return candidates.stream().map(AuthenticatedCandidate::toTriple).collect(Collectors.toList());
  }

  /**
   * Represents single unauthenticated multiplication triple. <p>Contains a single left factor group
   * <b>a</b>, single right factor <i>b</i>, and product group <b>c</b>. An unauthenticated triple
   * will go into the Combine sub-protocol of Protocol 4.</p>
   */
  private final class UnauthenticatedTriple {

    private final List<FieldElement> leftFactors;
    private final FieldElement rightFactor;
    private final List<FieldElement> product;

    UnauthenticatedTriple(List<FieldElement> leftFactors, FieldElement rightFactor,
        List<FieldElement> product) {
      super();
      this.leftFactors = leftFactors;
      this.rightFactor = rightFactor;
      this.product = product;
    }

    UnauthenticatedCandidate toCandidate(List<FieldElement> masks,
        List<FieldElement> sacrificeMasks) {
      FieldElement left = fieldElementUtils.innerProduct(leftFactors, masks);
      FieldElement prod = fieldElementUtils.innerProduct(product, masks);
      FieldElement leftSac = fieldElementUtils.innerProduct(leftFactors, sacrificeMasks);
      FieldElement prodSac = fieldElementUtils.innerProduct(product, sacrificeMasks);
      return new UnauthenticatedCandidate(left, rightFactor, prod, leftSac, prodSac);
    }
  }

  /**
   * Represents single unauthenticated triple candidate (<i>a</i>, <i>b</i>, <i>c</i>, <i>a'</i>,
   * <i>c'</i>). <p>An unauthenticated triple candidate is the input to the authentication
   * sub-protocol of Protocol 4.</p>
   */
  private final class UnauthenticatedCandidate extends TripleCandidate<FieldElement> {

    UnauthenticatedCandidate(FieldElement leftFactor, FieldElement rightFactor,
        FieldElement product, FieldElement leftFactorHat, FieldElement productHat) {
      super(leftFactor, rightFactor, product, leftFactorHat, productHat);
    }
  }

  /**
   * Represents single authenticated triple candidate (<i>[[a]]</i>, <i>[[b]]</i>, <i>[[c]]</i>,
   * <i>[[a']]</i>, <i>[[c']]</i>). <p>Note <i>[[a']]</i>, <i>[[c']]</i> in the Sacrifice
   * sub-protocol of Protocol 4.</p>
   */
  private final class AuthenticatedCandidate extends TripleCandidate<AuthenticatedElement> {

    AuthenticatedCandidate(List<AuthenticatedElement> ordered) {
      super(ordered);
    }

    AuthenticatedElement computeRho(FieldElement mask) {
      return leftFactor.multiply(mask).subtract(leftFactorHat);
    }

    AuthenticatedElement computeSigma(FieldElement openRho, FieldElement mask) {
      return product.multiply(mask).subtract(productHat).subtract(rightFactor.multiply(openRho));
    }

    MultiplicationTriple toTriple() {
      return new MultiplicationTriple(leftFactor, rightFactor, product);
    }
  }

  private class TripleCandidate<T> {

    final T leftFactor;
    final T rightFactor;
    final T product;
    final T leftFactorHat;
    final T productHat;

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
}
