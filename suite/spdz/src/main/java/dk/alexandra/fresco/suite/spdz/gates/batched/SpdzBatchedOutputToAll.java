package dk.alexandra.fresco.suite.spdz.gates.batched;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.util.OpenedValueStore;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePool;
import dk.alexandra.fresco.suite.spdz.datatypes.Deferred;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.gates.SpdzNativeProtocol;
import dk.alexandra.fresco.suite.spdz.gates.SpdzRequiresMacCheck;
import dk.alexandra.fresco.suite.spdz.utils.SpdzSIntShareSerializer;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

/**
 * A batched native protocol for opening secret values. <p>This protocol accumulates values to be
 * opened in a batch and opens them once evaluate is called.</p>
 */
public class SpdzBatchedOutputToAll extends SpdzNativeProtocol<Void> implements
    SpdzRequiresMacCheck {

  private List<SpdzSInt> authenticated;
  private final Deque<DRes<SInt>> shares;
  private final Deque<Deferred<BigInteger>> opened;

  public SpdzBatchedOutputToAll() {
    shares = new LinkedList<>();
    opened = new LinkedList<>();
  }

  public DRes<BigInteger> append(DRes<SInt> input) {
    Deferred<BigInteger> deferred = new Deferred<>();
    shares.add(input);
    opened.add(deferred);
    return deferred;
  }

  @Override
  public EvaluationStatus evaluate(int round, SpdzResourcePool resourcePool, Network network) {
    OpenedValueStore<SpdzSInt, BigInteger> openedValueStore = resourcePool.getOpenedValueStore();
    int byteLength = resourcePool.getModBitLength() / Byte.SIZE;
    ByteSerializer<SpdzSInt> shareSerializer = new SpdzSIntShareSerializer(
        resourcePool.getSerializer(), byteLength);
    if (round == 0) {
      authenticated = new ArrayList<>(shares.size());
      while (!shares.isEmpty()) {
        SpdzSInt sInt = SpdzSInt.toSpdzSInt(shares.pop());
        authenticated.add(sInt);
      }
      network.sendToAll(shareSerializer.serialize(authenticated));
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else {
      final List<BigInteger> reconstructed = receiveAndReconstruct(network,
          resourcePool.getSerializer(), resourcePool.getModulus(), resourcePool.getMyId(),
          resourcePool.getNoOfParties());
      openedValueStore.pushOpenedValues(authenticated, reconstructed);
      for (BigInteger reconstructedElement : reconstructed) {
        opened.pop().callback(resourcePool.convertRepresentation(reconstructedElement));
      }
      reconstructed.clear();
      return EvaluationStatus.IS_DONE;
    }
  }

  @Override
  public Void out() {
    return null;
  }

  /**
   * Receives shares for epsilons and deltas and reconstructs each.
   */
  private List<BigInteger> receiveAndReconstruct(Network network,
      ByteSerializer<BigInteger> serializer,
      BigInteger modulus, int myId, int noOfParties) {
    List<BigInteger> reconstructed = new ArrayList<>(authenticated.size());
    for (SpdzSInt anAuthenticated : authenticated) {
      reconstructed.add(anAuthenticated.getShare());
    }
    for (int partyId = 1; partyId <= noOfParties; partyId++) {
      // need to receive own shares to clear buffers
      byte[] raw = network.receive(partyId);
      if (myId != partyId) {
        List<BigInteger> thisPartyShares = serializer.deserializeList(raw);
        for (int j = 0; j < reconstructed.size(); j++) {
          reconstructed.set(j, reconstructed.get(j).add(thisPartyShares.get(j)));
        }
      }
    }
    for (int j = 0; j < reconstructed.size(); j++) {
      reconstructed.set(j, reconstructed.get(j).mod(modulus));
    }
    return reconstructed;
  }

}
