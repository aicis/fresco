package dk.alexandra.fresco.tools.mascot.triple;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import dk.alexandra.fresco.suite.spdz.datatypes.SpdzElement;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzTriple;
import dk.alexandra.fresco.tools.mascot.MultiPartyProtocol;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import dk.alexandra.fresco.tools.mascot.mult.MultiplyBiDirectional;
import dk.alexandra.fresco.tools.mascot.net.ExtendedNetwork;
import dk.alexandra.fresco.tools.mascot.share.ShareGen;
import dk.alexandra.fresco.tools.mascot.utils.sample.DummySampler;
import dk.alexandra.fresco.tools.mascot.utils.sample.Sampler;

public class TripleGen extends MultiPartyProtocol {

  protected int numTriplesSecParam;
  protected ShareGen shareGen;
  protected Sampler sampler;

  protected List<MultiplyBiDirectional> multiplyProtocols;
  protected boolean initialized;

  public TripleGen(Integer myId, List<Integer> partyIds, BigInteger modulus, int kBitLength,
      int lambdaSecurityParam, int numTriplesSecParam, ExtendedNetwork network,
      ExecutorService executor, Random rand) {
    super(myId, partyIds, modulus, kBitLength, network, executor, rand);
    this.numTriplesSecParam = numTriplesSecParam;
    // TODO: lambdaSecurityParam should be different than kBitLenght
    this.shareGen = new ShareGen(modulus, kBitLength, myId, partyIds, lambdaSecurityParam, network,
        rand, executor);
    this.multiplyProtocols = new LinkedList<>();
    for (Integer partyId : partyIds) {
      if (!myId.equals(partyId)) {
        MultiplyBiDirectional mult = new MultiplyBiDirectional(myId, partyId, kBitLength,
            lambdaSecurityParam, numTriplesSecParam, rand, network, executor, modulus);
        this.multiplyProtocols.add(mult);
      }
    }
    this.sampler = new DummySampler(rand);
    this.initialized = false;
  }

  public void initialize() {
    if (initialized) {
      throw new IllegalStateException("Already initialized");
    }
    this.shareGen.initialize();
    this.initialized = true;
  }

  public List<FieldElement> multiply(List<FieldElement> leftFactorCandidates,
      FieldElement rightFactor) throws IOException {
    List<FieldElement> productCandidates = leftFactorCandidates.stream()
        .map(leftFactor -> leftFactor.multiply(rightFactor)).collect(Collectors.toList());

    // TODO: parallelize
    for (MultiplyBiDirectional mult : multiplyProtocols) {
      Integer otherId = mult.getOtherId();
      List<FieldElement> leftSubCandidates = null;
      List<FieldElement> rightSubCandidates = null;
      // TODO: parallelize
      if (myId < otherId) {
        leftSubCandidates = mult.multiplyLeft(leftFactorCandidates);
        rightSubCandidates = mult.multiplyRight(rightFactor);
      } else {
        rightSubCandidates = mult.multiplyRight(rightFactor);
        leftSubCandidates = mult.multiplyLeft(leftFactorCandidates);
      }
      for (int i = 0; i < numTriplesSecParam; i++) {
        productCandidates.set(i, productCandidates.get(i).add(leftSubCandidates.get(i)));
        productCandidates.set(i, productCandidates.get(i).add(rightSubCandidates.get(i)));
      }
    }
    return productCandidates;
  }

  public Combined<FieldElement> combine(List<FieldElement> leftFactors, FieldElement rightFactor,
      List<FieldElement> products) {
    List<FieldElement> r = sampler.jointSample(modulus, kBitLength, numTriplesSecParam);
    List<FieldElement> rHat = sampler.jointSample(modulus, kBitLength, numTriplesSecParam);

    FieldElement a = FieldElement.innerProduct(leftFactors, r);
    FieldElement aHat = FieldElement.innerProduct(leftFactors, rHat);
    FieldElement c = FieldElement.innerProduct(products, r);
    FieldElement cHat = FieldElement.innerProduct(products, rHat);

    return new Combined<>(a, rightFactor, c, aHat, cHat);
  }

  public Combined<SpdzElement> authenticate(Combined<FieldElement> combined) throws IOException {
    // TODO: parallelize
    List<List<SpdzElement>> subShares = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
      subShares.add(new ArrayList<>());
    }
    for (Integer partyId : partyIds) {
      if (partyId.equals(myId)) {
        List<FieldElement> els = combined.toOrderedList();
        for (int i = 0; i < els.size(); i++) {
          subShares.get(i).add(shareGen.input(els.get(i)));
        }
      } else {
        for (int i = 0; i < subShares.size(); i++) {
          subShares.get(i).add(shareGen.input(partyId));
        }
      }
    }
    List<SpdzElement> shares =
        subShares.stream().map(shareList -> shareList.stream().reduce((l, r) -> l.add(r)).get())
            .collect(Collectors.toList());
    return new Combined<>(shares);
  }

  public SpdzTriple sacrifice(Combined<SpdzElement> auth) throws IOException {
    FieldElement mask = sampler.jointSample(modulus, kBitLength);
    SpdzElement rho = auth.a.multiply(mask.toBigInteger()).subtract(auth.aHat);
    FieldElement rhoOpen = shareGen.open(rho);
    SpdzElement sigma = auth.c.multiply(mask.toBigInteger()).subtract(auth.cHat)
        .subtract(auth.b.multiply(rhoOpen.toBigInteger()));
    // technically no need to include zero element
    shareGen.check(Arrays.asList(rho, sigma),
        Arrays.asList(rhoOpen, new FieldElement(0, modulus, kBitLength)));
    return new SpdzTriple(auth.a, auth.b, auth.c);
  }

  public SpdzTriple triple() throws IOException {
    // TODO: throw if not initialized
    List<FieldElement> leftFactorCandidates =
        sampler.sample(modulus, kBitLength, numTriplesSecParam);

    FieldElement rightFactor = sampler.sample(modulus, kBitLength);

    List<FieldElement> productCandidates = multiply(leftFactorCandidates, rightFactor);

    Combined<FieldElement> combined = combine(leftFactorCandidates, rightFactor, productCandidates);

    Combined<SpdzElement> authenticated = authenticate(combined);

    SpdzTriple triple = sacrifice(authenticated);

    return triple;
  }

  private class Combined<T> {
    T a;
    T b;
    T c;
    T aHat;
    T cHat;

    public Combined(T a, T b, T c, T aHat, T cHat) {
      super();
      this.a = a;
      this.b = b;
      this.c = c;
      this.aHat = aHat;
      this.cHat = cHat;
    }

    public Combined(List<T> ordered) {
      super();
      // TODO: throw if incorrect size
      this.a = ordered.get(0);
      this.b = ordered.get(1);
      this.c = ordered.get(2);
      this.aHat = ordered.get(3);
      this.cHat = ordered.get(4);
    }

    List<T> toOrderedList() {
      return Arrays.asList(a, b, c, aHat, cHat);
    }

    @Override
    public String toString() {
      return "Combined [a=" + a + ", b=" + b + ", c=" + c + ", aHat=" + aHat + ", cHat=" + cHat
          + "]";
    }
  }
}
