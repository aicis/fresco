package dk.alexandra.fresco.suite.spdz2k.protocols.natives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUIntFactory;
import dk.alexandra.fresco.suite.spdz2k.datatypes.Spdz2kSInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.Spdz2kTriple;
import dk.alexandra.fresco.suite.spdz2k.resource.Spdz2kResourcePool;
import java.util.Arrays;

/**
 * Native protocol for computing product of two secret numbers.
 */
public class Spdz2kMultiplyProtocol<PlainT extends CompUInt<?, ?, PlainT>> extends
    Spdz2kNativeProtocol<SInt, PlainT> {

  private final DRes<SInt> left;
  private final DRes<SInt> right;
  private Spdz2kTriple<PlainT> triple;
  private Spdz2kSInt<PlainT> epsilon;
  private Spdz2kSInt<PlainT> delta;
  private SInt product;

  /**
   * Creates new {@link Spdz2kMultiplyProtocol}.
   *
   * @param left left factor
   * @param right right factor
   */
  public Spdz2kMultiplyProtocol(DRes<SInt> left, DRes<SInt> right) {
    this.left = left;
    this.right = right;
  }

  @Override
  public EvaluationStatus evaluate(int round, Spdz2kResourcePool<PlainT> resourcePool,
      Network network) {
    final PlainT macKeyShare = resourcePool.getDataSupplier().getSecretSharedKey();
    ByteSerializer<PlainT> serializer = resourcePool.getPlainSerializer();
    if (round == 0) {
      triple = resourcePool.getDataSupplier().getNextTripleShares();
      epsilon = toSpdz2kSInt(left).subtract(triple.getLeft());
      delta = toSpdz2kSInt(right).subtract(triple.getRight());
      network.sendToAll(epsilon.getShare().getLeastSignificant().toByteArray());
      network.sendToAll(delta.getShare().getLeastSignificant().toByteArray());
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else {
      Pair<PlainT, PlainT> epsilonAndDelta = receiveAndReconstruct(network,
          resourcePool.getFactory(),
          resourcePool.getNoOfParties(),
          serializer);
      // compute [prod] = [c] + epsilon * [b] + delta * [a] + epsilon * delta
      PlainT e = epsilonAndDelta.getFirst();
      PlainT d = epsilonAndDelta.getSecond();
      PlainT ed = e.multiply(d);
      Spdz2kSInt<PlainT> tripleRight = triple.getRight();
      Spdz2kSInt<PlainT> tripleLeft = triple.getLeft();
      Spdz2kSInt<PlainT> tripleProduct = triple.getProduct();
      CompUIntFactory<PlainT> factory = resourcePool.getFactory();
      this.product = tripleProduct
          .add(tripleRight.multiply(e))
          .add(tripleLeft.multiply(d))
          .addConstant(ed,
              macKeyShare,
              factory.zero(),
              resourcePool.getMyId() == 1);
      resourcePool.getOpenedValueStore().pushOpenedValues(
          Arrays.asList(epsilon, delta),
          Arrays.asList(e, d)
      );
      return EvaluationStatus.IS_DONE;
    }
  }

  /**
   * Retrieves shares for epsilon and delta and reconstructs each.
   */
  private Pair<PlainT, PlainT> receiveAndReconstruct(Network network,
      CompUIntFactory<PlainT> factory, int noOfParties,
      ByteSerializer<PlainT> serializer) {
    PlainT e = factory.zero();
    PlainT d = factory.zero();
    for (int i = 1; i <= noOfParties; i++) {
      e = e.add(serializer.deserialize(network.receive(i)));
      d = d.add(serializer.deserialize(network.receive(i)));
    }
    return new Pair<>(e, d);
  }

  @Override
  public SInt out() {
    return product;
  }

}
