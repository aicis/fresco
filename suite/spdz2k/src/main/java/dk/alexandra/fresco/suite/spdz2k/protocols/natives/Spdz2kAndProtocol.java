package dk.alexandra.fresco.suite.spdz2k.protocols.natives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUIntFactory;
import dk.alexandra.fresco.suite.spdz2k.datatypes.Spdz2kSIntBoolean;
import dk.alexandra.fresco.suite.spdz2k.datatypes.Spdz2kTriple;
import dk.alexandra.fresco.suite.spdz2k.resource.Spdz2kResourcePool;
import java.util.Arrays;

/**
 * Native protocol for computing logical AND of two values in boolean form.
 */
public class Spdz2kAndProtocol<PlainT extends CompUInt<?, ?, PlainT>> extends
    Spdz2kNativeProtocol<SInt, PlainT> {

  private final DRes<SInt> left;
  private final DRes<SInt> right;
  private Spdz2kTriple<PlainT, Spdz2kSIntBoolean<PlainT>> triple;
  private Spdz2kSIntBoolean<PlainT> epsilon;
  private Spdz2kSIntBoolean<PlainT> delta;
  private SInt product;

  /**
   * Creates new {@link dk.alexandra.fresco.suite.spdz2k.protocols.natives.Spdz2kMultiplyProtocol}.
   *
   * @param left left factor
   * @param right right factor
   */
  public Spdz2kAndProtocol(DRes<SInt> left, DRes<SInt> right) {
    this.left = left;
    this.right = right;
  }

  @Override
  public EvaluationStatus evaluate(int round, Spdz2kResourcePool<PlainT> resourcePool,
      Network network) {
    PlainT macKeyShare = resourcePool.getDataSupplier().getSecretSharedKey();
    CompUIntFactory<PlainT> factory = resourcePool.getFactory();
    if (round == 0) {
      triple = resourcePool.getDataSupplier().getNextBitTripleShares();
      epsilon = factory.toSpdz2kSIntBoolean(left).xor(triple.getLeft());
      delta = factory.toSpdz2kSIntBoolean(right).xor(triple.getRight());
      int packed = epsilon.serializeShareLow()[0] ^ (delta.serializeShareLow()[0] << 1);
      final byte[] bytes = new byte[]{(byte) packed};
      network.sendToAll(bytes);
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else {
      Pair<PlainT, PlainT> epsilonAndDelta = receiveAndReconstruct(network,
          resourcePool.getNoOfParties(),
          factory);
      PlainT e = epsilonAndDelta.getFirst();
      PlainT d = epsilonAndDelta.getSecond();
      resourcePool.getOpenedValueStore().pushOpenedValues(
          Arrays.asList(
              epsilon.asArithmetic(),
              delta.asArithmetic()
          ),
          Arrays.asList(
              e.toArithmeticRep(),
              d.toArithmeticRep()
          )
      );
      int eBit = e.bitValue();
      int dBit = d.bitValue();
      PlainT ed = e.multiply(d);
      // compute [prod] = [c] XOR epsilon AND [b] XOR delta AND [a] XOR epsilon AND delta
      Spdz2kSIntBoolean<PlainT> tripleLeft = triple.getLeft();
      Spdz2kSIntBoolean<PlainT> tripleRight = triple.getRight();
      Spdz2kSIntBoolean<PlainT> tripleProduct = triple.getProduct();
      this.product = tripleProduct
          .xor(tripleRight.and(eBit))
          .xor(tripleLeft.and(dBit))
          .xorOpen(ed,
              macKeyShare,
              factory.zero().toBitRep(),
              resourcePool.getMyId() == 1);
      return EvaluationStatus.IS_DONE;
    }
  }

  /**
   * Retrieves shares for epsilon and delta and reconstructs each.
   */
  private Pair<PlainT, PlainT> receiveAndReconstruct(Network network,
      int noOfParties, CompUIntFactory<PlainT> factory) {
    int received = network.receive(1)[0];
    PlainT e = factory.fromBit(received & 1); // first bit
    PlainT d = factory.fromBit((received & 2) >>> 1); // second bit
    for (int i = 2; i <= noOfParties; i++) {
      received = network.receive(i)[0];
      e = e.add(factory.fromBit(received & 1));
      d = d.add(factory.fromBit((received & 2) >>> 1));
    }
    return new Pair<>(e, d);
  }

  @Override
  public SInt out() {
    return product;
  }
}
