package dk.alexandra.fresco.tools.mascot;

import dk.alexandra.fresco.commitment.HashBasedCommitment;
import dk.alexandra.fresco.commitment.HashBasedCommitmentSerializer;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.network.serializers.StrictBitVectorSerializer;
import dk.alexandra.fresco.framework.sce.resources.ResourcePoolImpl;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.ExceptionConverter;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.cointossing.CoinTossing;
import dk.alexandra.fresco.tools.mascot.field.FieldElementSerializer;
import dk.alexandra.fresco.tools.mascot.utils.FieldElementPrg;
import dk.alexandra.fresco.tools.mascot.utils.FieldElementPrgImpl;
import dk.alexandra.fresco.tools.ot.base.RotBatch;
import dk.alexandra.fresco.tools.ot.otextension.BristolRotBatch;
import dk.alexandra.fresco.tools.ot.otextension.RotList;
import dk.alexandra.fresco.tools.ot.otextension.OtExtensionResourcePool;
import dk.alexandra.fresco.tools.ot.otextension.OtExtensionResourcePoolImpl;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map;

public class MascotResourcePoolImpl extends ResourcePoolImpl implements MascotResourcePool {

  private final Map<Integer, RotList> seedOts;
  private final List<Integer> partyIds;
  private final int instanceId;
  private final BigInteger modulus;
  private final int modBitLength;
  private final int lambdaSecurityParam;
  private final int prgSeedLength;
  private final int numLeftFactors;
  private final FieldElementPrg localSampler;
  private final FieldElementSerializer fieldElementSerializer;
  private final StrictBitVectorSerializer strictBitVectorSerializer;
  private final ByteSerializer<HashBasedCommitment> commitmentSerializer;
  private final MessageDigest messageDigest;

  /**
   * Creates new mascot resource pool.
   */
  public MascotResourcePoolImpl(Integer myId, List<Integer> partyIds,
      int instanceId, Drbg drbg, Map<Integer, RotList> seedOts,
      BigInteger modulus, int modBitLength, int lambdaSecurityParam,
      int prgSeedLength, int numLeftFactors) {
    super(myId, partyIds.size(), drbg);
    this.partyIds = partyIds;
    this.instanceId = instanceId;
    this.seedOts = seedOts;
    this.modulus = modulus;
    this.modBitLength = modBitLength;
    this.lambdaSecurityParam = lambdaSecurityParam;
    this.numLeftFactors = numLeftFactors;
    this.prgSeedLength = prgSeedLength;
    this.localSampler = new FieldElementPrgImpl(new StrictBitVector(prgSeedLength, drbg));
    this.fieldElementSerializer = new FieldElementSerializer(modulus, modBitLength);
    this.strictBitVectorSerializer = new StrictBitVectorSerializer();
    this.commitmentSerializer = new HashBasedCommitmentSerializer();
    this.messageDigest = ExceptionConverter.safe(() -> MessageDigest.getInstance("SHA-256"),
        "Configuration error, SHA-256 is needed for Mascot");
  }

  @Override
  public List<Integer> getPartyIds() {
    return partyIds;
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
    return modBitLength;
  }

  @Override
  public int getLambdaSecurityParam() {
    return lambdaSecurityParam;
  }

  @Override
  public int getNumCandidatesPerTriple() {
    return numLeftFactors;
  }

  @Override
  public FieldElementPrg getLocalSampler() {
    return localSampler;
  }

  @Override
  public FieldElementSerializer getFieldElementSerializer() {
    return fieldElementSerializer;
  }

  @Override
  public StrictBitVectorSerializer getStrictBitVectorSerializer() {
    return strictBitVectorSerializer;
  }

  @Override
  public ByteSerializer<HashBasedCommitment> getCommitmentSerializer() {
    return commitmentSerializer;
  }

  @Override
  public RotBatch createRot(int otherId, Network network) {
    if (getMyId() == otherId) {
      throw new IllegalArgumentException("Cannot initialize with self");
    }
    CoinTossing ct = new CoinTossing(getMyId(), otherId, getRandomGenerator(), network);
    ct.initialize();
    OtExtensionResourcePool otResources = new OtExtensionResourcePoolImpl(getMyId(), otherId,
        getPrgSeedLength(), getLambdaSecurityParam(), getInstanceId(),
        getRandomGenerator(), ct, seedOts.get(otherId));
    return new BristolRotBatch(otResources, network);
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
    return prgSeedLength;
  }

}
