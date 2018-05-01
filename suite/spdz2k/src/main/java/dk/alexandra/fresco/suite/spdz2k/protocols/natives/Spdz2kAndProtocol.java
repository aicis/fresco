package dk.alexandra.fresco.suite.spdz2k.protocols.natives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
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
    final PlainT macKeyShare = resourcePool.getDataSupplier().getSecretSharedKey();
    ByteSerializer<PlainT> serializer = resourcePool.getPlainSerializer();
    CompUIntFactory<PlainT> factory = resourcePool.getFactory();
    if (round == 0) {
      triple = resourcePool.getDataSupplier().getNextBitTripleShares();
      epsilon = factory.toSpdz2kSIntBoolean(left).subtract(triple.getLeft());
      delta = factory.toSpdz2kSIntBoolean(right).subtract(triple.getRight());
      network.sendToAll(epsilon.serializeShareLow());
      network.sendToAll(delta.serializeShareLow());
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else {
      Pair<PlainT, PlainT> epsilonAndDelta = receiveAndReconstruct(network,
          factory,
          resourcePool.getNoOfParties(),
          serializer);
      // compute [prod] = [c] XOR epsilon AND [b] XOR delta AND [a] XOR epsilon AND delta
      PlainT e = epsilonAndDelta.getFirst();
      PlainT d = epsilonAndDelta.getSecond();
      PlainT ed = e.multiply(d);
      Spdz2kSIntBoolean<PlainT> tripleRight = triple.getRight();
      Spdz2kSIntBoolean<PlainT> tripleLeft = triple.getLeft();
      Spdz2kSIntBoolean<PlainT> tripleProduct = triple.getProduct();
      this.product = tripleProduct
          .add(tripleRight.multiply(e))
          .add(tripleLeft.multiply(d))
          .addConstant(ed,
              macKeyShare,
              factory.zero(),
              resourcePool.getMyId() == 1);
      resourcePool.getOpenedValueStore().pushOpenedValues(
          Arrays.asList(
              epsilon.asArithmetic(),
              delta.asArithmetic()
          ),
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
    PlainT e = factory.zero().toBitRep();
    PlainT d = factory.zero().toBitRep();
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
