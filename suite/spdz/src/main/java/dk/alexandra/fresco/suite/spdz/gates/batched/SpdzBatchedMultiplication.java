package dk.alexandra.fresco.suite.spdz.gates.batched;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePool;
import dk.alexandra.fresco.framework.Deferred;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzTriple;
import dk.alexandra.fresco.suite.spdz.gates.SpdzNativeProtocol;
import dk.alexandra.fresco.suite.spdz.utils.SpdzSIntShareSerializer;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

/**
 * A batched native protocol for multiplication. <p>This protocol accumulates values to be
 * multiplied in a batch and multiplies them once evaluate is called.</p>
 */
public class SpdzBatchedMultiplication extends SpdzNativeProtocol<Void> {

  private final Deque<DRes<SInt>> leftFactors;
  private final Deque<DRes<SInt>> rightFactors;
  private final Deque<Deferred<SInt>> products;
  private List<SpdzTriple> triples;
  private List<SpdzSInt> epsilons;
  private List<SpdzSInt> deltas;
  private List<BigInteger> openEpsilons;
  private List<BigInteger> openDeltas;

  public SpdzBatchedMultiplication() {
    this.leftFactors = new LinkedList<>();
    this.rightFactors = new LinkedList<>();
    this.products = new LinkedList<>();
  }

  public DRes<SInt> append(DRes<SInt> left, DRes<SInt> right) {
    Deferred<SInt> deferred = new Deferred<>();
    leftFactors.add(left);
    rightFactors.add(right);
    products.add(deferred);
    return deferred;
  }

  @Override
  public EvaluationStatus evaluate(int round, SpdzResourcePool resourcePool, Network network) {
    final BigInteger macKeyShare = resourcePool.getDataSupplier().getSecretSharedKey();
    final BigInteger modulus = resourcePool.getModulus();
    final ByteSerializer<SpdzSInt> shareSerializer = new SpdzSIntShareSerializer(
        resourcePool.getSerializer(), resourcePool.getModBitLength() / Byte.SIZE);
    if (round == 0) {
      epsilons = new ArrayList<>(leftFactors.size());
      deltas = new ArrayList<>(rightFactors.size());
      openEpsilons = new ArrayList<>(leftFactors.size());
      openDeltas = new ArrayList<>(leftFactors.size());
      triples = resourcePool.getDataSupplier().getNextTriples(leftFactors.size());
      int i = 0;
      while (!leftFactors.isEmpty()) {
        SpdzSInt left = SpdzSInt.toSpdzSInt(leftFactors.pop());
        SpdzSInt epsilon = left.subtract(triples.get(i).getA());
        epsilons.add(epsilon);
        SpdzSInt right = SpdzSInt.toSpdzSInt(rightFactors.pop());
        SpdzSInt delta = right.subtract(triples.get(i).getB());
        deltas.add(delta);
        i++;
      }
      serializeAndSend(network, shareSerializer);
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else {
      receiveAndReconstruct(network, resourcePool.getSerializer(), modulus,
          resourcePool.getMyId(), resourcePool.getNoOfParties());
      for (int i = 0; i < openEpsilons.size(); i++) {
        // compute [prod] = [c] + epsilons * [b] + deltas * [a] + epsilons * deltas
        BigInteger e = openEpsilons.get(i);
        BigInteger d = openDeltas.get(i);
        BigInteger ed = e.multiply(d).mod(modulus);
        SInt product = triples.get(i).getC()
            .add(triples.get(i).getB().multiply(e))
            .add(triples.get(i).getA().multiply(d))
            .addConstant(ed, macKeyShare, resourcePool.getModulus(),
                resourcePool.getMyId() == 1);
        products.pop().callback(product);
      }
      resourcePool.getOpenedValueStore().pushOpenedValues(epsilons, openEpsilons);
      resourcePool.getOpenedValueStore().pushOpenedValues(deltas, openDeltas);
      epsilons.clear();
      deltas.clear();
      return EvaluationStatus.IS_DONE;
    }
  }

  /**
   * Serializes and sends epsilon and delta values.
   */
  private void serializeAndSend(Network network, ByteSerializer<SpdzSInt> serializer) {
    network.sendToAll(serializer.serialize(epsilons));
    network.sendToAll(serializer.serialize(deltas));
  }

  /**
   * Receives shares for epsilons and deltas and reconstructs each.
   */
  private void receiveAndReconstruct(Network network, ByteSerializer<BigInteger> serializer,
      BigInteger modulus, int myId, int noOfParties) {
    for (int i = 0; i < epsilons.size(); i++) {
      openEpsilons.add(epsilons.get(i).getShare());
      openDeltas.add(deltas.get(i).getShare());
    }
    for (int partyId = 1; partyId <= noOfParties; partyId++) {
      // need to receive own shares to clear buffers
      byte[] rawEpsilons = network.receive(partyId);
      byte[] rawDeltas = network.receive(partyId);
      if (myId != partyId) {
        List<BigInteger> thisPartyEpsilons = serializer.deserializeList(rawEpsilons);
        List<BigInteger> thisPartyDeltas = serializer.deserializeList(rawDeltas);
        for (int j = 0; j < epsilons.size(); j++) {
          openEpsilons.set(j, openEpsilons.get(j).add(thisPartyEpsilons.get(j)));
          openDeltas.set(j, openDeltas.get(j).add(thisPartyDeltas.get(j)));
        }
      }
    }
    for (int j = 0; j < epsilons.size(); j++) {
      openEpsilons.set(j, openEpsilons.get(j).mod(modulus));
      openDeltas.set(j, openDeltas.get(j).mod(modulus));
    }
  }

  @Override
  public Void out() {
    return null;
  }

}
