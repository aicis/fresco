package dk.alexandra.fresco.tools.ot.otextension;

import dk.alexandra.fresco.framework.sce.resources.ResourcePoolImpl;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.ExceptionConverter;
import dk.alexandra.fresco.framework.util.ValidationUtils;
import dk.alexandra.fresco.tools.cointossing.CoinTossing;
import java.security.MessageDigest;

public class BristolOtExtensionResourcePool extends ResourcePoolImpl implements
    OtExtensionResourcePool {

  private final int otherId;
  private final int computationalSecurityParam;
  private final int adjustedLambdaSecurityParam;
  private final int instanceId;
  private final MessageDigest digest;
  private final RotList seedOts;
  private final CoinTossing ct;
  private final Drbg drbg;

  /**
   * Constructs an OT extension resource pool.
   *
   * @param myId                       The ID of the calling party
   * @param otherId                    The ID of the other party
   * @param computationalSecurityParam The computational security parameter
   * @param lambdaSecurityParam        The statistical security parameter
   * @param instanceId                 The instance ID of this specific resource pool instance
   * @param drbg                       The randomness generator to be used by the calling party
   * @param ct                         An instance of a coin tossing protocol to be used with this
   *                                   specific resource pool
   * @param seedOts                    The seed OTs to be used as the base of the extension
   */
  public BristolOtExtensionResourcePool(int myId, int otherId,
      int computationalSecurityParam, int lambdaSecurityParam, int instanceId,
      Drbg drbg, CoinTossing ct, RotList seedOts) {
    super(myId, 2);
    ValidationUtils.assertValidId(otherId);
    if (computationalSecurityParam < 1 || lambdaSecurityParam < 1
        || lambdaSecurityParam % 8 != 0 || computationalSecurityParam
        % 8 != 0) {
      throw new IllegalArgumentException(
          "Security parameters must be at least 1 and divisible by 8");
    }
    this.drbg = drbg;
    this.otherId = otherId;
    this.computationalSecurityParam = computationalSecurityParam;
    // In case lambdaSecurityParam is not divisible by 16, we have to round up to ensure that the adjusted lambda parameter is still divisible by 8
    int adjustment = (lambdaSecurityParam % 16 == 0 ? 0 : 4);
    // Set the internal statistical security parameter to 150% of the input. This is because there is a deterioration
    // of the security parameter in the Bristol OT protocol, as pointed out in section 4.1.3 here https://eprint.iacr.org/2022/192.pdf
    this.adjustedLambdaSecurityParam = lambdaSecurityParam + (lambdaSecurityParam / 2) + adjustment;
    this.instanceId = instanceId;
    this.digest = ExceptionConverter.safe(() -> MessageDigest
            .getInstance("SHA-256"),
        "Configuration error, SHA-256 is needed for OT extension");
    this.ct = ct;
    this.seedOts = seedOts;
  }

  @Override
  public int getComputationalSecurityParameter() {
    return computationalSecurityParam;
  }

  /**
   * Returns the statistical security parameter to be used within the Bristol OT extension. NOTE: It
   * is larger than the regular statistical security parameter.
   */
  @Override
  public int getLambdaSecurityParam() {
    return adjustedLambdaSecurityParam;
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

  @Override
  public Drbg getRandomGenerator() {
    return drbg;
  }
}
