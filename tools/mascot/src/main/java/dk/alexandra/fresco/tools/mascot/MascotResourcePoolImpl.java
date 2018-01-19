package dk.alexandra.fresco.tools.mascot;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.sce.resources.ResourcePoolImpl;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.ExceptionConverter;
import dk.alexandra.fresco.framework.util.ModulusFinder;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.cointossing.CoinTossing;
import dk.alexandra.fresco.tools.mascot.prg.FieldElementPrg;
import dk.alexandra.fresco.tools.mascot.prg.FieldElementPrgImpl;
import dk.alexandra.fresco.tools.ot.base.RotBatch;
import dk.alexandra.fresco.tools.ot.otextension.BristolRotBatch;
import dk.alexandra.fresco.tools.ot.otextension.OtExtensionResourcePool;
import dk.alexandra.fresco.tools.ot.otextension.OtExtensionResourcePoolImpl;
import dk.alexandra.fresco.tools.ot.otextension.RotImpl;
import dk.alexandra.fresco.tools.ot.otextension.RotList;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Map;

public class MascotResourcePoolImpl extends ResourcePoolImpl implements MascotResourcePool {

  private final Map<Integer, RotList> seedOts;
  private final int instanceId;
  private final BigInteger modulus;
  private final FieldElementPrg localSampler;
  private final MessageDigest messageDigest;
  private final MascotSecurityParameters mascotSecurityParameters;

  /**
   * Creates new {@link MascotResourcePoolImpl}.
   *
   * @param myId this party's id
   * @param noOfParties number of parties
   * @param instanceId the instance ID which is unique for this particular resource pool object, but
   * only in the given execution.
   * @param drbg source of randomness
   * @param seedOts pre-computed base OTs
   * @param mascotSecurityParameters mascot security parameters ({@link MascotSecurityParameters})
   */
  public MascotResourcePoolImpl(int myId, int noOfParties, int instanceId, Drbg drbg,
      Map<Integer, RotList> seedOts, MascotSecurityParameters mascotSecurityParameters) {
    super(myId, noOfParties, drbg);
    this.instanceId = instanceId;
    this.seedOts = seedOts;
    this.modulus = ModulusFinder.findSuitableModulus(mascotSecurityParameters.getModBitLength());
    this.mascotSecurityParameters = mascotSecurityParameters;
    this.localSampler = new FieldElementPrgImpl(
        new StrictBitVector(mascotSecurityParameters.getPrgSeedLength(), drbg));
    this.messageDigest = ExceptionConverter.safe(() -> MessageDigest.getInstance("SHA-256"),
        "Configuration error, SHA-256 is needed for Mascot");
  }

  @Override
  public BigInteger getModulus() {
    return modulus;
  }

  @Override
  public int getInstanceId() {
    return instanceId;
  }

  @Override
  public int getModBitLength() {
    return mascotSecurityParameters.getModBitLength();
  }

  @Override
  public int getLambdaSecurityParam() {
    return mascotSecurityParameters.getLambdaSecurityParam();
  }

  @Override
  public int getNumCandidatesPerTriple() {
    return mascotSecurityParameters.getNumCandidatesPerTriple();
  }

  @Override
  public FieldElementPrg getLocalSampler() {
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
        getPrgSeedLength(), getLambdaSecurityParam(), getInstanceId(),
        getRandomGenerator(), ct, seedOts.get(otherId));
    return new BristolRotBatch(new RotImpl(otResources, network));
  }

  @Override
  public ByteSerializer<BigInteger> getSerializer() {
    throw new UnsupportedOperationException();
  }

  @Override
  public MessageDigest getMessageDigest() {
    return messageDigest;
  }

  @Override
  public int getPrgSeedLength() {
    return mascotSecurityParameters.getPrgSeedLength();
  }

}
