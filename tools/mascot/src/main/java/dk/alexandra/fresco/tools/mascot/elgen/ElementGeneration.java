package dk.alexandra.fresco.tools.mascot.elgen;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.tools.mascot.BaseProtocol;
import dk.alexandra.fresco.tools.mascot.MascotResourcePool;
import dk.alexandra.fresco.tools.mascot.cope.CopeInputter;
import dk.alexandra.fresco.tools.mascot.cope.CopeSigner;
import dk.alexandra.fresco.tools.mascot.field.AuthenticatedElement;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import dk.alexandra.fresco.tools.mascot.maccheck.MacCheck;
import dk.alexandra.fresco.tools.mascot.utils.FieldElementPrg;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Actively-secure protocol for generating authentication, secret-shared elements based on the
 * MASCOT protocol (https://eprint.iacr.org/2016/505.pdf). <br>
 * Allows a single party to secret-share a field element among all parties such that the element is
 * authenticated via a MAC. The MAC is secret-shared among the parties, as is the MAC key.
 */
public class ElementGeneration extends BaseProtocol {

  private final MacCheck macChecker;
  private final FieldElement macKeyShare;
  private final FieldElementPrg localSampler;
  private final FieldElementPrg jointSampler;
  private final Sharer sharer;
  private final Map<Integer, CopeSigner> copeSigners;
  private final Map<Integer, CopeInputter> copeInputters;

  /**
   * Creates new {@link ElementGeneration}.
   */
  public ElementGeneration(MascotResourcePool resourcePool, Network network,
      FieldElement macKeyShare, FieldElementPrg jointSampler) {
    super(resourcePool, network);
    this.macChecker = new MacCheck(resourcePool, network);
    this.macKeyShare = macKeyShare;
    this.localSampler = resourcePool.getLocalSampler();
    this.jointSampler = jointSampler;
    this.sharer = new AdditiveSharer(localSampler, getModulus(), getModBitLength());
    this.copeSigners = new HashMap<>();
    this.copeInputters = new HashMap<>();
    for (Integer partyId : getPartyIds()) {
      if (getMyId() != partyId) {
        CopeSigner signer;
        CopeInputter inputter;
        // construction order matters since receive blocks and this is not parallelized
        if (getMyId() < partyId) {
          signer = new CopeSigner(resourcePool, network, partyId, this.macKeyShare);
          inputter = new CopeInputter(resourcePool, network, partyId);
        } else {
          inputter = new CopeInputter(resourcePool, network, partyId);
          signer = new CopeSigner(resourcePool, network, partyId, this.macKeyShare);
        }
        copeInputters.put(partyId, inputter);
        copeSigners.put(partyId, signer);
      }
    }
  }

  List<List<FieldElement>> otherPartiesMac(List<FieldElement> values) {
    List<List<FieldElement>> perPartySignatures = new ArrayList<>();
    for (CopeInputter copeInputter : copeInputters.values()) {
      perPartySignatures.add(copeInputter.extend(values));
    }
    return perPartySignatures;
  }

  List<FieldElement> selfMac(List<FieldElement> values) {
    return getFieldElementUtils().scalarMultiply(values, macKeyShare);
  }

  List<FieldElement> macValues(List<FieldElement> values) {
    List<FieldElement> selfMacced = selfMac(values);
    List<List<FieldElement>> maccedByAll = otherPartiesMac(values);
    maccedByAll.add(selfMacced);
    return getFieldElementUtils().pairwiseSum(maccedByAll);
  }

  List<FieldElement> secretShare(List<FieldElement> values, int numShares) {
    List<List<FieldElement>> allShares =
        values.stream().map(value -> sharer.share(value, numShares)).collect(Collectors.toList());
    List<List<FieldElement>> byParty = getFieldElementUtils().transpose(allShares);
    for (Integer partyId : getPartyIds()) {
      // send shares to everyone but self
      if (!partyId.equals(getMyId())) {
        // assume party ids go from 1...n
        List<FieldElement> shares = byParty.get(partyId - 1);
        getNetwork().send(partyId, getFieldElementSerializer().serialize(shares));
      }
    }
    // return own shares
    return byParty.get(getMyId() - 1);
  }

  List<AuthenticatedElement> toAuthenticatedElements(List<FieldElement> shares,
      List<FieldElement> macs) {
    Stream<AuthenticatedElement> spdzElements = IntStream.range(0, shares.size()).mapToObj(idx -> {
      FieldElement share = shares.get(idx);
      FieldElement mac = macs.get(idx);
      return new AuthenticatedElement(share, mac, getModulus(), getModBitLength());
    });
    return spdzElements.collect(Collectors.toList());
  }

  /**
   * Computes this party's authenticated shares of input. <br>
   * To be called by input party.
   *
   * @param values values to input
   * @return authenticated shares of inputs
   */
  public List<AuthenticatedElement> input(List<FieldElement> values) {
    // make sure we can add elements to list etc
    values = new ArrayList<>(values);

    // add extra random element which will later be used to mask inputs
    FieldElement extraElement = localSampler.getNext(getModulus(), getModBitLength());
    values.add(extraElement);

    // compute per element mac share
    List<FieldElement> macs = macValues(values);

    // generate masks for values and macs
    List<FieldElement> masks = jointSampler.getNext(getModulus(), getModBitLength(), values.size());

    // mask and combine values
    FieldElement maskedValue = getFieldElementUtils().innerProduct(values, masks);

    // send masked value to all other parties
    getNetwork().sendToAll(maskedValue.toByteArray());
    // so that we can use receiveFromAll correctly later
    getNetwork().receive(getMyId());

    // perform mac-check on opened value (will throw if mac check fails)
    runMacCheck(maskedValue, masks, macs);

    // inputter secret-shares input values (note that we exclude dummy element)
    List<FieldElement> toSecretShare = values.subList(0, values.size() - 1);
    List<FieldElement> shares = secretShare(toSecretShare, getPartyIds().size());

    // combine shares and mac shares to spdz elements (exclude mac for dummy element)
    List<FieldElement> nonDummyMacs = macs.subList(0, shares.size());
    List<AuthenticatedElement> spdzElements = toAuthenticatedElements(shares, nonDummyMacs);
    return spdzElements;
  }

  /**
   * Computes this party's authenticated shares of inputter party's inputs.
   *
   * @param inputterId id of inputter
   * @param numInputs number of inputs
   * @return authenticated shares of inputs
   */
  public List<AuthenticatedElement> input(Integer inputterId, int numInputs) {
    // receive per-element mac shares
    CopeSigner copeSigner = copeSigners.get(inputterId);
    List<FieldElement> macs = copeSigner.extend(numInputs + 1);

    // generate masks for macs
    List<FieldElement> masks = jointSampler.getNext(getModulus(), getModBitLength(), numInputs + 1);

    // receive masked value we will use in mac-check
    FieldElement maskedValue =
        getFieldElementSerializer().deserialize(getNetwork().receive(inputterId));

    // perform mac-check on opened value
    runMacCheck(maskedValue, masks, macs);

    // receive shares from inputter
    List<FieldElement> shares =
        getFieldElementSerializer().deserializeList(getNetwork().receive(inputterId));

    // combine shares and mac shares to spdz elements (exclude mac for dummy element)
    List<FieldElement> nonDummyMacs = macs.subList(0, numInputs);
    List<AuthenticatedElement> spdzElements = toAuthenticatedElements(shares, nonDummyMacs);
    return spdzElements;
  }

  void runMacCheck(FieldElement value, List<FieldElement> masks, List<FieldElement> macs) {
    // mask and combine macs
    FieldElement maskedMac = getFieldElementUtils().innerProduct(macs, masks);
    // perform mac-check on open masked value
    macChecker.check(value, macKeyShare, maskedMac);
  }

  /**
   * Runs mac-check on opened values.
   */
  public void check(List<AuthenticatedElement> sharesWithMacs, List<FieldElement> openValues) {
    // will use this to mask macs
    List<FieldElement> masks =
        jointSampler.getNext(getModulus(), getModBitLength(), sharesWithMacs.size());
    // only need macs
    List<FieldElement> macs =
        sharesWithMacs.stream().map(AuthenticatedElement::getMac).collect(Collectors.toList());
    // apply masks to open element so that it matches the macs when we mask them
    FieldElement open = getFieldElementUtils().innerProduct(openValues, masks);
    runMacCheck(open, masks, macs);
  }

  /**
   * Opens secret elements (distributes shares among all parties and recombines).
   *
   * @param closed authenticated elements to open
   * @return opened value
   */
  public List<FieldElement> open(List<AuthenticatedElement> closed) {
    // get shares from authenticated elements
    List<FieldElement> ownShares =
        closed.stream().map(AuthenticatedElement::getShare).collect(Collectors.toList());
    // send own shares to others
    getNetwork().sendToAll(getFieldElementSerializer().serialize(ownShares));
    // receive others' shares
    List<byte[]> rawShares = getNetwork().receiveFromAll();
    // parse
    List<List<FieldElement>> shares = rawShares.stream()
        .map(raw -> getFieldElementSerializer().deserializeList(raw)).collect(Collectors.toList());
    // recombine
    return getFieldElementUtils().pairwiseSum(shares);
  }

}
