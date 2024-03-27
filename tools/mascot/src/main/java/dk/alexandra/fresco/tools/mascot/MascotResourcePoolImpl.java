package dk.alexandra.fresco.tools.mascot;

import dk.alexandra.fresco.framework.builder.numeric.field.FieldDefinition;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.resources.ResourcePoolImpl;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.ExceptionConverter;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.framework.util.ValidationUtils;
import dk.alexandra.fresco.tools.cointossing.CoinTossing;
import dk.alexandra.fresco.tools.mascot.prg.FieldElementPrg;
import dk.alexandra.fresco.tools.mascot.prg.FieldElementPrgImpl;
import dk.alexandra.fresco.tools.ot.base.RotBatch;
import dk.alexandra.fresco.tools.ot.otextension.*;

import java.security.MessageDigest;
import java.util.Map;
import java.util.Objects;

public class MascotResourcePoolImpl extends ResourcePoolImpl implements MascotResourcePool {

  private final Map<Integer, RotList> seedOts;
  private final int instanceId;
  private final FieldDefinition fieldDefinition;
  private final FieldElementPrg localSampler;
  private final MessageDigest messageDigest;
  private final MascotSecurityParameters mascotSecurityParameters;
  private final Drbg drbg;

  /**
   * Creates new {@link MascotResourcePoolImpl}.
   *
   * @param myId this party's id
   * @param noOfParties number of parties
   * @param instanceId the instance ID which is unique for this particular resource pool object,
   *     but
   *     only in the given execution.
   * @param drbg source of randomness
   * @param seedOts pre-computed base OTs
   * @param mascotSecurityParameters mascot security parameters ({@link
   *     MascotSecurityParameters})
   * @param fieldDefinition field used for calculations
   */
  public MascotResourcePoolImpl(int myId, int noOfParties, int instanceId, Drbg drbg,
      Map<Integer, RotList> seedOts, MascotSecurityParameters mascotSecurityParameters,
      FieldDefinition fieldDefinition) {
    super(myId, noOfParties);
    ValidationUtils.assertValidId(myId, noOfParties);
    this.drbg = Objects.requireNonNull(drbg);
    this.instanceId = instanceId;
    this.seedOts = Objects.requireNonNull(seedOts);
    this.fieldDefinition = Objects.requireNonNull(fieldDefinition);
    this.mascotSecurityParameters = Objects.requireNonNull(mascotSecurityParameters);
    this.localSampler = new FieldElementPrgImpl(
        new StrictBitVector(mascotSecurityParameters.getPrgSeedLength(), drbg),
        this.fieldDefinition);
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
      OtExtensionResourcePool otResources = new BristolOtExtensionResourcePool(getMyId(), otherId,
              getPrgSeedLength(), getLambdaSecurityParam(), getInstanceId(),
              getRandomGenerator(), ct, seedOts.get(otherId));
      return new BristolRotBatch(new RotFactory(otResources, network));
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
  public int getPrgSeedLength() {
    return mascotSecurityParameters.getPrgSeedLength();
  }
}
