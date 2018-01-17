package dk.alexandra.fresco.tools.ot.otextension;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.ByteArrayHelper;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.PaddingAesCtrDrbg;
import dk.alexandra.fresco.framework.util.StrictBitVector;

import java.nio.ByteBuffer;
import java.security.MessageDigest;

/**
 * Superclass containing the common variables and methods for the sender and
 * receiver parties of correlated OT with errors.
 */
public class CoteShared {
  private final OtExtensionResourcePool resources;
  private final Network network;

  /**
   * Constructs a correlated OT extension with errors super-class.
   *
   * @param resources
   *          The common resource pool needed for OT extension
   * @param network
   *          The network object used to communicate with the other party
   */
  public CoteShared(OtExtensionResourcePool resources, Network network) {
    super();
    this.resources = resources;
    this.network = network;
  }

  public int getOtherId() {
    return resources.getOtherId();
  }

  public int getInstanceId() {
    return resources.getInstanceId();
  }

  public int getkBitLength() {
    return resources.getComputationalSecurityParameter();
  }

  public int getLambdaSecurityParam() {
    return resources.getLambdaSecurityParam();
  }

  public Drbg getRand() {
    return resources.getRandomGenerator();
  }

  public MessageDigest getDigest() {
    return resources.getDigest();
  }

  public Network getNetwork() {
    return network;
  }

  /**
   * Initializes a PRG based on a seed and the internal instance ID.
   *
   * @param originalSeed
   *          The seed to initialize the PRG from
   * @return The initialized PRG
   */
  protected Drbg initPrg(StrictBitVector originalSeed) {
    // Remember that int is 32 bits, thus 4 bytes
    byte[] seedBytes = originalSeed.toByteArray();
    ByteBuffer idBuffer = ByteBuffer.allocate(seedBytes.length);
    byte[] idArray = idBuffer.putInt(resources.getInstanceId()).array();
    ByteArrayHelper.xor(seedBytes, idArray);
    // TODO make sure this is okay!
    return new PaddingAesCtrDrbg(seedBytes);
  }
}
