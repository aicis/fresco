package dk.alexandra.fresco.tools.bitTriples.triple;

import dk.alexandra.fresco.framework.MaliciousException;
import dk.alexandra.fresco.framework.builder.numeric.field.FieldElement;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.ByteArrayHelper;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.bitTriples.BitTripleResourcePool;
import dk.alexandra.fresco.tools.bitTriples.bracket.Bracket;
import dk.alexandra.fresco.tools.bitTriples.bracket.MacCheckShares;
import dk.alexandra.fresco.tools.bitTriples.cointossing.CoinTossingMpc;
import dk.alexandra.fresco.tools.bitTriples.cote.CoteInstances;
import dk.alexandra.fresco.tools.bitTriples.field.AuthenticatedElement;
import dk.alexandra.fresco.tools.bitTriples.field.FieldElementUtils;
import dk.alexandra.fresco.tools.bitTriples.field.MultiplicationTriple;
import dk.alexandra.fresco.tools.bitTriples.prg.BytePrg;
import dk.alexandra.fresco.tools.bitTriples.utils.VectorOperations;
import dk.alexandra.fresco.tools.ot.otextension.CoteFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
  private final FieldElementUtils fieldElementUtils;
  private final Network network;
  private final StrictBitVector macLeft;
  private final StrictBitVector macRight;
  private final StrictBitVector macConcat;
  private final Bracket bracket;
  private final CoteInstances coTeInstances;
  private final int kappa;
  private final BytePrg jointSampler;
  private Map<Integer, List<StrictBitVector>> ts;
  private Map<Integer, List<StrictBitVector>> qs;

  /** Creates new triple generation protocol. */
  public TripleGeneration(BitTripleResourcePool resourcePool, Network network, int kappa, BytePrg jointSampler) {
    this.resourcePool = resourcePool;
    this.fieldElementUtils = new FieldElementUtils(resourcePool.getFieldDefinition());
    this.network = network;
    this.kappa = kappa;
    this.jointSampler = jointSampler;
    ts = new HashMap<>();
    qs = new HashMap<>();

    // Step 1 of Initialization
    macLeft = new StrictBitVector(kappa, resourcePool.getRandomGenerator());
    macRight = new StrictBitVector(kappa, resourcePool.getRandomGenerator());
    macConcat = StrictBitVector.concat(macLeft, macRight);
    // Step 2 of Initialization
    this.coTeInstances = new CoteInstances(resourcePool, network, macConcat);
    this.bracket = new Bracket(resourcePool, network, macRight, jointSampler);
    // Step 3 of Initialization - Check consistency of macs
    new Bracket(resourcePool, network, macConcat, jointSampler).input(kappa * 2);
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
    StrictBitVector input = extend(noOfTriples);

    Pair<StrictBitVector, StrictBitVector> yz = generateTriples(noOfTriples, input);
    // use el-gen to input candidates and combine them to the authenticated candidates
    List<StrictBitVector> xShares = authenticate();
    List<StrictBitVector> yShares = bracket.input(yz.getFirst());
    List<StrictBitVector> zShares = bracket.input(yz.getSecond());

    List<AuthenticatedCandidate> authenticatedCandidate =
        toAuthenticatedCandidate(
            toAuthenticatedElement(xShares, input),
            toAuthenticatedElement(yShares, yz.getFirst()),
            toAuthenticatedElement(zShares, yz.getSecond()));
    // for each candidate, run sacrifice and get valid triple
    return checkTriples(authenticatedCandidate);
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
      toReturn.add(new AuthenticatedCandidate(xs.get(i),ys.get(i),zs.get(i)));
    }
    return toReturn;
  }

  private List<StrictBitVector> authenticate() {
    List<List<StrictBitVector>> collect = new ArrayList<>();
    for (int j = 0; j < resourcePool.getNoOfParties(); j++) {
      if (j != resourcePool.getMyId()) {
        List<StrictBitVector> lastKappaOfTs = ts.get(j);
        lastKappaOfTs.forEach(
            t ->
                new StrictBitVector(
                    Arrays.copyOfRange(t.toByteArray().clone(), kappa + 1, kappa * 2)));
        List<StrictBitVector> lastKappaOfQs = qs.get(j);
        lastKappaOfQs.forEach(
            t ->
                new StrictBitVector(
                    Arrays.copyOfRange(t.toByteArray().clone(), kappa + 1, kappa * 2)));

        collect.add(lastKappaOfTs);
        collect.add(lastKappaOfQs);
      }
    }
    return VectorOperations.xorMatchingIndices(collect, kappa);
  }

  private Pair<StrictBitVector, StrictBitVector> generateTriples(
      int noOfTriples, StrictBitVector input) {
    // Step 1
    StrictBitVector ys = new StrictBitVector(noOfTriples, resourcePool.getRandomGenerator());
    // Step 2a
    Map<Integer, StrictBitVector> ws = breakCorrelation(ts, null);
    Map<Integer, StrictBitVector> v0s = breakCorrelation(qs, null);
    Map<Integer, StrictBitVector> v1s = breakCorrelation(qs, macLeft);
    // Step 2b
    List<StrictBitVector> ns = createNewCorrelations(ys, v0s, v1s, ws, input);
    // Step 3
    StrictBitVector zs = computeZs(ns, input, ys, v0s);

    return new Pair<>(ys, zs);
  }

  private StrictBitVector computeZs(
      List<StrictBitVector> ns,
      StrictBitVector input,
      StrictBitVector ys,
      Map<Integer, StrictBitVector> v0s) {
    StrictBitVector sumOfNs = VectorOperations.bitwiseXor(ns);
    StrictBitVector sumOfV0s =
        VectorOperations.bitwiseXor(
            VectorOperations.mapToList(v0s, resourcePool.getNoOfParties(), resourcePool.getMyId()));
    sumOfNs.xor(VectorOperations.bitwiseAnd(input, ys));
    sumOfNs.xor(sumOfV0s);
    return sumOfNs;
  }

  private List<StrictBitVector> createNewCorrelations(
      StrictBitVector ys,
      Map<Integer, StrictBitVector> v0s,
      Map<Integer, StrictBitVector> v1s,
      Map<Integer, StrictBitVector> ws,
      StrictBitVector input) {
    Map<Integer, StrictBitVector> correlatedVectors = new HashMap<>();
    for (int j = 0; j < resourcePool.getNoOfParties(); j++) {
      if (resourcePool.getMyId() != j) {
        StrictBitVector s = new StrictBitVector(ys.toByteArray().clone());
        s.xor(v0s.get(j));
        s.xor(v1s.get(j));
        correlatedVectors.put(j, s);
      }
    }
    HashMap<Integer, StrictBitVector> comms = new HashMap<>();
    for (int sender = 0; sender < resourcePool.getNoOfParties(); sender++) {
      for (int receiver = 0; receiver < resourcePool.getNoOfParties(); receiver++) {
        if (sender != receiver) {
          if (resourcePool.getMyId() == sender) {
            network.send(
                receiver,
                resourcePool
                    .getStrictBitVectorSerializer()
                    .serialize(correlatedVectors.get(receiver)));
          } else if (resourcePool.getMyId() == receiver) {
            comms.put(
                sender,
                resourcePool.getStrictBitVectorSerializer().deserialize(network.receive(sender)));
          }
        }
      }
    }

    List<StrictBitVector> ns = new ArrayList<>();
    for (int j = 0; j < resourcePool.getNoOfParties(); j++) {
      if (resourcePool.getMyId() != j) {
        StrictBitVector nj = new StrictBitVector(ws.get(j).toByteArray().clone());
        nj.xor(VectorOperations.bitwiseAnd(input, comms.get(j)));
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
      for (int j = 0; j < toBreak.size(); j++) {
        if (resourcePool.getMyId() != j) {
          // For each party
          List<StrictBitVector> listForPartyJ = toBreak.get(j);
          StrictBitVector resultingVector = new StrictBitVector(kappa);
          for (int h = 0; h < listForPartyJ.size(); h++) {
            // For each vector, hash to single bit.
            StrictBitVector toHash =
                new StrictBitVector(
                    Arrays.copyOfRange(listForPartyJ.get(j).toByteArray().clone(), 0, kappa));
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
    StrictBitVector input =
        new StrictBitVector(numberOfInputBits, resourcePool.getRandomGenerator());
    for (int receiver = 0; receiver < network.getNoOfParties(); receiver++) {
      for (int sender = 0; sender < network.getNoOfParties(); sender++) {
        if (receiver != sender) {
          CoteFactory instance = coTeInstances.get(receiver, sender);
          if (resourcePool.getMyId() == receiver) {
            ts.put(sender, instance.getReceiver().extend(input));
          } else if (resourcePool.getMyId() == sender) {
            qs.put(receiver, instance.getSender().extend(numberOfInputBits));
          }
        }
      }
    }
    return input;
  }

  /** Implements sub-protocol CheckTriples of Figure 23 */
  private List<MultiplicationTriple> checkTriples(List<AuthenticatedCandidate> candidates) {
    List<AuthenticatedElement> openedElements = new ArrayList<>();
    List<Boolean> openedTo = new ArrayList<>();
    // Phase 1
    int c = 3;
    List<AuthenticatedCandidate> cutCandidates =
        cutAndChoose(candidates, c, openedElements);
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

  private void checkOpenedValues(List<AuthenticatedElement> openedElements) {
    StrictBitVector publicValues = new StrictBitVector(openedElements.size());
    List<StrictBitVector> macShares = new ArrayList<>();
    for(int i = 0; i < openedElements.size(); i++){
      publicValues.setBit(i,openedElements.get(i).getBit());
      macShares.add(openedElements.get(i).getMac());
    }
    new MacCheckShares(resourcePool,network, jointSampler).check(publicValues,macShares,macLeft);
  }

  private List<AuthenticatedCandidate> combineBuckets(
      List<AuthenticatedCandidate> candidates,
      int noOfBuckets,
      List<AuthenticatedElement> openedElements) {
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
      List<AuthenticatedCandidate> bucket,
      List<AuthenticatedElement> openedElements) {
    AuthenticatedCandidate firstCandidate = bucket.remove(0);
    return recursivelyCombine(firstCandidate, bucket, openedElements);
  }

  private AuthenticatedCandidate recursivelyCombine(
      AuthenticatedCandidate accumulatedCombination,
      List<AuthenticatedCandidate> bucket,
      List<AuthenticatedElement> openedElements) {
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
      List<AuthenticatedElement> openedElements) {
    AuthenticatedElement ys = left.rightFactor.xor(right.rightFactor);
    boolean openedYs = openElement(ys);
    AuthenticatedElement xNew = left.leftFactor.xor(right.leftFactor);
    AuthenticatedElement zNew =
        left.product.xor(right.product).xor(right.leftFactor.and(openedYs));
    openedElements.add(ys);
    return new AuthenticatedCandidate(xNew, left.rightFactor, zNew);
  }

  private List<AuthenticatedCandidate> bucketSacrifice(
      List<AuthenticatedCandidate> candidates,
      int noOfBuckets,
      List<AuthenticatedElement> openedElements) {
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
    CoinTossingMpc coinTossingMpc = new CoinTossingMpc(resourcePool, network);
    SecureRandom random =
        new SecureRandom(
            coinTossingMpc.generateJointSeed(resourcePool.getPrgSeedBitLength()).toByteArray());
    Collections.shuffle(candidates, random);
    return candidates;
  }

  /** Implements batched version of Sacrifice sub-protocol of Protocol 4. */
  private AuthenticatedCandidate sacrificeBucket(
      List<AuthenticatedCandidate> candidates,
      AuthenticatedCandidate bucketHead,
      List<AuthenticatedElement> openedElements) {
    for (AuthenticatedCandidate candidate : candidates) {
      if (!checkR(bucketHead, candidate, openedElements)) {
        throw new MaliciousException("Verification of bucket head failed");
      }
    }
    return bucketHead;
  }

  /**
   *  a=[x_i]+[x_j] og b=[y_i]+[y_j]
   *  c = [z_j]+[z_i]+[x_i]*b+[y_i]*a+a*b
   *  check om c = 0
   * @param bucketHead
   * @param candidate
   * @param openedElements
   * @return
   */

  private boolean checkR(
      AuthenticatedCandidate bucketHead,
      AuthenticatedCandidate candidate,
      List<AuthenticatedElement> openedElements) {
    AuthenticatedElement a = bucketHead.leftFactor.xor(candidate.leftFactor);
    boolean aOpen = openElement(a); // x_i + x_j
    AuthenticatedElement b = bucketHead.rightFactor.xor(candidate.rightFactor); // y_i + y_j
    boolean bOpen = openElement(b);
    AuthenticatedElement c =
        bucketHead
            .product
            .xor(candidate.product)
            .xor(bucketHead.leftFactor.and(bOpen))
            .xor(bucketHead.rightFactor.and(aOpen));
    openedElements.add(a);
    openedElements.add(b);
    openedElements.add(c);
    return c.getBit() == aOpen && bOpen;
  }

  private boolean openElement(AuthenticatedElement a) {
    StrictBitVector toSend = new StrictBitVector(8);
    toSend.setBit(0,a.getBit());
    List<StrictBitVector> received = VectorOperations.distributeVector(toSend, resourcePool,network);
    return VectorOperations.bitwiseXor(received).getBit(0);
  }

  private List<AuthenticatedCandidate> cutAndChoose(
      List<AuthenticatedCandidate> candidates,
      int c,
      List<AuthenticatedElement> openedElements) {
    CoinTossingMpc coinTossingMpc = new CoinTossingMpc(resourcePool, network);
    SecureRandom random =
        new SecureRandom(
            coinTossingMpc.generateJointSeed(resourcePool.getPrgSeedBitLength()).toByteArray());

    StrictBitVector randomBitVector = generateByteVector(random, c, candidates.size());
    List<AuthenticatedCandidate> candidatesToCheck = new ArrayList<>();
    List<AuthenticatedCandidate> unopenedCandidates = new ArrayList<>();
    for (int i = 0; i < randomBitVector.getSize(); i++) {
      if (randomBitVector.getBit(i)) {
        candidatesToCheck.add(candidates.get(i));
      } else {
        unopenedCandidates.add(candidates.get(i));
      }
    }
    if(checkMultiplicationPredicate(candidatesToCheck,openedElements)){
      throw new MaliciousException("Aborting - multiplication predicate was not satisfied");
    }
    return unopenedCandidates;
  }

  /**
   * Generates a StrictBitVector with exactly c 1's, and rest 0's
   *
   * @param random randomness
   * @param c number of 1's.
   * @param size size of .
   * @return
   */
  private StrictBitVector generateByteVector(SecureRandom random, int c, int size) {
    StrictBitVector strictBitVector = new StrictBitVector(size);
    return setBits(strictBitVector, random, c);
  }

  /**
   * Runs through the given vector, and sets c bits, that have previously not been set.
   *
   * @param vector vector
   * @param random random
   * @param c number of bits to be set
   * @return The bitvector with c new bits set.
   */
  private StrictBitVector setBits(StrictBitVector vector, SecureRandom random, int c) {
    if (c <= 0) {
      return vector;
    }
    int index = random.nextInt(vector.getSize());
    if (vector.getBit(index)) {
      return setBits(vector, random, c);
    } else {
      vector.setBit(index, true);
      return setBits(vector, random, c - 1);
    }
  }

  private boolean checkMultiplicationPredicate(
      List<AuthenticatedCandidate> candidates,
      List<AuthenticatedElement> openedElements) {
    StrictBitVector xs = new StrictBitVector(candidates.size());
    StrictBitVector ys = new StrictBitVector(candidates.size());
    StrictBitVector zs = new StrictBitVector(candidates.size());
    for(int i = 0; i < candidates.size(); i++){
      xs.setBit(i,candidates.get(i).leftFactor.getBit());
      ys.setBit(i,candidates.get(i).rightFactor.getBit());
      zs.setBit(i,candidates.get(i).product.getBit());
      openedElements.add(candidates.get(i).leftFactor);
      openedElements.add(candidates.get(i).rightFactor);
      openedElements.add(candidates.get(i).product);
    }
    xs.xor(VectorOperations.bitwiseXor(VectorOperations.distributeVector(xs,resourcePool,network)));
    ys.xor(VectorOperations.bitwiseXor(VectorOperations.distributeVector(ys,resourcePool,network)));
    zs.xor(VectorOperations.bitwiseXor(VectorOperations.distributeVector(zs,resourcePool,network)));

    return VectorOperations.bitwiseAnd(xs,ys).equals(zs);
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
   * Represents single unauthenticated multiplication triple.
   *
   * <p>Contains a single left factor group <b>a</b>, single right factor <i>b</i>, and product
   * group <b>c</b>. An unauthenticated triple will go into the Combine sub-protocol of Protocol 4.
   */
  private final class UnauthenticatedTriple {

    private final List<FieldElement> leftFactors;
    private final FieldElement rightFactor;
    private final List<FieldElement> product;

    UnauthenticatedTriple(
        List<FieldElement> leftFactors, FieldElement rightFactor, List<FieldElement> product) {
      super();
      this.leftFactors = leftFactors;
      this.rightFactor = rightFactor;
      this.product = product;
    }

    UnauthenticatedCandidate toCandidate(
        List<FieldElement> masks, List<FieldElement> sacrificeMasks) {
      FieldElement left = fieldElementUtils.innerProduct(leftFactors, masks);
      FieldElement prod = fieldElementUtils.innerProduct(product, masks);
      return new UnauthenticatedCandidate(left, rightFactor, prod);
    }
  }

  /**
   * Represents single unauthenticated triple candidate (<i>a</i>, <i>b</i>, <i>c</i>, <i>a'</i>,
   * <i>c'</i>).
   *
   * <p>An unauthenticated triple candidate is the input to the authentication sub-protocol of
   * Protocol 4.
   */
  private final class UnauthenticatedCandidate extends TripleCandidate<FieldElement> {

    UnauthenticatedCandidate(
        FieldElement leftFactor, FieldElement rightFactor, FieldElement product) {
      super(leftFactor, rightFactor, product);
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
}
