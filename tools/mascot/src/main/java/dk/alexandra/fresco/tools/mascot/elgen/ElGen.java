package dk.alexandra.fresco.tools.mascot.elgen;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.tools.mascot.BaseProtocol;
import dk.alexandra.fresco.tools.mascot.MascotContext;
import dk.alexandra.fresco.tools.mascot.cope.CopeInputter;
import dk.alexandra.fresco.tools.mascot.cope.CopeSigner;
import dk.alexandra.fresco.tools.mascot.field.AuthenticatedElement;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import dk.alexandra.fresco.tools.mascot.field.FieldElementSerializer;
import dk.alexandra.fresco.tools.mascot.maccheck.MacCheck;
import dk.alexandra.fresco.tools.mascot.utils.BatchArithmetic;
import dk.alexandra.fresco.tools.mascot.utils.Sharer;
import dk.alexandra.fresco.tools.mascot.utils.sample.DummySampler;
import dk.alexandra.fresco.tools.mascot.utils.sample.Sampler;

public class ElGen extends BaseProtocol {

  private MacCheck macChecker;
  private FieldElement macKeyShare;
  private boolean initialized;
  private Sampler sampler;
  private Sharer sharer;
  private Map<Integer, CopeSigner> copeSigners;
  private List<CopeInputter> copeInputters;

  public ElGen(MascotContext ctx, FieldElement macKeyShare) {
    super(ctx);
    this.macChecker = new MacCheck(ctx);
    this.macKeyShare = macKeyShare;
    this.sampler = new DummySampler(ctx.getRand());
    this.sharer = new Sharer(sampler);
    this.copeSigners = new HashMap<>();
    this.copeInputters = new LinkedList<>();
    for (Integer partyId : ctx.getPartyIds()) {
      if (!ctx.getMyId()
          .equals(partyId)) {
        copeSigners.put(partyId, new CopeSigner(ctx, partyId, this.macKeyShare));
        copeInputters.add(new CopeInputter(ctx, partyId));
      }
    }
    this.initialized = false;
  }

  public void initialize() {
    // shouldn't initialize again
    if (initialized) {
      throw new IllegalStateException("Already initialized");
    }
    // TODO run these in parallel
    // this will dead-lock if the order of initialization is switched, since cope signers block on
    // receiving in current network implementation

    // initialize cope inputters
    for (CopeInputter copeInputter : copeInputters) {
      copeInputter.initialize();
    }
    // initialize cope signers
    for (CopeSigner copeSigner : copeSigners.values()) {
      copeSigner.initialize();
    }
    this.initialized = true;
  }

  List<List<FieldElement>> otherPartiesMac(List<FieldElement> values) {
    List<List<FieldElement>> perPartySignatures = new ArrayList<>();
    for (CopeInputter copeInputter : copeInputters) {
      perPartySignatures.add(copeInputter.extend(values));
    }
    return perPartySignatures;
  }

  List<FieldElement> selfMac(List<FieldElement> values) {
    return values.stream()
        .map(value -> value.multiply(macKeyShare))
        .collect(Collectors.toList());
  }

  List<FieldElement> combineIntoMacShares(List<List<FieldElement>> singedByAll) {
    List<List<FieldElement>> tilted = ElGenUtils.transpose(singedByAll);
    Stream<FieldElement> combinedMacs = tilted.stream()
        .map(l -> FieldElement.sum(l));
    return combinedMacs.collect(Collectors.toList());
  }

  List<FieldElement> macValues(List<FieldElement> values) {
    List<FieldElement> selfMacced = selfMac(values);
    List<List<FieldElement>> maccedByAll = otherPartiesMac(values);
    maccedByAll.add(selfMacced);
    return combineIntoMacShares(maccedByAll);
  }

  List<FieldElement> mask(List<FieldElement> values, List<FieldElement> masks) {
    if (values.size() != masks.size()) {
      throw new IllegalArgumentException("Number of values must equal number of masks");
    }
    return IntStream.range(0, values.size())
        .mapToObj(idx -> {
          FieldElement value = values.get(idx);
          FieldElement mask = masks.get(idx);
          return value.multiply(mask);
        })
        .collect(Collectors.toList());
  }

  void runMacCheck(FieldElement value, List<FieldElement> masks, List<FieldElement> macs) {
    // mask and combine macs
    FieldElement maskedMac = FieldElement.sum(mask(macs, masks));
    // perform mac-check on open masked value
    try {
      macChecker.check(value, macKeyShare, maskedMac);
    } catch (Exception e) {
      // TODO handle
      e.printStackTrace();
    }
  }

  void sendShares(Integer partyId, List<FieldElement> shares) {
    Network network = ctx.getNetwork();
    network.send(partyId, FieldElementSerializer.serialize(shares));
  }

  List<FieldElement> secretShare(List<FieldElement> values, int numShares) {
    List<List<FieldElement>> allShares = values.stream()
        .map(value -> sharer.additiveShare(value, numShares))
        .collect(Collectors.toList());
    List<List<FieldElement>> byParty = ElGenUtils.transpose(allShares);
    for (Integer partyId : ctx.getPartyIds()) {
      // send shares to everyone but self
      if (!partyId.equals(ctx.getMyId())) {
        // assume party ids go from 1...n
        List<FieldElement> shares = byParty.get(partyId - 1);
        sendShares(partyId, shares);
      }
    }
    // return own shares
    return byParty.get(ctx.getMyId() - 1);
  }

