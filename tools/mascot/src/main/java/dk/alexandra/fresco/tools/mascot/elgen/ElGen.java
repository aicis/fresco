package dk.alexandra.fresco.tools.mascot.elgen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import dk.alexandra.fresco.tools.mascot.MascotContext;
import dk.alexandra.fresco.tools.mascot.MultiPartyProtocol;
import dk.alexandra.fresco.tools.mascot.arithm.CollectionUtils;
import dk.alexandra.fresco.tools.mascot.cope.CopeInputter;
import dk.alexandra.fresco.tools.mascot.cope.CopeSigner;
import dk.alexandra.fresco.tools.mascot.field.AuthenticatedElement;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import dk.alexandra.fresco.tools.mascot.field.FieldElementCollectionUtils;
import dk.alexandra.fresco.tools.mascot.field.FieldElementSerializer;
import dk.alexandra.fresco.tools.mascot.maccheck.MacCheck;
import dk.alexandra.fresco.tools.mascot.utils.Sharer;
import dk.alexandra.fresco.tools.mascot.utils.sample.DummySampler;
import dk.alexandra.fresco.tools.mascot.utils.sample.Sampler;

public class ElGen extends MultiPartyProtocol {

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
    // TODO parallelize

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
    List<List<FieldElement>> tilted = FieldElementCollectionUtils.transpose(singedByAll);
    Stream<FieldElement> combinedMacs = tilted.stream()
        .map(l -> CollectionUtils.sum(l));
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
    FieldElement maskedMac = CollectionUtils.sum(mask(macs, masks));
    // perform mac-check on open masked value
    try {
      macChecker.check(value, macKeyShare, maskedMac);
    } catch (Exception e) {
      // TODO handle
      e.printStackTrace();
    }
  }

  void sendShares(Integer partyId, List<FieldElement> shares) {
    network.send(partyId, FieldElementSerializer.serialize(shares));
  }

  List<FieldElement> secretShare(List<FieldElement> values, int numShares) {
    List<List<FieldElement>> allShares = values.stream()
        .map(value -> sharer.additiveShare(value, numShares))
        .collect(Collectors.toList());
    List<List<FieldElement>> byParty = FieldElementCollectionUtils.transpose(allShares);
    for (Integer partyId : partyIds) {
      // send shares to everyone but self
      if (!partyId.equals(myId)) {
        // assume party ids go from 1...n
        List<FieldElement> shares = byParty.get(partyId - 1);
        sendShares(partyId, shares);
      }
    }
    // return own shares
    return byParty.get(myId - 1);
  }

  List<FieldElement> receiveShares(Integer inputterId, int numElements) {
    List<FieldElement> receivedShares =
        FieldElementSerializer.deserializeList(network.receive(inputterId));
    return receivedShares;
  }

  List<AuthenticatedElement> toAuthenticatedElements(List<FieldElement> shares,
      List<FieldElement> macs) {
    if (shares.size() != macs.size()) {
      throw new IllegalArgumentException("Number of shares must equal number of mac shares");
    }
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

    // add extra random element which will later be used to mask inputs
    FieldElement extraElement = sampler.sample(modulus, modBitLength);
    values.add(extraElement);

    // compute per element mac share
    List<FieldElement> macs = macValues(values);

    // generate masks for values and macs
    List<FieldElement> masks = sampler.jointSample(modulus, modBitLength, values.size());

    // mask and combine values
    FieldElement maskedValue = CollectionUtils.sum(mask(values, masks));

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

  /**
   * Runs mac-check on opened values.
   * 
   * @param sharesWithMacs
   * @param openValues
   */
  public void check(List<AuthenticatedElement> sharesWithMacs, List<FieldElement> openValues) {
    List<FieldElement> masks = sampler.jointSample(modulus, modBitLength, sharesWithMacs.size());
    List<FieldElement> macs = sharesWithMacs.stream()
        .map(share -> share.getMac())
        .collect(Collectors.toList());
    FieldElement mac = FieldElementCollectionUtils.innerProduct(macs, masks);
    FieldElement open = FieldElementCollectionUtils.innerProduct(openValues, masks);
    try {
      macChecker.check(open, macKeyShare, mac);
    } catch (Exception e) {
      // TODO: handle exception
      e.printStackTrace();
    }
  }

  public List<FieldElement> open(List<AuthenticatedElement> closed) {
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
    return CollectionUtils.pairWiseSum(shares);
  }

}
