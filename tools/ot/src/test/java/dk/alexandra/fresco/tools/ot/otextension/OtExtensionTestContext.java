package dk.alexandra.fresco.tools.ot.otextension;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.tools.helper.Constants;
import dk.alexandra.fresco.tools.helper.TestRuntime;
import dk.alexandra.fresco.tools.ot.base.DummyOt;
import dk.alexandra.fresco.tools.ot.base.Ot;

import java.security.MessageDigest;
import java.util.Arrays;

public class OtExtensionTestContext {
  private final OtExtensionResourcePool resources;
  private final Network network;

  /**
   * Initialize the test context using specific parameters.
   *
   * @param myId
   *          The ID of the calling party
   * @param otherId
   *          The ID of the other party
   * @param kbitLength
   *          The computational security parameter
   * @param lambdaSecurityParam
   *          The statistical security parameter
   */
  public OtExtensionTestContext(int myId, int otherId, int kbitLength,
      int lambdaSecurityParam) {
    Drbg rand = new AesCtrDrbg(Constants.seedOne);
    this.resources = new OtExtensionResourcePoolImpl(myId, otherId, kbitLength,
        lambdaSecurityParam, rand);
    this.network = new CheatingNetwork(TestRuntime.defaultNetworkConfiguration(myId, Arrays.asList(
        1, 2)));
  }

  public OtExtensionResourcePool getResources() {
    return resources;
  }

  public Network getNetwork() {
    return network;
  }

  public Ot getDummyOtInstance() {
    return new DummyOt(resources.getOtherId(), network);
  }

  public int getMyId() {
    return resources.getMyId();
  }

  public int getOtherId() {
    return resources.getOtherId();
  }

  public int getLambdaSecurityParam() {
    return resources.getLambdaSecurityParam();
  }

  public int getKbitLength() {
    return resources.getComputationalSecurityParameter();
  }

  public Drbg getRand() {
    return resources.getRandomGenerator();
  }

  public MessageDigest getDigest() {
    return resources.getDigest();
  }
}
