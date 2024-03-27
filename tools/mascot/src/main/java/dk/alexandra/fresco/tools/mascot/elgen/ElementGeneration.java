package dk.alexandra.fresco.tools.mascot.elgen;

import dk.alexandra.fresco.framework.builder.numeric.Addable;
import dk.alexandra.fresco.framework.builder.numeric.field.FieldElement;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.SecretSharer;
import dk.alexandra.fresco.framework.util.TransposeUtils;
import dk.alexandra.fresco.tools.mascot.MascotResourcePool;
import dk.alexandra.fresco.tools.mascot.cope.CopeInputter;
import dk.alexandra.fresco.tools.mascot.cope.CopeSigner;
import dk.alexandra.fresco.tools.mascot.field.AuthenticatedElement;
import dk.alexandra.fresco.tools.mascot.field.FieldElementUtils;
import dk.alexandra.fresco.tools.mascot.maccheck.MacCheck;
import dk.alexandra.fresco.tools.mascot.prg.FieldElementPrg;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Actively-secure protocol for generating authentication, secret-shared elements based on the
 * MASCOT protocol (<a href="https://eprint.iacr.org/2016/505.pdf">https://eprint.iacr.org/2016/505.pdf</a>).
 * <br> Allows a single party to secret-share a field element among all parties such that the
 * element is authenticated via a MAC. The MAC is secret-shared among the parties, as is the MAC
 * key.
 */
public class ElementGeneration {

  private final MacCheck macChecker;
  private final FieldElement macKeyShare;
  private final FieldElementPrg localSampler;
  private final FieldElementPrg jointSampler;
  private final SecretSharer<FieldElement> sharer;
  private final Map<Integer, CopeSigner> copeSigners;
  private final Map<Integer, CopeInputter> copeInputters;
  private final MascotResourcePool resourcePool;
  private final Network network;
  private final FieldElementUtils fieldElementUtils;

  /**
   * Creates new {@link ElementGeneration}.
   */
  public ElementGeneration(MascotResourcePool resourcePool, Network network,
      FieldElement macKeyShare, FieldElementPrg jointSampler) {
    this.resourcePool = Objects.requireNonNull(resourcePool);
    this.network = Objects.requireNonNull(network);
    this.fieldElementUtils = new FieldElementUtils(resourcePool.getFieldDefinition());
    this.macChecker = new MacCheck(resourcePool, network);
    this.macKeyShare = Objects.requireNonNull(macKeyShare);
    this.localSampler = Objects.requireNonNull(resourcePool.getLocalSampler());
    this.jointSampler = Objects.requireNonNull(jointSampler);
    this.sharer = new AdditiveSecretSharer(localSampler);
    this.copeSigners = new HashMap<>();
    this.copeInputters = new HashMap<>();
    initializeCope(resourcePool, network);
  }

  /**
   * Computes this party's authenticated shares of input. <br> To be called by input party.
   * Implements the input party's side of the Input sub-protocol of Protocol 3.
   *
   * @param values values to input
   * @return authenticated shares of inputs
   */
  public List<AuthenticatedElement> input(List<FieldElement> values) {
    // make sure we are working with an array list
    values = new ArrayList<>(values);

    // add extra random element which will later be used to mask inputs (step 1)
    FieldElement extraElement = localSampler.getNext();
    values.add(extraElement);

    // inputter secret-shares input values (step 2)
    List<FieldElement> shares = secretShare(values, resourcePool.getNoOfParties());

    // compute per element mac share (steps 3, 4, 5)
    List<FieldElement> macs = macValues(values);

    // generate coefficients for values and macs (step 6)
    List<FieldElement> coefficients = jointSampler.getNext(values.size());

    // mask and combine values (step 7)
    FieldElement maskedValue = fieldElementUtils.innerProduct(values, coefficients);
    // send masked value to all other parties
    network.sendToAll(resourcePool.getFieldDefinition().serialize(maskedValue));
    // so that we can use receiveFromAll correctly later
    network.receive(resourcePool.getMyId());

    // perform mac-check on opened value (will throw if mac check fails) (steps 8 and 9)
    runMacCheck(maskedValue, coefficients, macs);

    // combine shares and mac shares to authenticated elements
    // (exclude mac and share of extra element) (step 10)
    List<FieldElement> inputElementMacs = macs.subList(0, shares.size() - 1);
    return toAuthenticatedElements(shares.subList(0, shares.size() - 1), inputElementMacs);
  }

  /**
   * Computes this party's authenticated shares of inputter party's inputs. Implements a non-input
   * party's side of the Input sub-protocol of Protocol 3.
   *
   * @param inputterId id of inputter
   * @param numInputs number of inputs
   * @return authenticated shares of inputs
   */
  public List<AuthenticatedElement> input(Integer inputterId, int numInputs) {
    // receive shares from inputter (step 2)
    List<FieldElement> shares =
        resourcePool.getFieldDefinition().deserializeList(network.receive(inputterId));

    // receive per-element mac shares (steps 3 through 5)
    CopeSigner copeSigner = copeSigners.get(inputterId);
    List<FieldElement> macs = copeSigner.extend(numInputs + 1);

    // generate coefficients for macs (step 6)
    List<FieldElement> coefficients = jointSampler.getNext(numInputs + 1);

    // receive masked value we will use in mac-check (step 7)
    FieldElement maskedValue =
        resourcePool.getFieldDefinition().deserialize(network.receive(inputterId));

    // perform mac-check on opened value (steps 8 through 9)
    runMacCheck(maskedValue, coefficients, macs);

    // combine shares and mac shares to authenticated  elements
    // (exclude mac and share of extra element) (step 10)
    List<FieldElement> inputElementMacs = macs.subList(0, numInputs);
    return toAuthenticatedElements(shares.subList(0, numInputs), inputElementMacs);
  }

  /**
   * Runs mac-check on opened values. Implements Check sub-protocol of Protocol 3.
   *
   * @param sharesWithMacs authenticated shares holding mac shares
   * @param openValues batch of opened, unchecked values
   */
  public void check(List<AuthenticatedElement> sharesWithMacs,
      List<FieldElement> openValues) {
    // will use this to mask macs
    List<FieldElement> masks = jointSampler.getNext(sharesWithMacs.size());
    // only need macs
    List<FieldElement> macs =
        sharesWithMacs.stream().map(AuthenticatedElement::getMac).collect(Collectors.toList());
    // apply masks to open element so that it matches the macs when we mask them
    FieldElement open = fieldElementUtils.innerProduct(openValues, masks);
    runMacCheck(open, masks, macs);
  }

  /**
   * Opens secret elements (distributes shares among all parties and recombines). Implements Open
   * sub-protocol of Protocol 3.
   *
   * @param closed authenticated elements to open
   * @return opened value
   */
  public List<FieldElement> open(List<AuthenticatedElement> closed) {
    // get shares from authenticated elements (step 1)
    List<FieldElement> ownShares =
        closed.stream().map(AuthenticatedElement::getShare).collect(Collectors.toList());
    // send own shares to others
    network.sendToAll(resourcePool.getFieldDefinition().serialize(ownShares));
    // receive others' shares
    List<byte[]> rawShares = network.receiveFromAll();
    // parse
    List<List<FieldElement>> shares = rawShares.stream()
        .map(resourcePool.getFieldDefinition()::deserializeList)
        .collect(Collectors.toList());
    // recombine (step 2)
    return Addable.sumRows(shares);
  }

  /**
   * Computes shares of macs of unauthenticated values.<br> For each unauthenticated value <i>v</i>,
   * computes <i>[v * (alpha<sub>1</sub> + ... + alpha<sub>n</sub>)]</i> where
   * <i>alpha<sub>i</sub></i> is party <i>i</i>'s mac key share.
   */
  private List<FieldElement> macValues(List<FieldElement> values) {
    List<FieldElement> selfMacced = selfMac(values);
    List<List<FieldElement>> maccedByAll = otherPartiesMac(values);
    maccedByAll.add(selfMacced);
    return Addable.sumRows(maccedByAll);
  }

  /**
   * Uses COPE protocol to multiply unathenticated values with each other party's (i.e., not this
   * party's) mac key share and get a share of the result.
   */
  private List<List<FieldElement>> otherPartiesMac(List<FieldElement> values) {
    List<List<FieldElement>> perPartySignatures = new ArrayList<>();
    // note that the order in which this is run does not matter so it's fine to use values().
    for (CopeInputter copeInputter : copeInputters.values()) {
      perPartySignatures.add(copeInputter.extend(values));
    }
    return perPartySignatures;
  }

  /**
   * Multiplies each unauthenticated value by this party's mac key share.
   */
  private List<FieldElement> selfMac(List<FieldElement> values) {
    return fieldElementUtils.scalarMultiply(values, macKeyShare);
  }

  /**
   * Computes additive (unauthenticated) shares of values and distributes the shares across
   * parties.
   */
  private List<FieldElement> secretShare(List<FieldElement> values, int numShares) {
    List<List<FieldElement>> allShares =
        values.stream().map(value -> sharer.share(value, numShares)).collect(Collectors.toList());
    List<List<FieldElement>> byParty = TransposeUtils.transpose(allShares);
    for (int partyId = 1; partyId <= resourcePool.getNoOfParties(); partyId++) {
      // send shares to everyone but self
      if (partyId != resourcePool.getMyId()) {
        List<FieldElement> shares = byParty.get(partyId - 1);
        network.send(partyId, resourcePool.getFieldDefinition().serialize(shares));
      }
    }
    // return own shares
    return byParty.get(resourcePool.getMyId() - 1);
  }

  /**
   * "Zips" raw value shares and mac shares into authenticated elements.
   */
  private List<AuthenticatedElement> toAuthenticatedElements(List<FieldElement> shares,
      List<FieldElement> macs) {
    return IntStream.range(0, shares.size())
        .mapToObj(idx -> {
          FieldElement share = shares.get(idx);
          FieldElement mac = macs.get(idx);
          return new AuthenticatedElement(share, mac);
        })
        .collect(Collectors.toList());
  }

  /**
   * Performs mac check on opened value. The opened value is a linear combination of a batch of
   * opened values and random coefficients {@code randomCoefficients}.
   *
   * @param value a linear combination of a batch of opened values and random coefficients
   * @param randomCoefficients random coefficients
   * @param macs mac shares
   */
  private void runMacCheck(FieldElement value, List<FieldElement> randomCoefficients,
      List<FieldElement> macs) {
    // mask and combine macs
    FieldElement maskedMac = fieldElementUtils.innerProduct(macs, randomCoefficients);
    // perform mac-check on open masked value
    macChecker.check(value, macKeyShare, maskedMac);
  }

  /**
   * Initializes COPE protocols. Implements Initialize sub-protocol of Protocol 3 (with the only
   * difference that the mac key share has already been sampled before this protocol runs).
   */
  private void initializeCope(MascotResourcePool resourcePool, Network network) {
    for (int partyId = 1; partyId <= resourcePool.getNoOfParties(); partyId++) {
      if (resourcePool.getMyId() != partyId) {
        CopeSigner signer;
        CopeInputter inputter;
        // construction order matters since receive blocks and this is not parallelized
        if (resourcePool.getMyId() < partyId) {
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
}
