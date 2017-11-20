package dk.alexandra.fresco.tools.mascot.share;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import dk.alexandra.fresco.suite.spdz.datatypes.SpdzElement;
import dk.alexandra.fresco.tools.mascot.MultiPartyProtocol;
import dk.alexandra.fresco.tools.mascot.cope.Cope;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import dk.alexandra.fresco.tools.mascot.maccheck.DummyMacCheck;
import dk.alexandra.fresco.tools.mascot.maccheck.MacCheck;
import dk.alexandra.fresco.tools.mascot.net.ExtendedNetwork;
import dk.alexandra.fresco.tools.mascot.utils.Sharer;
import dk.alexandra.fresco.tools.mascot.utils.sample.DummySampler;
import dk.alexandra.fresco.tools.mascot.utils.sample.Sampler;

public class ShareGen extends MultiPartyProtocol {

  protected int lambdaSecurityParam;
  protected boolean initialized;
  protected FieldElement macKeyShare;
  protected Sampler sampler;
  protected Sharer sharer;
  protected Map<Integer, Cope> copeProtocols;
  protected MacCheck macCheckProtocol;

  public ShareGen(BigInteger modulus, int kBitLength, Integer myId, List<Integer> partyIds,
      int lambdaSecurityParam, ExtendedNetwork network, Random rand, ExecutorService executor) {
    super(myId, partyIds, modulus, kBitLength, network, executor, rand);
    this.lambdaSecurityParam = lambdaSecurityParam;
    this.sampler = new DummySampler(rand);
    this.sharer = new Sharer(rand);
    this.macKeyShare = new FieldElement(new BigInteger(kBitLength, rand), modulus, kBitLength);
    this.copeProtocols = new HashMap<>();
    for (Integer partyId : partyIds) {
      if (!myId.equals(partyId)) {
        Cope cope = new Cope(myId, partyId, kBitLength, lambdaSecurityParam, rand, macKeyShare,
            network, executor, modulus);
        this.copeProtocols.put(partyId, cope);
      }
    }
    this.macCheckProtocol =
        new DummyMacCheck(myId, partyIds, modulus, kBitLength, network, executor, rand);
    this.initialized = false;
  }

  public void initialize() {
    if (initialized) {
      throw new IllegalStateException("Already initialized");
    }
    Stream<Cope> copeStream = copeProtocols.values().stream();
    CompletableFuture.allOf(copeStream.map(cope -> cope.initializeAsynch(executor))
        .toArray(i -> new CompletableFuture[i])).join();
    this.initialized = true;
  }

  public void shutdown() {
    executor.shutdown();
    try {
      executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    } catch (InterruptedException e) {
      System.out.println("broken");
    }
  }

  private List<FieldElement> getTValues(List<List<FieldElement>> tValuesPerParty, int numElements) {
    // TODO: clean up
    List<FieldElement> tValues = new ArrayList<>();
    for (int i = 0; i < numElements; i++) {
      tValues.add(new FieldElement(0, modulus, kBitLength));
    }
    for (List<FieldElement> perParty : tValuesPerParty) {
      for (int i = 0; i < perParty.size(); i++) {
        tValues.set(i, tValues.get(i).add(perParty.get(i)));
      }
    }
    return tValues;
  }

