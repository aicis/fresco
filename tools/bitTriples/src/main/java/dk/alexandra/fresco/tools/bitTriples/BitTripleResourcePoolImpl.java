package dk.alexandra.fresco.tools.bitTriples;

import dk.alexandra.fresco.framework.builder.numeric.field.BigIntegerFieldDefinition;
import dk.alexandra.fresco.framework.builder.numeric.field.FieldDefinition;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.resources.ResourcePoolImpl;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.ExceptionConverter;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.bitTriples.prg.BytePrg;
import dk.alexandra.fresco.tools.bitTriples.prg.BytePrgImpl;
import dk.alexandra.fresco.tools.cointossing.CoinTossing;
import dk.alexandra.fresco.tools.ot.base.DhParameters;
import dk.alexandra.fresco.tools.ot.base.NaorPinkasOt;
import dk.alexandra.fresco.tools.ot.base.RotBatch;
import dk.alexandra.fresco.tools.ot.otextension.BristolRotBatch;
import dk.alexandra.fresco.tools.ot.otextension.CoteFactory;
import dk.alexandra.fresco.tools.ot.otextension.OtExtensionResourcePool;
import dk.alexandra.fresco.tools.ot.otextension.OtExtensionResourcePoolImpl;
import dk.alexandra.fresco.tools.ot.otextension.RotFactory;
import dk.alexandra.fresco.tools.ot.otextension.RotList;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Map;

public class BitTripleResourcePoolImpl extends ResourcePoolImpl implements BitTripleResourcePool {

  private final Map<Integer, RotList> seedOts;
  private final int instanceId;
  private final FieldDefinition fieldDefinition;
  private final BytePrg localSampler;
  private final MessageDigest messageDigest;
  private final BitTripleSecurityParameters bitTripleSecurityParameters;
  private final Drbg drbg;

  /**
   * Creates new {@link BitTripleResourcePoolImpl}.
   *
   * @param myId this party's id
   * @param noOfParties number of parties
   * @param instanceId the instance ID which is unique for this particular resource pool object,
   *     but
   *     only in the given execution.
   * @param drbg source of randomness - Must be initiated with same seed
   * @param seedOts pre-computed base OTs
   * @param bitTripleSecurityParameters mascot security parameters ({@link
   *     BitTripleSecurityParameters})
   */
  public BitTripleResourcePoolImpl(int myId, int noOfParties, int instanceId, Drbg drbg,
      Map<Integer, RotList> seedOts, BitTripleSecurityParameters bitTripleSecurityParameters) {
    super(myId, noOfParties);
    this.drbg = drbg;
    this.instanceId = instanceId;
    this.seedOts = seedOts;
    this.fieldDefinition = new BigIntegerFieldDefinition(BigInteger.valueOf(2));
    this.bitTripleSecurityParameters = bitTripleSecurityParameters;
    this.localSampler = new BytePrgImpl(drbg);
    this.messageDigest = ExceptionConverter.safe(() -> MessageDigest.getInstance("SHA-256"),
        "Configuration error, SHA-256 is needed for Mascot");
  }

  @Override
  public FieldDefinition getFieldDefinition() {
    return fieldDefinition;
  }

  @Override
  public int getInstanceId() {
    return instanceId;
  }

  @Override
  public int getModBitLength() {
    return fieldDefinition.getBitLength();
  }

  @Override
  public int getComputationalSecurityBitParameter() {
    return bitTripleSecurityParameters.getComputationalSecurityBitParameter();
  }
  @Override
  public int getStatisticalSecurityByteParameter() {
    return bitTripleSecurityParameters.getStatisticalSecurityByteParameter();
  }

  @Override
  public int getNumCandidatesPerTriple() {
    return bitTripleSecurityParameters.getNumCandidatesPerTriple();
  }

  @Override
  public BytePrg getLocalSampler() {
    return localSampler;
  }

  @Override
  public RotBatch createRot(int otherId, Network network) {
    if (getMyId() == otherId) {
      throw new IllegalArgumentException("Cannot initialize with self");
    }
    CoinTossing ct = new CoinTossing(getMyId(), otherId, getRandomGenerator());
    ct.initialize(network);
    OtExtensionResourcePool otResources = new OtExtensionResourcePoolImpl(getMyId(), otherId,
        getPrgSeedBitLength(), getComputationalSecurityBitParameter(), getInstanceId(),
        getRandomGenerator(), ct, seedOts.get(otherId));
    return new BristolRotBatch(new RotFactory(otResources, network),
        getPrgSeedBitLength(), getComputationalSecurityBitParameter());
  }

  @Override
  public CoteFactory createCote(int otherId, Network network, StrictBitVector choices) {
    if (getMyId() == otherId) {
      throw new IllegalArgumentException("Cannot initialize with self");
    }
    CoinTossing ct = new CoinTossing(getMyId(), otherId, getRandomGenerator());
    ct.initialize(network);
    NaorPinkasOt ot = new NaorPinkasOt(otherId,getRandomGenerator(),network, DhParameters.getStaticDhParams());
    RotList currentSeedOts = new RotList(drbg, choices.getSize(), choices);
    if (getMyId() < otherId) {
      currentSeedOts.send(ot);
      currentSeedOts.receive(ot);
    } else {
      currentSeedOts.receive(ot);
      currentSeedOts.send(ot);
    }
    OtExtensionResourcePool otResources = new OtExtensionResourcePoolImpl(getMyId(), otherId,
        choices.getSize(), getStatisticalSecurityByteParameter(), getInstanceId(),
        getRandomGenerator(), ct, currentSeedOts);

    return new CoteFactory(otResources, network);
  }

  @Override
  public Drbg getRandomGenerator() {
    return drbg;
  }

  @Override
  public MessageDigest getMessageDigest() {
    return messageDigest;
  }

  @Override
  public int getPrgSeedBitLength() {
    return bitTripleSecurityParameters.getPrgSeedBitLength();
  }

}
