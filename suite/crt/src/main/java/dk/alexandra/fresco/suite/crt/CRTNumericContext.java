package dk.alexandra.fresco.suite.crt;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.NumericResourcePool;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericContext;
import dk.alexandra.fresco.suite.crt.datatypes.CRTSInt;
import dk.alexandra.fresco.suite.crt.datatypes.resource.CRTResourcePool;
import dk.alexandra.fresco.suite.crt.protocols.framework.ProtocolBuilderNumericWrapper;

import java.math.BigInteger;

public class CRTNumericContext<ResourcePoolA extends NumericResourcePool, ResourcePoolB extends NumericResourcePool> extends BasicNumericContext {

  private final BigInteger p, q;
  private final BuilderFactoryNumeric left;
  private final BuilderFactoryNumeric right;

  private final CRTResourcePool<ResourcePoolA, ResourcePoolB> resourcePool;

  /**
   * Construct a new BasicNumericContext.
   *
   * @param maxBitLength The maximum length in bits that the numbers in the application will have.
   * @param myId         my party id
   * @param noOfParties  number of parties in computation
   */
  public CRTNumericContext(int maxBitLength, int myId, int noOfParties,
                           BuilderFactoryNumeric left, BuilderFactoryNumeric right,
                           BigInteger p, BigInteger q, CRTResourcePool<ResourcePoolA, ResourcePoolB> resourcePool) {
    super(maxBitLength, myId, noOfParties, new CRTRingDefinition(p, q), p.bitLength());
    this.p = p;
    this.q = q;
    this.left = left;
    this.right = right;
    this.resourcePool = resourcePool;
  }

  /** Get the modulus of the left ring in the RNS representation. */
  public BigInteger getLeftModulus() {
    return p;
  }

  /** Get the modulus of the right ring in the RNS representation. */
  public BigInteger getRightModulus() {
    return q;
  }

  /**
   * Get a {@link BuilderFactoryNumeric} for the MPC system on the left ring.
   */
  public BuilderFactoryNumeric getLeft() {
    return left;
  }

  /**
   * Get a {@link BuilderFactoryNumeric} for the MPC system on the right ring.
   */
  public BuilderFactoryNumeric getRight() {
    return right;
  }


  public ResourcePoolA getResourcePoolLeft() {
    return resourcePool.getSubResourcePools().getFirst();
  }

  public ResourcePoolB getResourcePoolRight() {
    return resourcePool.getSubResourcePools().getSecond();
  }

  public CRTResourcePool<ResourcePoolA, ResourcePoolB> getResourcePool() {
    return resourcePool;
  }

  /**
   * Get an instance of Numeric for the left scheme using the given builder.
   */
  public Numeric leftNumeric(ProtocolBuilderNumeric builder) {
    return left.createNumeric(new ProtocolBuilderNumericWrapper<>(builder, left, resourcePool.getSubResourcePools().getFirst()));
  }

  /**
   * Get an instance of Numeric for the right scheme using the given builder.
   */
  public Numeric rightNumeric(ProtocolBuilderNumeric builder) {
    return right.createNumeric(new ProtocolBuilderNumericWrapper<>(builder, right, resourcePool.getSubResourcePools().getSecond()));
  }

  public DRes<SInt> getLeft(DRes<SInt> crtInteger) {
    if (crtInteger.out() == null || !(crtInteger.out() instanceof CRTSInt)) {
      throw new IllegalArgumentException("Input must be a non-null CRTSInt");
    }
    return ((CRTSInt) crtInteger).getLeft();
  }

  public DRes<SInt> getRight(DRes<SInt> crtInteger) {
    if (crtInteger.out() == null || !(crtInteger.out() instanceof CRTSInt)) {
      throw new IllegalArgumentException("Input must be a non-null CRTSInt");
    }
    return ((CRTSInt) crtInteger.out()).getRight();
  }

}
