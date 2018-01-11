package dk.alexandra.fresco.tools.ot.otextension;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import dk.alexandra.fresco.framework.util.ByteArrayHelper;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.PaddingAesCtrDrbg;
import dk.alexandra.fresco.tools.cointossing.CoinTossing;
import dk.alexandra.fresco.tools.helper.Constants;
import dk.alexandra.fresco.tools.helper.TestRuntime;
import dk.alexandra.fresco.tools.ot.base.DummyOt;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class OtExtensionTestContext {
  private final Network network;
  private final int myId;
  private final int otherId;
  private final int kbitLength;
  private final int lambdaSecurityParam;
  private final RotList seedOts;

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
    this.network = new CheatingNetwork(TestRuntime.defaultNetworkConfiguration(
        myId, Arrays.asList(1, 2)));
    DummyOt dummyOt = new DummyOt(otherId, network);
    Drbg rand = new AesCtrDrbg(Constants.seedOne);
    this.seedOts = new RotList(rand, kbitLength);
    this.seedOts.send(dummyOt);
    this.seedOts.receive(dummyOt);
    this.myId = myId;
    this.otherId = otherId;
    this.kbitLength = kbitLength;
    this.lambdaSecurityParam = lambdaSecurityParam;
  }

  public OtExtensionResourcePool createResources(int instanceId) {
    Drbg rand = new AesCtrDrbg(Constants.seedOne);
    CoinTossing ct = new CoinTossing(myId, otherId, rand, network);
    ct.initialize();
    return new OtExtensionResourcePoolImpl(myId, otherId, kbitLength,
        lambdaSecurityParam, instanceId, rand, ct, seedOts);
  }

  public Network getNetwork() {
    return network;
  }
  //
  // public int getMyId() {
  // return resources.getMyId();
  // }
  //
  // public int getOtherId() {
  // return resources.getOtherId();
  // }
  //
  // public int getLambdaSecurityParam() {
  // return resources.getLambdaSecurityParam();
  // }
  //
  // public int getKbitLength() {
  // return resources.getComputationalSecurityParameter();
  // }
  //
  public Drbg createRand(int instanceId) {
    ByteBuffer idBuffer = ByteBuffer.allocate(Constants.seedOne.length);
    byte[] seedBytes = idBuffer.putInt(instanceId).array();
    ByteArrayHelper.xor(seedBytes, Constants.seedOne);
    // TODO make sure this is okay!
    return new PaddingAesCtrDrbg(seedBytes, 256);
  }
  //
  // public MessageDigest getDigest() {
  // return resources.getDigest();
  // }
}