  List<FieldElement> receiveShares(Integer inputterId, int numElements) {
    Network network = ctx.getNetwork();
    List<FieldElement> receivedShares =
        FieldElementSerializer.deserializeList(network.receive(inputterId));
    return receivedShares;
  }

  List<AuthenticatedElement> toAuthenticatedElements(List<FieldElement> shares,
      List<FieldElement> macs) {
    if (shares.size() != macs.size()) {
      throw new IllegalArgumentException("Number of shares must equal number of mac shares");
    }
    BigInteger modulus = ctx.getModulus();
    int modBitLength = ctx.getkBitLength();
    Stream<AuthenticatedElement> spdzElements = IntStream.range(0, shares.size())
        .mapToObj(idx -> {
          FieldElement share = shares.get(idx);
          FieldElement mac = macs.get(idx);
          return new AuthenticatedElement(share, mac, modulus, modBitLength);
        });
    return spdzElements.collect(Collectors.toList());
  }

  public List<AuthenticatedElement> input(List<FieldElement> values) {
    // can't input before initializing
    if (!initialized) {
      throw new IllegalStateException("Need to initialize first");
    }

    // make sure we can add elements to list etc
    values = new ArrayList<>(values);

    BigInteger modulus = ctx.getModulus();
    int modBitLength = ctx.getkBitLength();
    Network network = ctx.getNetwork();
    List<Integer> partyIds = ctx.getPartyIds();

    // add extra random element which will later be used to mask inputs
    FieldElement extraElement = sampler.sample(modulus, modBitLength);
    values.add(extraElement);

    // compute per element mac share
    List<FieldElement> macs = macValues(values);

    // generate masks for values and macs
    List<FieldElement> masks = sampler.jointSample(modulus, modBitLength, values.size());

    // mask and combine values
    FieldElement maskedValue = FieldElement.sum(mask(values, masks));

    // send masked value to all other parties
    network.sendToAll(maskedValue.toByteArray());

    // perform mac-check on opened value (will throw if mac check fails)
    runMacCheck(maskedValue, masks, macs);

    // inputter secret-shares input values (note that we exclude dummy element)
    List<FieldElement> toSecretShare = values.subList(0, values.size() - 1);
    List<FieldElement> shares = secretShare(toSecretShare, partyIds.size());

    // combine shares and mac shares to spdz elements (exclude mac for dummy element)
    List<FieldElement> nonDummyMacs = macs.subList(0, shares.size());
    List<AuthenticatedElement> spdzElements = toAuthenticatedElements(shares, nonDummyMacs);
    return spdzElements;
  }

  public List<AuthenticatedElement> input(Integer inputterId, int numInputs) {
    // can't input before initializing
    if (!initialized) {
      throw new IllegalStateException("Need to initialize first");
    }

    BigInteger modulus = ctx.getModulus();
    int modBitLength = ctx.getkBitLength();
    Network network = ctx.getNetwork();

    // receive per-element mac shares
    CopeSigner copeSigner = copeSigners.get(inputterId);
    List<FieldElement> macs = copeSigner.extend(numInputs + 1);

    // generate masks for macs
    List<FieldElement> masks = sampler.jointSample(modulus, modBitLength, numInputs + 1);

    // receive masked value we will use in mac-check
    FieldElement maskedValue = new FieldElement(network.receive(inputterId), modulus, modBitLength);

    // perform mac-check on opened value
    runMacCheck(maskedValue, masks, macs);

    // receive shares from inputter
    List<FieldElement> shares = receiveShares(inputterId, numInputs);

    // combine shares and mac shares to spdz elements (exclude mac for dummy element)
    List<FieldElement> nonDummyMacs = macs.subList(0, numInputs);
    List<AuthenticatedElement> spdzElements = toAuthenticatedElements(shares, nonDummyMacs);
    return spdzElements;
  }

  public void check(List<AuthenticatedElement> sharesWithMacs, List<FieldElement> openValues) {
    // TODO: mask vector not always necessary
    // BigInteger modulus = ctx.getModulus();
    // int modBitLength = ctx.getkBitLength();
    // List<FieldElement> maskVector =
    // sampler.jointSample(modulus, modBitLength, sharesWithMacs.size());
    // TODO make sure we don't need to remask vectors
    List<FieldElement> macsOnly = sharesWithMacs.stream()
        .map(share -> share.getMac())
        .collect(Collectors.toList());
    FieldElement maskedMac = FieldElement.sum(macsOnly);
    FieldElement maskedValue = FieldElement.sum(openValues);
    try {
      macChecker.check(maskedValue, macKeyShare, maskedMac);
    } catch (Exception e) {
      // TODO: handle exception
      e.printStackTrace();
    }
  }

  public List<FieldElement> open(List<AuthenticatedElement> closed) {
    List<Integer> partyIds = ctx.getPartyIds();
    Integer myId = ctx.getMyId();
    Network network = ctx.getNetwork();
    // all shares
    List<List<FieldElement>> shares = new ArrayList<>();
    // get shares from authenticated elements
    List<FieldElement> ownShares = closed.stream()
        .map(el -> el.getShare())
        .collect(Collectors.toList());
    // send own shares to others
    network.sendToAll(FieldElementSerializer.serialize(ownShares));
    // receive shares from others
    for (Integer partyId : partyIds) {
      if (!myId.equals(partyId)) {
        byte[] raw = network.receive(partyId);
        shares.add(FieldElementSerializer.deserializeList(raw));
      } else {
        shares.add(ownShares);
      }
    }
    // recombine
    return BatchArithmetic.pairWiseAddRows(shares);
  }

}
