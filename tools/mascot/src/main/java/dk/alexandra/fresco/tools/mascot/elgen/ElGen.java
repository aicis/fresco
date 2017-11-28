package dk.alexandra.fresco.tools.mascot.elgen;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import dk.alexandra.fresco.suite.spdz.datatypes.SpdzElement;
import dk.alexandra.fresco.tools.mascot.BaseProtocol;
import dk.alexandra.fresco.tools.mascot.MascotContext;
import dk.alexandra.fresco.tools.mascot.cope.CopeInputter;
import dk.alexandra.fresco.tools.mascot.cope.CopeSigner;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import dk.alexandra.fresco.tools.mascot.maccheck.MacCheck;
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
    // TODO: should run these in parallel
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
    return null;
  }

  public List<SpdzElement> input(List<FieldElement> values) {
    // can't input before initializing
    if (!initialized) {
      throw new IllegalStateException("Need to initialize first");
    }
    BigInteger modulus = ctx.getModulus();
    int modBitLength = ctx.getkBitLength();

    // add extra random element which will later be used to mask inputs
    FieldElement extraElement = sampler.sample(modulus, modBitLength);
    values.add(0, extraElement);

    // compute per element mac share
    List<FieldElement> macs = macValues(values);
    
    return null;
  }

}
