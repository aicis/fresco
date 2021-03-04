package dk.alexandra.fresco.tools.bitTriples.triple;

import dk.alexandra.fresco.framework.MaliciousException;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.ByteArrayHelper;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.bitTriples.BitTripleResourcePool;
import dk.alexandra.fresco.tools.bitTriples.bracket.Bracket;
import dk.alexandra.fresco.tools.bitTriples.bracket.MacCheckShares;
import dk.alexandra.fresco.tools.bitTriples.cote.CoteInstances;
import dk.alexandra.fresco.tools.bitTriples.field.AuthenticatedElement;
import dk.alexandra.fresco.tools.bitTriples.field.MultiplicationTriple;
import dk.alexandra.fresco.tools.bitTriples.prg.BytePrg;
import dk.alexandra.fresco.tools.bitTriples.utils.VectorOperations;
import dk.alexandra.fresco.tools.ot.otextension.CoteFactory;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
  private final StrictBitVector macConcat;
  private final Bracket bracket;
  private final CoteInstances coTeInstances;
  private final int kappa;
  private final MacCheckShares macChecker;
  private final Map<Integer, List<StrictBitVector>> ts;
  private final Map<Integer, List<StrictBitVector>> qs;
  private final BytePrg jointSampler;
  private List<AuthenticatedCandidate> candidates;

  /** Creates new triple generation protocol. */
  public TripleGeneration(
      BitTripleResourcePool resourcePool, Network network, int kappa, BytePrg jointSampler) {
    this.resourcePool = resourcePool;
    this.network = network;
    this.kappa = kappa;
    ts = new HashMap<>();
    qs = new HashMap<>();
    this.macChecker = new MacCheckShares(resourcePool, network, jointSampler);
    this.jointSampler = jointSampler;

    // Step 1 of Initialization
    macLeft = resourcePool.getLocalSampler().getNext(kappa);
    macRight = resourcePool.getLocalSampler().getNext(kappa);
    macConcat = StrictBitVector.concat(macLeft, macRight);

    Bracket bracket = new Bracket(resourcePool, network, macConcat, jointSampler); //
    bracket.input(kappa); //
    // Step 2 of Initialization
    this.coTeInstances = bracket.getCOTeInstances();
    this.bracket = new Bracket(resourcePool, network, macRight, jointSampler);
    // Step 3 of Initialization - Check consistency of macs
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
    // Extend cote
    StrictBitVector xs = extend(noOfTriples);

    Pair<StrictBitVector, StrictBitVector> yz = generateTriples(xs);

    List<StrictBitVector> xShares = bracket.input(xs);
    List<StrictBitVector> yShares = bracket.input(yz.getFirst());
    List<StrictBitVector> zShares = bracket.input(yz.getSecond());

    List<AuthenticatedCandidate> authenticatedCandidate =
        toAuthenticatedCandidate(
            toAuthenticatedElement(xShares, xs),
            toAuthenticatedElement(yShares, yz.getFirst()),
            toAuthenticatedElement(zShares, yz.getSecond()));

    candidates = authenticatedCandidate;

    return checkTriples(authenticatedCandidate);
  }

  private void printSBV(StrictBitVector x) {
    StringBuilder toPrint = new StringBuilder();
    toPrint.append("Vector for ").append(resourcePool.getMyId()).append(": ");
    toPrint.append("[");
    for (int i = 0; i < x.getSize(); i++) {
      if (x.getBit(i, false)) {
        toPrint.append(1);
      } else {
        toPrint.append(0);
      }
    }
    toPrint.append("]");
    System.out.println(toPrint);
  }

  private void testCheckWithClosed(
      StrictBitVector xClosed, List<StrictBitVector> shares, StrictBitVector mac) {
    StrictBitVector xOpen = VectorOperations.openVector(xClosed, resourcePool, network);
    testCheck(xOpen, mac, shares);
    macChecker.check(xOpen, shares, mac);
  }

  private void testCheck(StrictBitVector xOpen, StrictBitVector mac, List<StrictBitVector> shares) {

    StrictBitVector finalMac = VectorOperations.openVector(mac, resourcePool, network);

    System.out.println("Final mac: " + finalMac);
    System.out.println("myMac mac: " + mac);

    List<StrictBitVector> distributed =
        VectorOperations.distributeShares(shares, resourcePool, network);

    System.out.println(distributed);

    printSBV(xOpen);
  }

  private List<AuthenticatedElement> toAuthenticatedElement(
      List<StrictBitVector> macs, StrictBitVector shares) {
    if (macs.size() != shares.getSize()) {
      throw new IllegalStateException("There must be the same number of shares and macs.");
    }
    List<AuthenticatedElement> toReturn = new ArrayList<>();
    for (int i = 0; i < macs.size(); i++) {
      toReturn.add(new AuthenticatedElement(shares.getBit(i, false), macs.get(i)));
    }
    return toReturn;
  }

  private List<AuthenticatedCandidate> toAuthenticatedCandidate(
      List<AuthenticatedElement> xs, List<AuthenticatedElement> ys, List<AuthenticatedElement> zs) {
    if (xs.size() != ys.size() || xs.size() != zs.size()) {
      throw new IllegalStateException("There must be the same number of shares.");
    }
    List<AuthenticatedCandidate> toReturn = new ArrayList<>();
    for (int i = 0; i < xs.size(); i++) {
      toReturn.add(new AuthenticatedCandidate(xs.get(i), ys.get(i), zs.get(i)));
    }
    return toReturn;
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

    StrictBitVector xOpen = VectorOperations.openVector(xs, resourcePool, network);
    StrictBitVector yOpen = VectorOperations.openVector(ys, resourcePool, network);
    StrictBitVector zOpen = VectorOperations.openVector(zs, resourcePool, network);

    StrictBitVector product = VectorOperations.and(xOpen, yOpen);
    boolean b = zOpen.equals(product);


    return new Pair<>(ys, zs);
  }

  private StrictBitVector computeZs(
      List<StrictBitVector> ns,
      StrictBitVector xs,
      StrictBitVector ys,
      Map<Integer, StrictBitVector> v0s) {

    StrictBitVector u = VectorOperations.xor(
        VectorOperations.sum(ns),
        VectorOperations.and(xs, ys));

    return VectorOperations.xor(u, VectorOperations.sum(VectorOperations.mapToList(v0s)));
  }

  private List<StrictBitVector> createNewCorrelations(
      StrictBitVector ys,
      StrictBitVector xs,
      Map<Integer, StrictBitVector> v0s,
      Map<Integer, StrictBitVector> v1s,
      Map<Integer, StrictBitVector> ws) {
    Map<Integer, StrictBitVector> correlatedVectors = new HashMap<>();
    if (resourcePool.getMyId() == 1) {
      System.out.println("xs");
      printSBV(xs);
      System.out.println("w");
      printSBV(ws.get(3 - resourcePool.getMyId()));
    } else {
      System.out.println("v0");
      printSBV(v0s.get(3 - resourcePool.getMyId()));
      System.out.println("v1");
      printSBV(v1s.get(3 - resourcePool.getMyId()));
    }
    for (int j = 1; j <= resourcePool.getNoOfParties(); j++) {
      if (resourcePool.getMyId() != j) {
        StrictBitVector s = VectorOperations.xor(v1s.get(j),VectorOperations.xor(ys,v0s.get(j)));
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
        StrictBitVector nj = VectorOperations.xor(VectorOperations.and(xs, sReceived.get(j)),ws.get(j));
        ns.add(nj);
      }
    }
    return ns;
  }

  private Map<Integer, StrictBitVector> breakCorrelation(
      Map<Integer, List<StrictBitVector>> toBreak, StrictBitVector mac) {
    Map<Integer, StrictBitVector> result = new HashMap<>();
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
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
            byte[] hashed = digest.digest(toHash.toByteArray());
            resultingVector.setBit(h, ByteArrayHelper.getBit(hashed, 0), false);
          }
          result.put(j, resultingVector);
        }
      }
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace(); // Shouldn't happen
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
  private List<MultiplicationTriple> checkTriples(List<AuthenticatedCandidate> candidates) {

    List<OpenedElement> openedElements = new ArrayList<>();
    // Phase 1
    int c = 8;
    List<AuthenticatedCandidate> cutCandidates = cutAndChoose(candidates, c, openedElements);
    checkOpenedValues(openedElements);
    // Phase 2
    int bucketSize =
        getBucketSize(cutCandidates.size(), resourcePool.getComputationalSecurityBitParameter());

    List<AuthenticatedCandidate> sacrificedBuckets =
        bucketSacrifice(cutCandidates, bucketSize, openedElements);

    // Phase 3
    List<AuthenticatedCandidate> checkedTriples =
        combineBuckets(sacrificedBuckets, bucketSize, openedElements);

    // Phase 4
    checkOpenedValues(openedElements);

    // convert candidates to valid triples and return
    return toMultTriples(checkedTriples);
  }

  private void checkOpenedValues(List<OpenedElement> openedElements) {
    StrictBitVector values = new StrictBitVector(openedElements.size());
    List<StrictBitVector> macShares = new ArrayList<>();
    for (int i = 0; i < openedElements.size(); i++) {
      values.setBit(i, openedElements.get(i).value, false);
      macShares.add(openedElements.get(i).share);
    }
    macChecker.check(values, macShares, macRight);
  }

  private List<AuthenticatedCandidate> combineBuckets(
      List<AuthenticatedCandidate> candidates,
      int noOfBuckets,
      List<OpenedElement> openedElements) {
    int bucketSize = candidates.size() / noOfBuckets;

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
      List<AuthenticatedCandidate> candidates,
      int noOfBuckets,
      List<OpenedElement> openedElements) {
    int bucketSize = candidates.size() / noOfBuckets;

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
    StrictBitVector v = jointSampler.getNext(resourcePool.getPrgSeedBitLength());
    Random random = new Random(new BigInteger(v.toByteArray()).intValue());
    Collections.shuffle(candidates, random);
    return candidates;
  }

  /** Implements batched version of Sacrifice sub-protocol of Protocol 4. */
  private AuthenticatedCandidate sacrificeBucket(
      List<AuthenticatedCandidate> candidates,
      AuthenticatedCandidate bucketHead,
      List<OpenedElement> openedElements) {
    System.out.println(bucketHead.leftFactor);
    System.out.println(bucketHead.rightFactor);
    System.out.println(bucketHead.product);
    for (AuthenticatedCandidate candidate : candidates) {
      if (!checkR(bucketHead, candidate, openedElements)) {
        throw new MaliciousException("Verification of bucket head failed");
      }
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
  private boolean checkR(
      AuthenticatedCandidate bucketHead,
      AuthenticatedCandidate candidate,
      List<OpenedElement> openedElements) {
    System.out.println("¤¤¤¤");
    if (resourcePool.getMyId() == 1) {
      System.out.println("im 1");
    }
    OpenedElement x1 = openElement(bucketHead.leftFactor);
    OpenedElement x2 = openElement(candidate.leftFactor);
    OpenedElement y1 = openElement(bucketHead.rightFactor);
    OpenedElement y2 = openElement(candidate.rightFactor);
    OpenedElement z1 = openElement(bucketHead.product);
    OpenedElement z2 = openElement(candidate.product);

    System.out.println(x1.value && y1.value == z1.value);
    System.out.println(x2.value && y2.value == z2.value);

    AuthenticatedElement a = bucketHead.leftFactor.xor(candidate.leftFactor);
    OpenedElement aOpen = openElement(a);
    System.out.println("a:" + aOpen.value); // x_i + x_j
    AuthenticatedElement b = bucketHead.rightFactor.xor(candidate.rightFactor); // y_i + y_j
    OpenedElement bOpen = openElement(b);
    System.out.println("b:" + bOpen.value); // x_i + x_j
    AuthenticatedElement c =
        bucketHead
            .product
            .xor(candidate.product)
            .xor(bucketHead.leftFactor.and(bOpen.value))
            .xor(bucketHead.rightFactor.and(aOpen.value));
    openedElements.add(aOpen);
    openedElements.add(bOpen);
    System.out.println(
        resourcePool.getMyId()
            + " - Check condition: "
            + (c.getBit() == aOpen.value && bOpen.value));
    OpenedElement cOpen = openElement(c);
    return cOpen.value == aOpen.value && bOpen.value;
  }

  private OpenedElement openElement(AuthenticatedElement element) {
    System.out.println(resourcePool.getMyId() + ": " + element);
    StrictBitVector toSend = new StrictBitVector(8);
    toSend.setBit(0, element.getBit(), false);
    return new OpenedElement(
        VectorOperations.openVector(toSend, resourcePool, network).getBit(0, false),
        element.getMac());
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

    if (!multiplicationPredicateHolds(candidatesToCheck, openedElements)) {
      throw new MaliciousException("Aborting - multiplication predicates were not satisfied");
    }

    return unopenedCandidates;
  }

  private boolean multiplicationPredicateHolds(
      List<AuthenticatedCandidate> candidates, List<OpenedElement> openedElements) {
    StrictBitVector xs = new StrictBitVector(candidates.size());
    StrictBitVector ys = new StrictBitVector(candidates.size());
    StrictBitVector zs = new StrictBitVector(candidates.size());
    for (int i = 0; i < candidates.size(); i++) {
      xs.setBit(i, candidates.get(i).leftFactor.getBit(), false);
      ys.setBit(i, candidates.get(i).rightFactor.getBit(), false);
      zs.setBit(i, candidates.get(i).product.getBit(), false);
    }

    StrictBitVector xOpen = VectorOperations.openVector(xs, resourcePool, network);
    StrictBitVector yOpen = VectorOperations.openVector(ys, resourcePool, network);
    StrictBitVector zOpen = VectorOperations.openVector(zs, resourcePool, network);
    System.out.println(xOpen);
    System.out.println(yOpen);
    System.out.println(zOpen);

    for (int i = 0; i < candidates.size(); i++) {
      openedElements.add(
          new OpenedElement(xOpen.getBit(i, false), candidates.get(i).leftFactor.getMac()));
      openedElements.add(
          new OpenedElement(yOpen.getBit(i, false), candidates.get(i).rightFactor.getMac()));
      openedElements.add(
          new OpenedElement(zOpen.getBit(i, false), candidates.get(i).product.getMac()));
    }

    return VectorOperations.and(xOpen, yOpen).equals(zOpen);
  }

  private List<MultiplicationTriple> toMultTriples(List<AuthenticatedCandidate> candidates) {
    return candidates.stream().map(AuthenticatedCandidate::toTriple).collect(Collectors.toList());
  }

  private int getBucketSize(int numberOfTriples, int securityParameter) {
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

  /**
   * Represents single authenticated triple candidate (<i>[[a]]</i>, <i>[[b]]</i>, <i>[[c]]</i>,
   * <i>[[a']]</i>, <i>[[c']]</i>).
   *
   * <p>Note <i>[[a']]</i>, <i>[[c']]</i> in the Sacrifice sub-protocol of Protocol 4.
   */
  private final class AuthenticatedCandidate extends TripleCandidate<AuthenticatedElement> {

    AuthenticatedCandidate(List<AuthenticatedElement> ordered) {
      super(ordered);
    }

    AuthenticatedCandidate(
        AuthenticatedElement left, AuthenticatedElement right, AuthenticatedElement product) {
      super(left, right, product);
    }

    MultiplicationTriple toTriple() {
      return new MultiplicationTriple(leftFactor, rightFactor, product);
    }
  }

  private class TripleCandidate<T> {

    final T leftFactor;
    final T rightFactor;
    final T product;

    TripleCandidate(T leftFactor, T rightFactor, T product) {
      this.leftFactor = leftFactor;
      this.rightFactor = rightFactor;
      this.product = product;
    }

    TripleCandidate(List<T> ordered) {
      this(ordered.get(0), ordered.get(1), ordered.get(2));
    }

    Stream<T> stream() {
      return Stream.of(leftFactor, rightFactor, product);
    }
  }

  private final class OpenedElement {

    private final boolean value;
    private final StrictBitVector share;

    public OpenedElement(boolean openedValue, StrictBitVector share) {
      this.value = openedValue;
      this.share = share;
    }
  }
}
