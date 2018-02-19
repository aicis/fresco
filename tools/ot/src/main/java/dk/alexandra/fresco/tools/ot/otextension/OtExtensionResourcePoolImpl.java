package dk.alexandra.fresco.tools.ot.otextension;

import dk.alexandra.fresco.framework.sce.resources.ResourcePoolImpl;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.ExceptionConverter;
import dk.alexandra.fresco.tools.cointossing.CoinTossing;

import java.math.BigInteger;
import java.security.MessageDigest;

public class OtExtensionResourcePoolImpl extends ResourcePoolImpl implements
    OtExtensionResourcePool {
  // TODO move somewhere reasonable
  /**
   * The group generator of a Diffie-Hellman group of 2048 bits.
   */
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
  /**
   * The prime modulus of a Diffie-Hellman group of 2048 bits.
   */
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

  private final int otherId;
  private final int computationalSecurityParam;
  private final int lambdaSecurityParam;
  private final int instanceId;
  private final MessageDigest digest;
  private final RotList seedOts;
  private final CoinTossing ct;

  /**
   * Constructs an OT extension resource pool.
   *
   * @param myId
   *          The ID of the calling party
   * @param otherId
   *          The ID of the other party
   * @param computationalSecurityParam
   *          The computational security parameter
   * @param lambdaSecurityParam
   *          The statistical security parameter
   * @param instanceId
   *          The instance ID of this specific resource pool instance
   * @param drbg
   *          The randomness generator to be used by the calling party
   * @param ct
   *          An instance of a coin tossing protocol to be used with this specific resource pool
   * @param seedOts
   *          The seed OTs to be used as the base of the extension
   */
  public OtExtensionResourcePoolImpl(int myId, int otherId,
      int computationalSecurityParam, int lambdaSecurityParam, int instanceId,
      Drbg drbg, CoinTossing ct, RotList seedOts) {
    super(myId, 2, drbg);
    if (computationalSecurityParam < 1 || lambdaSecurityParam < 1
        || lambdaSecurityParam % 8 != 0 || computationalSecurityParam
            % 8 != 0) {
      throw new IllegalArgumentException(
          "Security parameters must be at least 1 and divisible by 8");
    }
    this.otherId = otherId;
    this.computationalSecurityParam = computationalSecurityParam;
    this.lambdaSecurityParam = lambdaSecurityParam;
    this.instanceId = instanceId;
    this.digest = ExceptionConverter.safe(() -> MessageDigest
        .getInstance("SHA-256"),
        "Configuration error, SHA-256 is needed for OT extension");
    this.ct = ct;
    this.seedOts = seedOts;
  }

  @Override
  public int getNoOfParties() {
    // By definition OT is a two-party protocol
    return 2;
  }

  @Override
  public int getComputationalSecurityParameter() {
    return computationalSecurityParam;
  }

  @Override
  public int getLambdaSecurityParam() {
    return lambdaSecurityParam;
  }

  @Override
  public int getOtherId() {
    return otherId;
  }

  @Override
  public MessageDigest getDigest() {
    return digest;
  }

  @Override
  public int getInstanceId() {
    return instanceId;
  }

  @Override
  public RotList getSeedOts() {
    return seedOts;
  }

  @Override
  public CoinTossing getCoinTossing() {
    return ct;
  }
}
