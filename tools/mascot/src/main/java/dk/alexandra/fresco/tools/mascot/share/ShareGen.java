package dk.alexandra.fresco.tools.mascot.share;

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

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.tools.mascot.BaseProtocol;
import dk.alexandra.fresco.tools.mascot.cope.Cope;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import dk.alexandra.fresco.tools.mascot.utils.Sharer;
import dk.alexandra.fresco.tools.mascot.utils.sample.DummySampler;
import dk.alexandra.fresco.tools.mascot.utils.sample.Sampler;

public class ShareGen extends BaseProtocol {

  protected int lambdaSecurityParam;
  protected boolean initialized;
  protected FieldElement macKeyShare;
  protected Sampler sampler;
  protected Sharer sharer;
  protected Map<Integer, Cope> copeProtocols;

  public ShareGen(BigInteger modulus, int kBitLength, Integer myID, List<Integer> partyIDs,
      int lambdaSecurityParam, Network network, Random rand, ExecutorService executor) {
    super(partyIDs, myID, modulus, kBitLength, network, executor, rand);
    this.lambdaSecurityParam = lambdaSecurityParam;
    this.sampler = new DummySampler(rand);
    this.sharer = new Sharer(rand);
    this.macKeyShare = new FieldElement(new BigInteger(kBitLength, rand), modulus, kBitLength);
    this.copeProtocols = new HashMap<>();
    for (Integer partyID : partyIDs) {
      if (!myID.equals(partyID)) {
        Cope cope = new Cope(myID, partyID, kBitLength, lambdaSecurityParam, rand, macKeyShare,
            network, modulus);
        this.copeProtocols.put(partyID, cope);
      }
    }
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

  public FieldElement input(FieldElement value) {
    FieldElement x0 = sampler.sample(modulus, kBitLength);
    List<FieldElement> values = Arrays.asList(x0, value);
    int numElements = values.size();

    List<List<FieldElement>> shares = values.stream()
        .map(x -> sharer.additiveShare(x, partyIDs.size())).collect(Collectors.toList());

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
    
    FieldElement macShare = IntStream.range(0, numElements)
        .mapToObj(idx -> maskingVector.get(idx).multiply(subMacShares.get(idx)))
        .reduce(new FieldElement(BigInteger.ZERO, modulus, kBitLength),
            (left, right) -> (left.add(right)));
    
    FieldElement y = IntStream.range(0, numElements)
        .mapToObj(idx -> maskingVector.get(idx).multiply(values.get(idx)))
        .reduce(new FieldElement(BigInteger.ZERO, modulus, kBitLength),
            (left, right) -> (left.add(right)));
    
    // FieldElement macShare = macShares.get(0);
    // try {
    // BigInteger raw = new BigInteger(network.receive(0, 2));
    // FieldElement otherMacShare = new FieldElement(raw, modulus, kBitLength);
    // BigInteger rawKey = new BigInteger(network.receive(0, 2));
    // FieldElement otherKeyShare = new FieldElement(rawKey, modulus, kBitLength);
    //
    // FieldElement mac = sharer.additiveRecombine(Arrays.asList(macShare, otherMacShare));
    // FieldElement key = sharer.additiveRecombine(Arrays.asList(macKeyShare, otherKeyShare));
    //
    // System.out.println(mac);
    // System.out.println(key.multiply(x0));
    //
    // } catch (IOException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // }

    return null;
  }

  public FieldElement input(Integer inputter) {
    int numElements = 2;
    
    List<FieldElement> subMacShares = copeProtocols.get(inputter).getSigner().extend(2);
    List<FieldElement> maskingVector = sampler.jointSample(modulus, kBitLength, numElements);
    
    FieldElement macShare = IntStream.range(0, numElements)
        .mapToObj(idx -> maskingVector.get(idx).multiply(subMacShares.get(idx)))
        .reduce(new FieldElement(BigInteger.ZERO, modulus, kBitLength),
            (left, right) -> (left.add(right)));
    // try {
    // network.send(0, 1, macShares.get(0).toByteArray());
    // network.send(0, 1, macKeyShare.toByteArray());
    // } catch (IOException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // }
    return null;
  }

}
