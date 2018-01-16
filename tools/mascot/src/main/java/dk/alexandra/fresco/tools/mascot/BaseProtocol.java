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
import java.util.List;

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
    this.fieldElementUtils = new FieldElementUtils(getModulus(), getModBitLength());
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

  public int getLambdaSecurityParam() {
    return resourcePool.getLambdaSecurityParam();
  }

  public FieldElementSerializer getFieldElementSerializer() {
    return resourcePool.getFieldElementSerializer();
  }

  public FieldElementPrg getLocalSampler() {
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

  public Drbg getRandomGenerator() {
    return resourcePool.getRandomGenerator();
  }

  public ByteSerializer<HashBasedCommitment> getCommitmentSerializer() {
    return resourcePool.getCommitmentSerializer();
  }

  public List<Integer> getPartyIds() {
    return resourcePool.getPartyIds();
  }

  public int getNumCandidatesPerTriple() {
    return resourcePool.getNumCandidatesPerTriple();
  }

}
