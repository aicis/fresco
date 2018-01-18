package dk.alexandra.fresco.tools.mascot;

import dk.alexandra.fresco.commitment.HashBasedCommitment;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.tools.mascot.field.FieldElementSerializer;
import dk.alexandra.fresco.tools.mascot.field.FieldElementUtils;
import dk.alexandra.fresco.tools.mascot.utils.FieldElementPrg;
import java.math.BigInteger;
import java.security.MessageDigest;

/**
 * Class that stores data and resources common across all MPC protocols.
 */
public abstract class BaseProtocol {

  private final MascotResourcePool resourcePool;
  private final Network network;
  private final FieldElementUtils fieldElementUtils;

  /**
   * Creates new {@link BaseProtocol}.
   *
   * @param resourcePool mascot resource pool
   * @param network network
   */
  public BaseProtocol(MascotResourcePool resourcePool, Network network) {
    this.resourcePool = resourcePool;
    this.network = network;
    this.fieldElementUtils = new FieldElementUtils(getModulus());
  }

  public int getMyId() {
    return resourcePool.getMyId();
  }

  public BigInteger getModulus() {
    return resourcePool.getModulus();
  }

  public int getModBitLength() {
    return resourcePool.getModBitLength();
  }

  protected int getLambdaSecurityParam() {
    return resourcePool.getLambdaSecurityParam();
  }

  public FieldElementSerializer getFieldElementSerializer() {
    return resourcePool.getFieldElementSerializer();
  }

  protected FieldElementPrg getLocalSampler() {
    return resourcePool.getLocalSampler();
  }

  public FieldElementUtils getFieldElementUtils() {
    return fieldElementUtils;
  }

  protected Network getNetwork() {
    return network;
  }

  public MessageDigest getMessageDigest() {
    return resourcePool.getMessageDigest();
  }

  protected Drbg getRandomGenerator() {
    return resourcePool.getRandomGenerator();
  }

  protected ByteSerializer<HashBasedCommitment> getCommitmentSerializer() {
    return resourcePool.getCommitmentSerializer();
  }

  protected int getNumCandidatesPerTriple() {
    return resourcePool.getNumCandidatesPerTriple();
  }

  protected int getNoOfParties() {
    return resourcePool.getNoOfParties();
  }

}
