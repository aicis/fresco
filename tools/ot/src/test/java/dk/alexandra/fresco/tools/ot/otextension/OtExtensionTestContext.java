package dk.alexandra.fresco.tools.ot.otextension;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import dk.alexandra.fresco.framework.util.ByteArrayHelper;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.PaddingAesCtrDrbg;
import dk.alexandra.fresco.tools.cointossing.CoinTossing;
import dk.alexandra.fresco.tools.helper.HelperForTests;
import dk.alexandra.fresco.tools.helper.RuntimeForTests;
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
    this.network = new CheatingNetwork(RuntimeForTests.defaultNetworkConfiguration(
        myId, Arrays.asList(1, 2)));
    DummyOt dummyOt = new DummyOt(otherId, network);
    Drbg rand = new AesCtrDrbg(HelperForTests.seedOne);
    this.seedOts = new RotList(rand, kbitLength);
    if (myId < otherId) {
      this.seedOts.send(dummyOt);
      this.seedOts.receive(dummyOt);
    } else {
      this.seedOts.receive(dummyOt);
      this.seedOts.send(dummyOt);
    }
    this.myId = myId;
    this.otherId = otherId;
    this.kbitLength = kbitLength;
    this.lambdaSecurityParam = lambdaSecurityParam;
  }

  public Network getNetwork() {
    return network;
  }

  /**
   * Creates a new OT extension resource pool based on a specific instance ID 
   * and initializes necessary functionalities. This means it initializes coin
   * tossing using a randomness generator unique for {@code instanceId}.
   *
   * @param instanceId
   *          The id of the instance we wish to create a resource pool for
   * @return A new resources pool
   */
  public OtExtensionResourcePool createResources(int instanceId) {
    Drbg rand = createRand(instanceId);
    CoinTossing ct = new CoinTossing(myId, otherId, rand);
    ct.initialize(network);
    return new OtExtensionResourcePoolImpl(myId, otherId, kbitLength,
        lambdaSecurityParam, instanceId, rand, ct, seedOts);
  }

  /**
   * Creates a new randomness generator unique for {@code instanceId}.
   *
   * @param instanceId
   *          The ID which we wish to base the randomness generator on.
   * @return A new randomness generator unique for {@code instanceId}
   */
  public Drbg createRand(int instanceId) {
    ByteBuffer idBuffer = ByteBuffer.allocate(HelperForTests.seedOne.length);
    byte[] seedBytes = idBuffer.putInt(instanceId).array();
    ByteArrayHelper.xor(seedBytes, HelperForTests.seedOne);
    // TODO make sure this is okay!
    return new PaddingAesCtrDrbg(seedBytes);
  }
}
