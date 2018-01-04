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
import dk.alexandra.fresco.tools.mascot.field.FieldElementSerializer;
import dk.alexandra.fresco.tools.mascot.utils.FieldElementPrg;
import dk.alexandra.fresco.tools.mascot.utils.FieldElementPrgImpl;
import dk.alexandra.fresco.tools.ot.base.NaorPinkasOt;
import dk.alexandra.fresco.tools.ot.base.Ot;
import dk.alexandra.fresco.tools.ot.base.RotBatch;
import dk.alexandra.fresco.tools.ot.otextension.BristolRotBatch;
import dk.alexandra.fresco.tools.ot.otextension.OtExtensionResourcePool;
import dk.alexandra.fresco.tools.ot.otextension.OtExtensionResourcePoolImpl;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.List;
import javax.crypto.spec.DHParameterSpec;

public class MascotResourcePoolImpl extends ResourcePoolImpl implements MascotResourcePool {

  // TODO move somewhere reasonable
  public static final BigInteger DH_G_VALUE = new BigInteger(
      "1817929693051677794042418360119535939035448877384059423016092223723589389"
          + "89386921540078076694389023214591116103022506752626702949377742490622411"
          + "36154252930934999558878557838951366230121689192613836661801579283976804"
          + "90566221950235571908449465416597162122008963523511429191971262704962062"
          + "23722995544735685829105160578247097947199471860741139749699562917671426"
          + "82888600060270321923905677901250333513320663621356005726499527794262632"
          + "80575136645831734174762968521856711608877942562412558950963899754610266"
          + "97615963606394464455636761856586890950014177457842992286652934126338664"
          + "99748366638338849983708609236396436614761807745");
  public static final BigInteger DH_P_VALUE = new BigInteger(
      "2080109726332741595567900301553712643291061397185326442939225885200811703"
          + "46221477943683854922915625754365585955880683687623164529077074421717622"
          + "55815247681135202838112705300460371527291002353818384380395178484616163"
          + "81789931732016235932408088148285827220196826505807878031275264842308641"
          + "84386700540754381703938109115634660390655677474772619937553430208773150"
          + "06567328507885962926589890627547794887973720401310026543273364787901564"
          + "85827844212318499978829377355564689095172787513731965744913645190518423"
          + "06594567246898679968677700656495114013774368779648395287433119164167454"
          + "67731166272088057888135437754886129005590419051");

  private final List<Integer> partyIds;
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
  public MascotResourcePoolImpl(Integer myId, List<Integer> partyIds, Drbg drbg, BigInteger modulus,
      int modBitLength, int lambdaSecurityParam, int prgSeedLength, int numLeftFactors) {
    super(myId, partyIds.size(), drbg);
    this.partyIds = partyIds;
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
    DHParameterSpec params = new DHParameterSpec(DH_P_VALUE, DH_G_VALUE);
    Ot ot = ExceptionConverter.safe(() -> new NaorPinkasOt(getMyId(), otherId,
            getRandomGenerator(), network, params),
        "Missing security hash function or PRG, which is dependent in this library");
    OtExtensionResourcePool otResources = new OtExtensionResourcePoolImpl(getMyId(), otherId,
        getModBitLength(), getLambdaSecurityParam(), getRandomGenerator());
    return new BristolRotBatch(otResources, network, ot);
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
