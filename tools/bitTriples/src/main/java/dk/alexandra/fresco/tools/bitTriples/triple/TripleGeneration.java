package dk.alexandra.fresco.tools.bitTriples.triple;

import dk.alexandra.fresco.framework.MaliciousException;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.ByteArrayHelper;
import dk.alexandra.fresco.framework.util.ExceptionConverter;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.bitTriples.BitTripleResourcePool;
import dk.alexandra.fresco.tools.bitTriples.bracket.Bracket;
import dk.alexandra.fresco.tools.bitTriples.cote.CoteInstances;
import dk.alexandra.fresco.tools.bitTriples.elements.AuthenticatedElement;
import dk.alexandra.fresco.tools.bitTriples.elements.MultiplicationTriple;
import dk.alexandra.fresco.tools.bitTriples.maccheck.MacCheck;
import dk.alexandra.fresco.tools.bitTriples.prg.BytePrg;
import dk.alexandra.fresco.tools.bitTriples.utils.VectorOperations;
import dk.alexandra.fresco.tools.ot.otextension.CoteFactory;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Actively-secure protocol for computing authenticated, secret-shared multiplication triples based
 * on the MASCOT protocol (<a
 * href="https://eprint.iacr.org/2016/505.pdf">https://eprint.iacr.org/2016/505.pdf</a>).
 *
 * <p>In particular, produces random, authenticated, secret-shared triples of the form a, b, c such
 * that <i>a * b = c</i>. This protocol is refered to as <i>&Pi;<sub>Triple</sub></i> and listed as
 * <i>Protocol 4</i> in the MASCOT paper
 */
public class TripleGeneration {

  private final BitTripleResourcePool resourcePool;
  private final Network network;
  private final StrictBitVector macLeft;
  private final StrictBitVector macRight;
  private final Bracket bracket;
  private final CoteInstances coTeInstances;
  private final int kappa;
  private final MacCheck macChecker;
  private final Map<Integer, List<StrictBitVector>> ts;
  private final Map<Integer, List<StrictBitVector>> qs;
  private final BytePrg jointSampler;
  private int c;

  /** Creates new triple generation protocol. */
  public TripleGeneration(
      BitTripleResourcePool resourcePool, Network network, int kappa, BytePrg jointSampler,
      StrictBitVector macLeft, StrictBitVector macRight) {
    this.resourcePool = resourcePool;
    this.network = network;
    this.kappa = kappa;
    ts = new HashMap<>();
    qs = new HashMap<>();
    this.macChecker = new MacCheck(resourcePool, network, jointSampler);
    this.jointSampler = jointSampler;

    this.macLeft = macLeft;
    this.macRight = macRight;
    StrictBitVector macConcat = StrictBitVector.concat(macLeft, macRight);

    Bracket bracket = new Bracket(resourcePool, network, macConcat, jointSampler); //
    bracket.input(kappa); //
    // Step 2 of Initialization
    this.coTeInstances = bracket.getCOTeInstances();
    this.bracket = new Bracket(resourcePool, network, macRight, jointSampler);
    // Step 3 of Initialization - Check consistency of macs
  }

  public TripleGeneration(BitTripleResourcePool resourcePool, Network network, int kappa, BytePrg jointSampler){
    this(resourcePool,network,kappa,jointSampler,resourcePool.getLocalSampler().getNext(kappa),resourcePool.getLocalSampler().getNext(kappa));
  }

  /**
   * Generates numTriples multiplication triples in a batch.
   *
   * <p>Implements Protocol 4 (all steps). Note that while the paper describes a protocol for
   * generating a single triple, this implementation produces a batch of multiplication triples.
   *
   * @param noOfTriples number of triples to generate
   * @return valid multiplication triples
   */
  public List<MultiplicationTriple> triple(int noOfTriples) {
    c = getBucketSize(noOfTriples, resourcePool.getStatisticalSecurityByteParameter());
    int triplesToGenerate = nearestPowerOfEight(noOfTriples * c * c + c);

    // Extend cote
    StrictBitVector xs = extend(triplesToGenerate);

    Pair<StrictBitVector, StrictBitVector> yz = generateTriples(xs);

    List<StrictBitVector> xShares = bracket.input(xs);
    List<StrictBitVector> yShares = bracket.input(yz.getFirst());
    List<StrictBitVector> zShares = bracket.input(yz.getSecond());

    List<AuthenticatedCandidate> authenticatedCandidate =
        toAuthenticatedCandidate(
            toAuthenticatedElement(xShares, xs),
            toAuthenticatedElement(yShares, yz.getFirst()),
            toAuthenticatedElement(zShares, yz.getSecond()));

    return checkTriples(
        authenticatedCandidate,
        getBucketSize(noOfTriples, resourcePool.getStatisticalSecurityByteParameter()));
  }

  private Pair<StrictBitVector, StrictBitVector> generateTriples(StrictBitVector xs) {
    // Step 1
    StrictBitVector ys = resourcePool.getLocalSampler().getNext(xs.getSize());
    // Step 2a
    Map<Integer, StrictBitVector> ws = breakCorrelation(ts, null);
    Map<Integer, StrictBitVector> v0s = breakCorrelation(qs, null);
    Map<Integer, StrictBitVector> v1s = breakCorrelation(qs, macLeft);
    // Step 2b
    List<StrictBitVector> ns = createNewCorrelations(ys, xs, v0s, v1s, ws);
    // Step 3
    StrictBitVector zs = computeZs(ns, xs, ys, v0s);

    return new Pair<>(ys, zs);
  }

  private StrictBitVector computeZs(
      List<StrictBitVector> ns,
      StrictBitVector xs,
      StrictBitVector ys,
      Map<Integer, StrictBitVector> v0s) {

    StrictBitVector u =
        VectorOperations.xor(VectorOperations.sum(ns), VectorOperations.and(xs, ys));

    return VectorOperations.xor(u, VectorOperations.sum(VectorOperations.mapToList(v0s)));
  }

  private List<StrictBitVector> createNewCorrelations(
      StrictBitVector ys,
      StrictBitVector xs,
      Map<Integer, StrictBitVector> v0s,
      Map<Integer, StrictBitVector> v1s,
      Map<Integer, StrictBitVector> ws) {
    Map<Integer, StrictBitVector> correlatedVectors = new HashMap<>();
    for (int j = 1; j <= resourcePool.getNoOfParties(); j++) {
      if (resourcePool.getMyId() != j) {
        StrictBitVector s = VectorOperations.xor(v1s.get(j), VectorOperations.xor(ys, v0s.get(j)));
        correlatedVectors.put(j, s);
      }
    }
    HashMap<Integer, StrictBitVector> sReceived = new HashMap<>();
    for (int otherId = 1; otherId <= resourcePool.getNoOfParties(); otherId++) {
      if (resourcePool.getMyId() != otherId) {
        if (resourcePool.getMyId() < otherId) {
          network.send(
              otherId,
              resourcePool
                  .getStrictBitVectorSerializer()
                  .serialize(correlatedVectors.get(otherId)));
          sReceived.put(
              otherId,
              resourcePool.getStrictBitVectorSerializer().deserialize(network.receive(otherId)));

        } else {
          sReceived.put(
              otherId,
              resourcePool.getStrictBitVectorSerializer().deserialize(network.receive(otherId)));
          network.send(
              otherId,
              resourcePool
                  .getStrictBitVectorSerializer()
                  .serialize(correlatedVectors.get(otherId)));
        }
      }
    }

    List<StrictBitVector> ns = new ArrayList<>();
    for (int j = 1; j <= resourcePool.getNoOfParties(); j++) {
      if (resourcePool.getMyId() != j) {
        StrictBitVector nj =
            VectorOperations.xor(VectorOperations.and(xs, sReceived.get(j)), ws.get(j));
        ns.add(nj);
      }
    }
    return ns;
  }

  private Map<Integer, StrictBitVector> breakCorrelation(
      Map<Integer, List<StrictBitVector>> toBreak, StrictBitVector mac) {
    Map<Integer, StrictBitVector> result = new HashMap<>();
    for (int j = 1; j <= resourcePool.getNoOfParties(); j++) {
      if (resourcePool.getMyId() != j) {
        // For each party
        List<StrictBitVector> listForPartyJ = toBreak.get(j);
        StrictBitVector resultingVector = new StrictBitVector(listForPartyJ.size());
        for (int h = 0; h < listForPartyJ.size(); h++) {
          // For each vector, hash to single bit.
          StrictBitVector toHash =
              new StrictBitVector(
                  Arrays.copyOfRange(listForPartyJ.get(h).toByteArray().clone(), 0, kappa / 8));
          if (mac != null) {
            toHash.xor(mac);
          }
          byte[] hashed = resourcePool.getMessageDigest().digest(toHash.toByteArray());
          resultingVector.setBit(h, ByteArrayHelper.getBit(hashed, 0), false);
        }
        result.put(j, resultingVector);
      }
    }
    return result;
  }

  private StrictBitVector extend(int numberOfInputBits) {
    StrictBitVector input = resourcePool.getLocalSampler().getNext(numberOfInputBits);
    for (int receiver = 1; receiver <= network.getNoOfParties(); receiver++) {
      for (int sender = 1; sender <= network.getNoOfParties(); sender++) {
        if (receiver != sender) {
          CoteFactory instance = coTeInstances.get(receiver, sender);
          if (resourcePool.getMyId() == sender) {
            qs.put(receiver, instance.getSender().extend(numberOfInputBits));
          } else if (resourcePool.getMyId() == receiver) {
            ts.put(sender, instance.getReceiver().extend(input));
          }
        }
      }
    }
    return input;
  }

  /** Implements sub-protocol CheckTriples of Figure 23 */
  private List<MultiplicationTriple> checkTriples(
      List<AuthenticatedCandidate> candidates, int bucketSize) {

    List<OpenedElement> openedElements = new ArrayList<>();
    // Phase 1
    List<AuthenticatedCandidate> cutCandidates = cutAndChoose(candidates, c, openedElements);
    // Phase 2
    List<AuthenticatedCandidate> sacrificedBuckets =
        bucketSacrifice(cutCandidates, bucketSize, openedElements);

    // Phase 3
    List<AuthenticatedCandidate> combinedBuckets =
        combineBuckets(sacrificedBuckets, bucketSize, openedElements);

    // Phase 4
    checkOpenedValues(openedElements);

    // convert candidates to valid triples and return
    return toMultTriples(combinedBuckets);
  }

  private void checkOpenedValues(List<OpenedElement> openedElements) {
    int nearestPower = nearestMultipleOfEight(openedElements.size());
    StrictBitVector values = new StrictBitVector(nearestPower);
    List<StrictBitVector> macShares = new ArrayList<>(nearestPower);
    for (int i = 0; i < openedElements.size(); i++) {
      values.setBit(i, openedElements.get(i).value, false);
      macShares.add(openedElements.get(i).share);
    }
    for (int i = 0; i < nearestPower - openedElements.size(); i++) {
      macShares.add(new StrictBitVector(kappa));
    }
    macChecker.check(values, macShares, macRight);
  }

  private int nearestMultipleOfEight(int size) {
    return size + (8 - size % 8);
  }

  private List<AuthenticatedCandidate> combineBuckets(
      List<AuthenticatedCandidate> candidates, int bucketSize, List<OpenedElement> openedElements) {
    int noOfBuckets = candidates.size() / bucketSize;

    List<AuthenticatedCandidate> permuted = randomPermute(candidates);
    // construct buckets of size noPerBucket
    List<List<AuthenticatedCandidate>> buckets = new ArrayList<>();
    for (int i = 0; i < noOfBuckets; i++) {
      List<AuthenticatedCandidate> bucket = new ArrayList<>();
      for (int j = 0; j < bucketSize; j++) {
        bucket.add(permuted.get(i * bucketSize + j));
      }
      buckets.add(bucket);
    }
    List<AuthenticatedCandidate> triples = new ArrayList<>();
    for (List<AuthenticatedCandidate> bucket : buckets) {
      triples.add(combineSingleBucket(bucket, openedElements));
    }
    return triples;
  }

  private AuthenticatedCandidate combineSingleBucket(
      List<AuthenticatedCandidate> bucket, List<OpenedElement> openedElements) {
    AuthenticatedCandidate firstCandidate = bucket.remove(0);
    return recursivelyCombine(firstCandidate, bucket, openedElements);
  }

  private AuthenticatedCandidate recursivelyCombine(
      AuthenticatedCandidate accumulatedCombination,
      List<AuthenticatedCandidate> bucket,
      List<OpenedElement> openedElements) {
    if (bucket.size() <= 0) {
      return accumulatedCombination;
    } else {
      AuthenticatedCandidate accumulate =
          combineTwoTriples(accumulatedCombination, bucket.remove(0), openedElements);
      return recursivelyCombine(accumulate, bucket, openedElements);
    }
  }

  private AuthenticatedCandidate combineTwoTriples(
      AuthenticatedCandidate left,
      AuthenticatedCandidate right,
      List<OpenedElement> openedElements) {
    AuthenticatedElement ys = left.rightFactor.xor(right.rightFactor);
    OpenedElement openedYs = openElement(ys);
    AuthenticatedElement xNew = left.leftFactor.xor(right.leftFactor);
    AuthenticatedElement zNew =
        left.product.xor(right.product).xor(right.leftFactor.and(openedYs.value));
    openedElements.add(openedYs);
    return new AuthenticatedCandidate(xNew, left.rightFactor, zNew);
  }

  private List<AuthenticatedCandidate> bucketSacrifice(
      List<AuthenticatedCandidate> candidates, int bucketSize, List<OpenedElement> openedElements) {
    int noOfBuckets = candidates.size() / bucketSize;

    List<AuthenticatedCandidate> permuted = randomPermute(candidates);

    // construct buckets
    List<AuthenticatedCandidate> resultingCandidates = new ArrayList<>();
    for (int i = 0; i < noOfBuckets; i++) {
      List<AuthenticatedCandidate> bucket = new ArrayList<>();
      for (int j = 0; j < bucketSize; j++) {
        bucket.add(permuted.get(i * bucketSize + j));
      }
      AuthenticatedCandidate sacrificed =
          sacrificeBucket(bucket.subList(1, bucketSize), bucket.get(0), openedElements);
      resultingCandidates.add(sacrificed);
    }
    return resultingCandidates;
  }

  private List<AuthenticatedCandidate> randomPermute(List<AuthenticatedCandidate> candidates) {
      SecureRandom random = ExceptionConverter.safe(
          () -> SecureRandom.getInstance("SHA1PRNG"),
          "Configuration error, SHA1PRNG is needed for BitTriple");
      StrictBitVector v = jointSampler.getNext(resourcePool.getPrgSeedBitLength());
      random.setSeed(v.toByteArray());
      Collections.shuffle(candidates, random);
      return candidates;
  }

  /** Implements batched version of Sacrifice sub-protocol of Protocol 4. */
  private AuthenticatedCandidate sacrificeBucket(
      List<AuthenticatedCandidate> candidates,
      AuthenticatedCandidate bucketHead,
      List<OpenedElement> openedElements) {
    for (AuthenticatedCandidate candidate : candidates) {
      checkR(bucketHead, candidate, openedElements);
    }
    return bucketHead;
  }

  /**
   * a=[x_i]+[x_j] og b=[y_i]+[y_j] c = [z_j]+[z_i]+[x_i]*b+[y_i]*a - check if c = a&&b
   *
   * @param bucketHead
   * @param candidate
   * @param openedElements
   * @return
   */
  protected void checkR(
      AuthenticatedCandidate bucketHead,
      AuthenticatedCandidate candidate,
      List<OpenedElement> openedElements) {

    AuthenticatedElement a = bucketHead.leftFactor.xor(candidate.leftFactor);
    OpenedElement aOpen = openElement(a);
    AuthenticatedElement b = bucketHead.rightFactor.xor(candidate.rightFactor);
    OpenedElement bOpen = openElement(b);
    AuthenticatedElement c =
        bucketHead
            .product
            .xor(candidate.product)
            .xor(bucketHead.leftFactor.and(bOpen.value))
            .xor(bucketHead.rightFactor.and(aOpen.value));
    openedElements.add(aOpen);
    openedElements.add(bOpen);
    OpenedElement cOpen = openElement(c);
    if (cOpen.value != (aOpen.value && bOpen.value)) {
      throw new MaliciousException("Verification of bucket head failed");
    }
  }

  private OpenedElement openElement(AuthenticatedElement element) {
    StrictBitVector toSend = new StrictBitVector(8);
    toSend.setBit(0, element.getBit(), false);
    return new OpenedElement(VectorOperations.openVector(toSend, resourcePool, network).getBit(0, false), element.getMac());
  }

  private List<AuthenticatedCandidate> cutAndChoose(
      List<AuthenticatedCandidate> candidates, int c, List<OpenedElement> openedElements) {

    StrictBitVector randomIndices =
        VectorOperations.generateRandomIndices(c, candidates.size(), resourcePool, jointSampler);
    List<AuthenticatedCandidate> candidatesToCheck = new ArrayList<>();
    List<AuthenticatedCandidate> unopenedCandidates = new ArrayList<>();
    for (int i = 0; i < randomIndices.getSize(); i++) {
      if (randomIndices.getBit(i, false)) {
        candidatesToCheck.add(candidates.get(i));
      } else {
        unopenedCandidates.add(candidates.get(i));
      }
    }

    checkMultiplicationPredicate(candidatesToCheck, openedElements);
    return unopenedCandidates;
  }

  protected void checkMultiplicationPredicate(
      List<AuthenticatedCandidate> candidates, List<OpenedElement> openedElements) {
    StrictBitVector xs = new StrictBitVector(nearestMultipleOfEight(candidates.size()));
    StrictBitVector ys = new StrictBitVector(nearestMultipleOfEight(candidates.size()));
    StrictBitVector zs = new StrictBitVector(nearestMultipleOfEight(candidates.size()));
    for (int i = 0; i < candidates.size(); i++) { //
      xs.setBit(i, candidates.get(i).leftFactor.getBit(), false);
      ys.setBit(i, candidates.get(i).rightFactor.getBit(), false);
      zs.setBit(i, candidates.get(i).product.getBit(), false);
    }

    StrictBitVector xOpen = VectorOperations.openVector(xs, resourcePool, network);
    StrictBitVector yOpen = VectorOperations.openVector(ys, resourcePool, network);
    StrictBitVector zOpen = VectorOperations.openVector(zs, resourcePool, network);

    for (int i = 0; i < candidates.size(); i++) {
      openedElements.add(new OpenedElement(xOpen.getBit(i, false), candidates.get(i).leftFactor.getMac()));
      openedElements.add(new OpenedElement(yOpen.getBit(i, false), candidates.get(i).rightFactor.getMac()));
      openedElements.add(new OpenedElement(zOpen.getBit(i, false), candidates.get(i).product.getMac()));
    }

    if (!VectorOperations.and(xOpen, yOpen).equals(zOpen)) {
      throw new MaliciousException("Aborting - multiplication predicates were not satisfied");
    }
  }

  private List<MultiplicationTriple> toMultTriples(List<AuthenticatedCandidate> candidates) {
    return candidates.stream().map(AuthenticatedCandidate::toTriple).collect(Collectors.toList());
  }

  protected static int getBucketSize(int numberOfTriples, int securityParameter) {
    if (securityParameter <= 40) {
      if (numberOfTriples <= 1024) {
        return 5;
      } else if (numberOfTriples <= 16384) {
        return 4;
      } else {
        return 3;
      }
    } else {
      if (numberOfTriples <= 1024) {
        return 8;
      } else if (numberOfTriples <= 16384) {
        return 6;
      } else {
        return 5;
      }
    }
  }


  private int nearestPowerOfEight(int noOfTriples) {
    int power = 8;
    while (power < noOfTriples) {
      power = power * 8;
    }
    return power;
  }


  protected static List<AuthenticatedElement> toAuthenticatedElement(
      List<StrictBitVector> macs, StrictBitVector shares) {
    if (macs.size() != shares.getSize()) {
      throw new IllegalArgumentException("There must be the same number of shares and macs.");
    }
    List<AuthenticatedElement> toReturn = new ArrayList<>();
    for (int i = 0; i < macs.size(); i++) {
      toReturn.add(new AuthenticatedElement(shares.getBit(i, false), macs.get(i)));
    }
    return toReturn;
  }

  protected static List<AuthenticatedCandidate> toAuthenticatedCandidate(
      List<AuthenticatedElement> xs, List<AuthenticatedElement> ys, List<AuthenticatedElement> zs) {
    if (xs.size() != ys.size() || xs.size() != zs.size()) {
      throw new IllegalArgumentException("There must be the same number of shares.");
    }
    List<AuthenticatedCandidate> toReturn = new ArrayList<>();
    for (int i = 0; i < xs.size(); i++) {
      toReturn.add(new AuthenticatedCandidate(xs.get(i), ys.get(i), zs.get(i)));
    }
    return toReturn;
  }


  /**
   * Represents single authenticated triple candidate (<i>[[a]]</i>, <i>[[b]]</i>, <i>[[c]]</i>).
   */
  protected static final class AuthenticatedCandidate extends TripleCandidate<AuthenticatedElement> {

    AuthenticatedCandidate(
        AuthenticatedElement left, AuthenticatedElement right, AuthenticatedElement product) {
      super(left, right, product);
    }

    MultiplicationTriple toTriple() {
      return new MultiplicationTriple(leftFactor, rightFactor, product);
    }
  }

  private static class TripleCandidate<T> {

    final T leftFactor;
    final T rightFactor;
    final T product;

    TripleCandidate(T leftFactor, T rightFactor, T product) {
      this.leftFactor = leftFactor;
      this.rightFactor = rightFactor;
      this.product = product;
    }

  }

  private static final class OpenedElement {

    private final boolean value;
    private final StrictBitVector share;

    public OpenedElement(boolean openedValue, StrictBitVector share) {
      this.value = openedValue;
      this.share = share;
    }
  }
}