  public SpdzElement input(FieldElement value) throws IOException {
    // TODO: throw if not initialized
    FieldElement x0 = sampler.sample(modulus, kBitLength);
    List<FieldElement> values = Arrays.asList(x0, value);
    int numElements = values.size();

    // TODO: wrap this
    List<List<FieldElement>> tValuesPerParty = new ArrayList<>();
    for (Cope cope : copeProtocols.values()) {
      tValuesPerParty.add(cope.getInputter().extend(values));
    }
    List<FieldElement> tValues = getTValues(tValuesPerParty, numElements);
    List<FieldElement> subMacShares = IntStream.range(0, numElements)
        .mapToObj(idx -> values.get(idx).multiply(macKeyShare).add(tValues.get(idx)))
        .collect(Collectors.toList());

    List<FieldElement> maskingVector = sampler.jointSample(modulus, kBitLength, numElements);

    FieldElement maskedMacShare = IntStream.range(0, numElements)
        .mapToObj(idx -> maskingVector.get(idx).multiply(subMacShares.get(idx)))
        .reduce(new FieldElement(BigInteger.ZERO, modulus, kBitLength),
            (left, right) -> (left.add(right)));

    FieldElement y = IntStream.range(0, numElements)
        .mapToObj(idx -> maskingVector.get(idx).multiply(values.get(idx)))
        .reduce(new FieldElement(BigInteger.ZERO, modulus, kBitLength),
            (left, right) -> (left.add(right)));

    network.sendToAll(y.toByteArray());
    // TODO: handle mac failure
    macCheckProtocol.check(y, macKeyShare, maskedMacShare);

    List<FieldElement> shares = sharer.additiveShare(value, partyIds.size());

    for (Integer partyId : partyIds) {
      if (!myId.equals(partyId)) {
        network.send(partyId, shares.get(partyId - 1).toByteArray());
      }
    }

    FieldElement share = shares.get(myId - 1);
    FieldElement macShare = subMacShares.get(1);
    return FieldElement.toSpdzElement(share, macShare);
  }

  public SpdzElement input(Integer inputter) throws IOException {
    int numElements = 2; // one real input and one dummy element

    List<FieldElement> subMacShares = copeProtocols.get(inputter).getSigner().extend(2);
    List<FieldElement> maskingVector = sampler.jointSample(modulus, kBitLength, numElements);

    FieldElement maksedMacShare = IntStream.range(0, numElements)
        .mapToObj(idx -> maskingVector.get(idx).multiply(subMacShares.get(idx)))
        .reduce(new FieldElement(BigInteger.ZERO, modulus, kBitLength),
            (left, right) -> (left.add(right)));

    FieldElement y = new FieldElement(network.receive(inputter), modulus, kBitLength);
    // TODO: handle mac failure
    macCheckProtocol.check(y, macKeyShare, maksedMacShare);
    FieldElement share = new FieldElement(network.receive(inputter), modulus, kBitLength);
    FieldElement macShare = subMacShares.get(1);
    return FieldElement.toSpdzElement(share, macShare);
  }

  public SpdzElement linearComb(List<SpdzElement> shares, List<FieldElement> scalars,
      FieldElement constant) {
    SpdzElement combined = IntStream.range(0, shares.size())
        .mapToObj(idx -> shares.get(idx).multiply(scalars.get(idx).toBigInteger()))
        .reduce((l, r) -> l.add(r)).get();

    FieldElement macOnConstant = constant.multiply(macKeyShare);
    SpdzElement constantWithMac =
        new SpdzElement(constant.toBigInteger(), macOnConstant.toBigInteger(), modulus);

    combined.add(constantWithMac, myId);
    return combined;
  }

  public FieldElement open(SpdzElement share) throws IOException {
    List<FieldElement> shares = new ArrayList<>();
    // TODO: parallelize receive
    for (Integer partyId : partyIds) {
      if (partyId.equals(myId)) {
        shares.add(new FieldElement(share.getShare(), modulus, kBitLength));
        network.sendToAll(share.getShare().toByteArray());
      } else {
        byte[] received = network.receive(partyId);
        shares.add(new FieldElement(received, modulus, kBitLength));
      }
    }
    return sharer.additiveRecombine(shares);
  }

  public void check(List<SpdzElement> sharesWithMacs, List<FieldElement> openValues) {
    // TODO: mask vector not always necessary
    List<FieldElement> maskVector = sampler.jointSample(modulus, kBitLength, sharesWithMacs.size());    
    List<FieldElement> macsOnly = sharesWithMacs.stream()
        .map(share -> new FieldElement(share.getMac(), modulus, kBitLength))
        .collect(Collectors.toList());
    FieldElement maskedMac = FieldElement.innerProduct(maskVector, macsOnly);
    FieldElement maskedValue = FieldElement.innerProduct(maskVector, openValues);
    macCheckProtocol.check(maskedValue, macKeyShare, maskedMac);
  }

}
