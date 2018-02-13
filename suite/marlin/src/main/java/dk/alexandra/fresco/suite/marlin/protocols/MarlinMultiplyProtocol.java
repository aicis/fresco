package dk.alexandra.fresco.suite.marlin.protocols;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.ByteAndBitConverter;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.marlin.datatypes.BigUInt;
import dk.alexandra.fresco.suite.marlin.datatypes.BigUIntFactory;
import dk.alexandra.fresco.suite.marlin.datatypes.MarlinSInt;
import dk.alexandra.fresco.suite.marlin.datatypes.MarlinTriple;
import dk.alexandra.fresco.suite.marlin.resource.MarlinResourcePool;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MarlinMultiplyProtocol<T extends BigUInt<T>> extends
    MarlinNativeProtocol<SInt, T> {

  private final DRes<SInt> left;
  private final DRes<SInt> right;
  private MarlinTriple<T> triple;
  private SInt product;
  private MarlinSInt<T> epsilon;
  private MarlinSInt<T> delta;

  public MarlinMultiplyProtocol(DRes<SInt> left, DRes<SInt> right) {
    this.left = left;
    this.right = right;
  }

  @Override
  public EvaluationStatus evaluate(int round, MarlinResourcePool<T> resourcePool, Network network) {
    final T macKeyShare = resourcePool.getDataSupplier().getSecretSharedKey();
    final BigUIntFactory<T> factory = resourcePool.getFactory();
    if (round == 0) {
      triple = resourcePool.getDataSupplier().getNextTripleShares();
      epsilon = ((MarlinSInt<T>) left.out()).subtract(triple.getLeft());
      delta = ((MarlinSInt<T>) right.out()).subtract(triple.getRight());
      // we only send the lower bits so we can't use serializer here
      network.sendToAll(ByteAndBitConverter.toByteArray(epsilon.getShare().getLow()));
      network.sendToAll(ByteAndBitConverter.toByteArray(delta.getShare().getLow()));
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else {
      Pair<T, T> epsilonAndDelta = receiveAndReconstruct(network, resourcePool.getNoOfParties(),
          factory);
      // compute [prod] = [c] + epsilon * [b] + delta * [a] + epsilon * delta
      T e = epsilonAndDelta.getFirst();
      T d = epsilonAndDelta.getSecond();
      T ed = e.multiply(d);
      product = triple.getProduct()
          .add(triple.getRight().multiply(e))
          .add(triple.getLeft().multiply(d))
          .addConstant(ed, resourcePool.getMyId(), macKeyShare, factory.zero());
      resourcePool.getOpenedValueStore().pushOpenedValues(
          Arrays.asList(epsilon, delta),
          Arrays.asList(e, d)
      );
      // TODO is this really necessary?
      triple = null;
      epsilon = null;
      delta = null;
      return EvaluationStatus.IS_DONE;
    }
  }

  /**
   * Retrieves shares for epsilon and delta and reconstructs each.
   */
  private Pair<T, T> receiveAndReconstruct(Network network, int noOfParties,
      BigUIntFactory<T> factory) {
    List<T> epsilonShares = new ArrayList<>(noOfParties);
    List<T> deltaShares = new ArrayList<>(noOfParties);
    // TODO figure out clean way to deal with long to BigUInt conversion
    for (int i = 1; i <= noOfParties; i++) {
      epsilonShares.add(factory.createFromBytes(network.receive(i)));
      deltaShares.add(factory.createFromBytes(network.receive(i)));
    }
    T e = BigUInt.sum(epsilonShares);
    T d = BigUInt.sum(deltaShares);
    return new Pair<>(e, d);
  }

  @Override
  public SInt out() {
    return product;
  }

}
