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
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.tools.mascot.cope.COPE;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import dk.alexandra.fresco.tools.mascot.utils.Sampler;
import dk.alexandra.fresco.tools.mascot.utils.Sharer;

public class ShareGen {

  // TODO: probably just want ResourcePool
  protected BigInteger modulus;
  protected int kBitLength;
  protected Integer myID;
  protected List<Integer> partyIDs;
  protected int lambdaSecurityParam;
  protected Network network;
  protected Map<Integer, COPE> copeProtocols;
  protected boolean initialized;
  protected FieldElement macKeyShare;
  protected Random rand;
  protected Sampler sampler;
  protected Sharer sharer;
  protected ExecutorService executor;

  public ShareGen(BigInteger modulus, int kBitLength, Integer myID, List<Integer> partyIDs,
      int lambdaSecurityParam, Network network, Random rand) {
    super();
    this.modulus = modulus;
    this.kBitLength = kBitLength;
    this.myID = myID;
    this.partyIDs = partyIDs;
    this.lambdaSecurityParam = lambdaSecurityParam;
    this.network = network;
    this.rand = rand;
    this.sampler = new Sampler(rand);
    this.sharer = new Sharer(rand);
    this.macKeyShare = new FieldElement(new BigInteger(kBitLength, rand), modulus, kBitLength);
    this.copeProtocols = new HashMap<>();
    for (Integer partyID : partyIDs) {
      if (!myID.equals(partyID)) {
        COPE cope = new COPE(myID, partyID, kBitLength, lambdaSecurityParam, rand, macKeyShare,
            network, modulus);
        this.copeProtocols.put(partyID, cope);
      }
    }
    this.executor = Executors.newCachedThreadPool();
    this.initialized = false;
  }

  public void initialize() {
    if (initialized) {
      throw new IllegalStateException("Already initialized");
    }
    Stream<COPE> copeStream = copeProtocols.values().stream();
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

  public FieldElement input(FieldElement value) {
    FieldElement x0 = sampler.sample(modulus, kBitLength);
    List<FieldElement> values = Arrays.asList(x0, value);
    int numElements = values.size();

    List<List<FieldElement>> shares = values.stream()
        .map(x -> sharer.additiveShare(x, partyIDs.size())).collect(Collectors.toList());

    // TODO: wrap this
    List<List<FieldElement>> tValuesPerParty = new ArrayList<>();
    for (COPE cope : copeProtocols.values()) {
      tValuesPerParty.add(cope.getInputter().extend(values));
    }
    List<FieldElement> tValues = new ArrayList<>();
    for (int i = 0; i < numElements; i++) {
      tValues.add(new FieldElement(0, modulus, kBitLength));
    }
    for (List<FieldElement> perParty : tValuesPerParty) {
      for (int i = 0; i < perParty.size(); i++) {
        tValues.set(i, tValues.get(i).add(perParty.get(i)));
      }
    }
    System.out.println(tValues);
    
    return null;
  }

  public FieldElement input(Integer inputter) {
    List<FieldElement> qValues = copeProtocols.get(inputter).getSigner().extend(2);
    System.out.println(qValues);
    return null;
  }

}
