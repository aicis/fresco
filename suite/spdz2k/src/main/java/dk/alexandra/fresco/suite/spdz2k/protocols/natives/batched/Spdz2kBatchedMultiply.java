package dk.alexandra.fresco.suite.spdz2k.protocols.natives.batched;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.Deferred;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUIntFactory;
import dk.alexandra.fresco.suite.spdz2k.datatypes.Spdz2kSInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.Spdz2kTriple;
import dk.alexandra.fresco.suite.spdz2k.protocols.natives.Spdz2kNativeProtocol;
import dk.alexandra.fresco.suite.spdz2k.resource.Spdz2kResourcePool;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

public class Spdz2kBatchedMultiply<PlainT extends CompUInt<?, ?, PlainT>> extends
    Spdz2kNativeProtocol<List<DRes<SInt>>, PlainT> {

  private final Deque<DRes<SInt>> leftFactors;
  private final Deque<DRes<SInt>> rightFactors;
  private final Deque<Deferred<SInt>> deferredProducts;
  private List<Spdz2kTriple<PlainT>> triples;
  private List<Spdz2kSInt<PlainT>> epsilons;
  private List<Spdz2kSInt<PlainT>> deltas;
  private List<PlainT> openEpsilons;
  private List<PlainT> openDeltas;

  public Spdz2kBatchedMultiply() {
    this.leftFactors = new LinkedList<>();
    this.rightFactors = new LinkedList<>();
    this.deferredProducts = new LinkedList<>();
  }

  public DRes<SInt> append(DRes<SInt> left, DRes<SInt> right) {
    Deferred<SInt> deferred = new Deferred<>();
    leftFactors.add(left);
    rightFactors.add(right);
    deferredProducts.add(deferred);
    return deferred;
  }

  @Override
  public EvaluationStatus evaluate(int round, Spdz2kResourcePool<PlainT> resourcePool,
      Network network) {
    PlainT macKeyShare = resourcePool.getDataSupplier().getSecretSharedKey();
    if (round == 0) {
      epsilons = new ArrayList<>(leftFactors.size());
      deltas = new ArrayList<>(leftFactors.size());
      openEpsilons = new ArrayList<>(leftFactors.size());
      openDeltas = new ArrayList<>(leftFactors.size());
      triples = resourcePool.getDataSupplier().getNextTripleShares(leftFactors.size());
      int i = 0;
      while (!leftFactors.isEmpty()) {
        Spdz2kSInt<PlainT> left = toSpdz2kSInt(leftFactors.pop());
        Spdz2kSInt<PlainT> epsilon = left.subtract(triples.get(i).getLeft());
        epsilons.add(epsilon);
        Spdz2kSInt<PlainT> right = toSpdz2kSInt(rightFactors.pop());
        Spdz2kSInt<PlainT> delta = right.subtract(triples.get(i).getRight());
        deltas.add(delta);
        i++;
      }
      serializeAndSend(network, resourcePool.getFactory(), epsilons, deltas);
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else {
      receiveAndReconstruct(network, resourcePool.getFactory(), resourcePool.getMyId(),
          resourcePool.getNoOfParties());
      PlainT zero = resourcePool.getFactory().zero();
      for (int i = 0; i < openEpsilons.size(); i++) {
        // compute [prod] = [c] + epsilons * [b] + deltas * [a] + epsilons * deltas
        PlainT e = openEpsilons.get(i);
        PlainT d = openDeltas.get(i);
        PlainT ed = e.multiply(d);
        SInt product = triples.get(i).getProduct()
            .add(triples.get(i).getRight().multiply(e))
            .add(triples.get(i).getLeft().multiply(d))
            .addConstant(ed, macKeyShare, zero, resourcePool.getMyId() == 1);
        deferredProducts.pop().callback(product);
      }
      resourcePool.getOpenedValueStore().pushOpenedValues(epsilons, openEpsilons);
      resourcePool.getOpenedValueStore().pushOpenedValues(deltas, openDeltas);
      epsilons.clear();
      deltas.clear();
      return EvaluationStatus.IS_DONE;
    }
  }

  /**
   * Retrieves shares for epsilons and deltas and reconstructs each.
   */
  private void receiveAndReconstruct(Network network, CompUIntFactory<PlainT> factory, int myId,
      int noOfParties) {
    for (int i = 0; i < epsilons.size(); i++) {
      openEpsilons.add(epsilons.get(i).getShare().clearHigh());
      openDeltas.add(deltas.get(i).getShare().clearHigh());
    }
    int byteLength = factory.getLowBitLength() / Byte.SIZE;
    for (int partyId = 1; partyId <= noOfParties; partyId++) {
      // need to receive own shares to clear buffers
      byte[] rawEpsilons = network.receive(partyId);
      byte[] rawDeltas = network.receive(partyId);
      if (myId != partyId) {
        for (int j = 0; j < epsilons.size(); j++) {
          int chunkIndex = j * byteLength;
          openEpsilons.set(j, openEpsilons.get(j)
              .add(factory.createFromBytes(rawEpsilons, chunkIndex, byteLength)));
          openDeltas.set(j, openDeltas.get(j)
              .add(factory.createFromBytes(rawDeltas, chunkIndex, byteLength)));
        }
      }
    }
  }

  /**
   * Serializes and sends epsilon and delta values.
   */
  private void serializeAndSend(Network network, CompUIntFactory<PlainT> factory,
      List<Spdz2kSInt<PlainT>> epsilons, List<Spdz2kSInt<PlainT>> deltas) {
    int byteLength = factory.getLowBitLength() / Byte.SIZE;
    byte[] epsilonBytes = new byte[epsilons.size() * byteLength];
    byte[] deltaBytes = new byte[epsilons.size() * byteLength];
    for (int i = 0; i < epsilons.size(); i++) {
      byte[] serializedEpsilon = epsilons.get(i).getShare().getLeastSignificant().toByteArray();
      byte[] serializedDelta = deltas.get(i).getShare().getLeastSignificant().toByteArray();
      System.arraycopy(serializedEpsilon, 0, epsilonBytes, i * byteLength, byteLength);
      System.arraycopy(serializedDelta, 0, deltaBytes, i * byteLength, byteLength);
    }
    network.sendToAll(epsilonBytes);
    network.sendToAll(deltaBytes);
  }

  @Override
  public List<DRes<SInt>> out() {
    return null;
  }

}
