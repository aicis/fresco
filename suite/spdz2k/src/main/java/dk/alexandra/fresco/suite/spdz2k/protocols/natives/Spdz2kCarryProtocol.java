package dk.alexandra.fresco.suite.spdz2k.protocols.natives;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.OpenedValueStore;
import dk.alexandra.fresco.framework.util.SIntPair;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUIntFactory;
import dk.alexandra.fresco.suite.spdz2k.datatypes.Spdz2kSIntArithmetic;
import dk.alexandra.fresco.suite.spdz2k.datatypes.Spdz2kSIntBoolean;
import dk.alexandra.fresco.suite.spdz2k.datatypes.Spdz2kTriple;
import dk.alexandra.fresco.suite.spdz2k.resource.Spdz2kResourcePool;
import java.util.ArrayList;
import java.util.List;

public class Spdz2kCarryProtocol<PlainT extends CompUInt<?, ?, PlainT>> extends
    Spdz2kNativeProtocol<List<SIntPair>, PlainT> {

  private final List<SIntPair> bits;
  private List<Spdz2kTriple<PlainT, Spdz2kSIntBoolean<PlainT>>> triples;
  private List<Spdz2kSIntBoolean<PlainT>> epsilons;
  private List<Spdz2kSIntBoolean<PlainT>> deltas;
  private List<PlainT> openEpsilons;
  private List<PlainT> openDeltas;
  private List<SIntPair> carried;

  public Spdz2kCarryProtocol(List<SIntPair> bits) {
    this.bits = bits;
  }

  @Override
  public EvaluationStatus evaluate(int round, Spdz2kResourcePool<PlainT> resourcePool,
      Network network) {
    PlainT macKeyShare = resourcePool.getDataSupplier().getSecretSharedKey();
    CompUIntFactory<PlainT> factory = resourcePool.getFactory();
    if (round == 0) {
      triples = new ArrayList<>(bits.size());
      epsilons = new ArrayList<>(bits.size());
      deltas = new ArrayList<>(bits.size());
      openEpsilons = new ArrayList<>(bits.size());
      openDeltas = new ArrayList<>(bits.size());
      carried = new ArrayList<>(bits.size() / 2);

      for (int i = 0; i < bits.size() / 2; i++) {
        // two multiplications
        triples.add(resourcePool.getDataSupplier().getNextBitTripleShares());
        triples.add(resourcePool.getDataSupplier().getNextBitTripleShares());

        SIntPair left = bits.get(2 * i + 1);
        SIntPair right = bits.get(2 * i);

        Spdz2kTriple<PlainT, Spdz2kSIntBoolean<PlainT>> p1p2Triple = triples.get(2 * i + 1);
        Spdz2kTriple<PlainT, Spdz2kSIntBoolean<PlainT>> p2g1Triple = triples.get(2 * i);

        Spdz2kSIntBoolean<PlainT> p1 = factory.toSpdz2kSIntBoolean(left.getFirst());
        Spdz2kSIntBoolean<PlainT> g1 = factory.toSpdz2kSIntBoolean(left.getSecond());
        Spdz2kSIntBoolean<PlainT> p2 = factory.toSpdz2kSIntBoolean(right.getFirst());

        // p2 * g1
        epsilons.add(factory.toSpdz2kSIntBoolean(p2).xor(p2g1Triple.getLeft()));
        deltas.add(factory.toSpdz2kSIntBoolean(g1).xor(p2g1Triple.getRight()));

        // p1 * p2
        epsilons.add(factory.toSpdz2kSIntBoolean(p1).xor(p1p2Triple.getLeft()));
        deltas.add(factory.toSpdz2kSIntBoolean(p2).xor(p1p2Triple.getRight()));
      }

      serializeAndSend(network, epsilons, deltas);
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else {
      receiveAndReconstruct(network, factory, resourcePool.getNoOfParties());

      OpenedValueStore<Spdz2kSIntArithmetic<PlainT>, PlainT> openedValueStore = resourcePool
          .getOpenedValueStore();

      for (int i = 0; i < bits.size() / 2; i++) {
        Spdz2kTriple<PlainT, Spdz2kSIntBoolean<PlainT>> p1p2Triple = triples.get(2 * i + 1);
        Spdz2kTriple<PlainT, Spdz2kSIntBoolean<PlainT>> p2g1Triple = triples.get(2 * i);

        PlainT p1p2E = openEpsilons.get(2 * i + 1);
        openedValueStore.pushOpenedValue(
            epsilons.get(2 * i + 1).asArithmetic(),
            p1p2E.toArithmeticRep()
        );
        PlainT p2g1E = openEpsilons.get(2 * i);
        openedValueStore.pushOpenedValue(
            epsilons.get(2 * i).asArithmetic(),
            p2g1E.toArithmeticRep()
        );

        PlainT p1p2D = openDeltas.get(2 * i + 1);
        openedValueStore.pushOpenedValue(
            deltas.get(2 * i + 1).asArithmetic(),
            p1p2D.toArithmeticRep()
        );
        PlainT p2g1D = openDeltas.get(2 * i);
        openedValueStore.pushOpenedValue(
            deltas.get(2 * i).asArithmetic(),
            p2g1D.toArithmeticRep()
        );
        Spdz2kSIntBoolean<PlainT> p = andAfterReceive(p1p2E, p1p2D, p1p2Triple, macKeyShare,
            factory,
            resourcePool.getMyId());

        Spdz2kSIntBoolean<PlainT> g2 = factory.toSpdz2kSIntBoolean(bits.get(2 * i).getSecond());

        Spdz2kSIntBoolean<PlainT> g = andAfterReceive(p2g1E, p2g1D, p2g1Triple, macKeyShare,
            factory,
            resourcePool.getMyId()).xor(g2);
        carried.add(new SIntPair(p, g));
      }
      // if we have an odd number of elements the last pair can just be taken directly from the input
      if (bits.size() % 2 != 0) {
        carried.add(bits.get(bits.size() - 1));
      }
      return EvaluationStatus.IS_DONE;
    }
  }

  private Spdz2kSIntBoolean<PlainT> andAfterReceive(
      PlainT e,
      PlainT d,
      Spdz2kTriple<PlainT, Spdz2kSIntBoolean<PlainT>> triple,
      PlainT macKeyShare,
      CompUIntFactory<PlainT> factory,
      int myId) {
    int eBit = e.bitValue();
    int dBit = d.bitValue();
    PlainT ed = e.multiply(d);
    // compute [prod] = [c] XOR epsilon AND [b] XOR delta AND [a] XOR epsilon AND delta
    Spdz2kSIntBoolean<PlainT> tripleLeft = triple.getLeft();
    Spdz2kSIntBoolean<PlainT> tripleRight = triple.getRight();
    Spdz2kSIntBoolean<PlainT> tripleProduct = triple.getProduct();
    return tripleProduct
        .xor(tripleRight.and(eBit))
        .xor(tripleLeft.and(dBit))
        .xorOpen(ed,
            macKeyShare,
            factory.zero().toBitRep(),
            myId == 1);
  }

  /**
   * Retrieves shares for epsilons and deltas and reconstructs each.
   */
  private void receiveAndReconstruct(Network network, CompUIntFactory<PlainT> factory,
      int noOfParties) {
    byte[] rawEpsilons = network.receive(1);
    byte[] rawDeltas = network.receive(1);

    for (int i = 0; i < epsilons.size(); i++) {
      int currentByteIdx = i / Byte.SIZE;
      int bitIndexWithinByte = i % Byte.SIZE;
      PlainT e = factory.fromBit((rawEpsilons[currentByteIdx] >>> bitIndexWithinByte));
      openEpsilons.add(e);
      PlainT d = factory.fromBit((rawDeltas[currentByteIdx] >>> bitIndexWithinByte));
      openDeltas.add(d);
    }

    for (int i = 2; i <= noOfParties; i++) {
      rawEpsilons = network.receive(i);
      rawDeltas = network.receive(i);

      for (int j = 0; j < epsilons.size(); j++) {
        int currentByteIdx = j / Byte.SIZE;
        int bitIndexWithinByte = j % Byte.SIZE;
        openEpsilons.set(j, openEpsilons.get(j).add(
            factory.fromBit((rawEpsilons[currentByteIdx] >>> bitIndexWithinByte))
        ));
        openDeltas.set(j, openDeltas.get(j).add(
            factory.fromBit((rawDeltas[currentByteIdx] >>> bitIndexWithinByte))
        ));
      }
    }
  }

  /**
   * Serializes and sends epsilon and delta values.
   */
  private void serializeAndSend(Network network,
      List<Spdz2kSIntBoolean<PlainT>> epsilons,
      List<Spdz2kSIntBoolean<PlainT>> deltas) {
    int numBytes = epsilons.size() / Byte.SIZE;
    if (epsilons.size() % 8 != 0) {
      numBytes++;
    }
    byte[] epsilonBytes = new byte[numBytes];
    byte[] deltaBytes = new byte[numBytes];

    for (int i = 0; i < epsilons.size(); i++) {
      int currentByteIdx = i / Byte.SIZE;
      int bitIndexWithinByte = i % Byte.SIZE;

      int serializedEpsilon = epsilons.get(i).getShare().bitValue();
      epsilonBytes[currentByteIdx] |= ((serializedEpsilon << bitIndexWithinByte)
          & (1 << bitIndexWithinByte));

      int serializedDelta = deltas
          .get(i)
          .getShare()
          .bitValue();
      deltaBytes[currentByteIdx] |= ((serializedDelta << bitIndexWithinByte)
          & (1 << bitIndexWithinByte));
    }
    network.sendToAll(epsilonBytes);
    network.sendToAll(deltaBytes);
  }

  @Override
  public List<SIntPair> out() {
    return carried;
  }

}
