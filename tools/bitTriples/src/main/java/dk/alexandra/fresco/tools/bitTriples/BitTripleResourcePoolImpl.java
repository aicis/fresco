package dk.alexandra.fresco.tools.bitTriples;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.resources.ResourcePoolImpl;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.ExceptionConverter;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.framework.util.ValidationUtils;
import dk.alexandra.fresco.tools.bitTriples.prg.BytePrg;
import dk.alexandra.fresco.tools.bitTriples.prg.BytePrgImpl;
import dk.alexandra.fresco.tools.cointossing.CoinTossing;
import dk.alexandra.fresco.tools.ot.base.AbstractNaorPinkasOT;
import dk.alexandra.fresco.tools.ot.base.BigIntNaorPinkas;
import dk.alexandra.fresco.tools.ot.otextension.BristolOtExtensionResourcePool;
import dk.alexandra.fresco.tools.ot.otextension.CoteFactory;
import dk.alexandra.fresco.tools.ot.otextension.OtExtensionResourcePool;
import dk.alexandra.fresco.tools.ot.otextension.RotList;
import java.security.MessageDigest;

public class BitTripleResourcePoolImpl extends ResourcePoolImpl implements BitTripleResourcePool {

  private final int instanceId;
  private final BytePrg localSampler;
  private final MessageDigest messageDigest;
  private final BitTripleSecurityParameters bitTripleSecurityParameters;
  private final Drbg drbg;

  /**
   * Creates new {@link BitTripleResourcePoolImpl}.
   *
   * @param myId                        this party's id
   * @param noOfParties                 number of parties
   * @param instanceId                  the instance ID which is unique for this particular resource
   *                                    pool object, but only in the given execution.
   * @param drbg                        source of randomness - Must be initiated with same seed
   * @param bitTripleSecurityParameters mascot security parameters
   *                                    ({@link BitTripleSecurityParameters})
   */
  public BitTripleResourcePoolImpl(
      int myId,
      int noOfParties,
      int instanceId,
      Drbg drbg,
      BitTripleSecurityParameters bitTripleSecurityParameters) {
    super(myId, noOfParties);
    ValidationUtils.assertValidId(myId, noOfParties);
    this.drbg = drbg;
    this.instanceId = instanceId;
    this.bitTripleSecurityParameters = bitTripleSecurityParameters;
    this.localSampler = new BytePrgImpl(drbg);
    this.messageDigest =
        ExceptionConverter.safe(
            () -> MessageDigest.getInstance("SHA-256"),
            "Configuration error, SHA-256 is needed for Mascot");
  }

  @Override
  public int getInstanceId() {
    return instanceId;
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
  public BytePrg getLocalSampler() {
    return localSampler;
  }

  @Override
  public CoteFactory createCote(int otherId, Network network, StrictBitVector choices) {
    if (getMyId() == otherId) {
      throw new IllegalArgumentException("Cannot initialize with self");
    }
    CoinTossing ct = new CoinTossing(getMyId(), otherId, getRandomGenerator());
    ct.initialize(network);
    AbstractNaorPinkasOT ot =
        new BigIntNaorPinkas(otherId, getRandomGenerator(), network);
    RotList currentSeedOts = new RotList(drbg, choices.getSize(), choices);
    if (getMyId() < otherId) {
      currentSeedOts.send(ot);
      currentSeedOts.receive(ot);
    } else {
      currentSeedOts.receive(ot);
      currentSeedOts.send(ot);
    }
    OtExtensionResourcePool otResources =
        new BristolOtExtensionResourcePool(
            getMyId(),
            otherId,
            choices.getSize(),
            getStatisticalSecurityByteParameter(),
            getInstanceId(),
            getRandomGenerator(),
            ct,
            currentSeedOts);

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
