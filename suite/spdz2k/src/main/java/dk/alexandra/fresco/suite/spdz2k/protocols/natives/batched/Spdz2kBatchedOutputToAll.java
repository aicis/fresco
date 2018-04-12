package dk.alexandra.fresco.suite.spdz2k.protocols.natives.batched;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.Deferred;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.OpenedValueStore;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUIntFactory;
import dk.alexandra.fresco.suite.spdz2k.datatypes.Spdz2kSInt;
import dk.alexandra.fresco.suite.spdz2k.protocols.natives.RequiresMacCheck;
import dk.alexandra.fresco.suite.spdz2k.protocols.natives.Spdz2kNativeProtocol;
import dk.alexandra.fresco.suite.spdz2k.resource.Spdz2kResourcePool;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

/**
 * A batched native protocol for opening secret values. <p>This protocol accumulates values to be
 * opened in a batch and opens them once evaluate is called.</p>
 */
public class Spdz2kBatchedOutputToAll<PlainT extends CompUInt<?, ?, PlainT>> extends
    Spdz2kNativeProtocol<Void, PlainT> implements RequiresMacCheck {

  private List<Spdz2kSInt<PlainT>> authenticated;
  private final Deque<DRes<SInt>> shares;
  private final Deque<Deferred<BigInteger>> opened;

  public Spdz2kBatchedOutputToAll() {
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
  public EvaluationStatus evaluate(int round, Spdz2kResourcePool<PlainT> resourcePool,
      Network network) {
    OpenedValueStore<Spdz2kSInt<PlainT>, PlainT> openedValueStore = resourcePool
        .getOpenedValueStore();
    if (round == 0) {
      authenticated = new ArrayList<>(shares.size());
      while (!shares.isEmpty()) {
        authenticated.add(toSpdz2kSInt(shares.pop()));
      }
      serializeAndSend(network, resourcePool.getFactory());
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else {
      List<PlainT> reconstructed = receiveAndReconstruct(network, resourcePool.getFactory(),
          resourcePool.getMyId(), resourcePool.getNoOfParties());
      for (int i = 0; i < reconstructed.size(); i++) {
        PlainT reconstructedElement = reconstructed.get(i);
        openedValueStore.pushOpenedValue(authenticated.get(i), reconstructedElement);
        opened.pop().callback(resourcePool.convertRepresentation(reconstructedElement));
      }
      reconstructed.clear();
      authenticated.clear();
      return EvaluationStatus.IS_DONE;
    }
  }

  @Override
  public Void out() {
    return null;
  }

  /**
   * Serializes and sends shares of values to be opened.
   */
  private void serializeAndSend(Network network, CompUIntFactory<PlainT> factory) {
    int byteLength = factory.getLowBitLength() / Byte.SIZE;
    byte[] bytes = new byte[authenticated.size() * byteLength];
    for (int i = 0; i < authenticated.size(); i++) {
      PlainT share = authenticated.get(i).getShare();
      byte[] serialized = share.getLeastSignificant().toByteArray();
      System.arraycopy(serialized, 0, bytes, i * byteLength, byteLength);
    }
    network.sendToAll(bytes);
  }

  /**
   * Receives shares for opened values and reconstructs each.
   */
  private List<PlainT> receiveAndReconstruct(Network network, CompUIntFactory<PlainT> factory,
      int myId,
      int noOfParties) {
    List<PlainT> reconstructed = new ArrayList<>(authenticated.size());
    for (Spdz2kSInt<PlainT> authenticatedElement : authenticated) {
      reconstructed.add(authenticatedElement.getShare().clearHigh());
    }
    int byteLength = factory.getLowBitLength() / Byte.SIZE;
    for (int partyId = 1; partyId <= noOfParties; partyId++) {
      // need to receive own shares to clear buffers
      byte[] raw = network.receive(partyId);
      if (myId != partyId) {
        for (int j = 0; j < authenticated.size(); j++) {
          int chunkIndex = j * byteLength;
          reconstructed.set(j, reconstructed.get(j)
              .add(factory.createFromBytes(raw, chunkIndex, byteLength)));
        }
      }
    }
    return reconstructed;
  }

}
