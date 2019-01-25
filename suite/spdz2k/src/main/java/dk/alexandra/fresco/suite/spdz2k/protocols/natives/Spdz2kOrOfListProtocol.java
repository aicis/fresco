package dk.alexandra.fresco.suite.spdz2k.protocols.natives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.OpenedValueStore;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUIntFactory;
import dk.alexandra.fresco.suite.spdz2k.datatypes.Spdz2kSIntArithmetic;
import dk.alexandra.fresco.suite.spdz2k.datatypes.Spdz2kSIntBoolean;
import dk.alexandra.fresco.suite.spdz2k.datatypes.Spdz2kTriple;
import dk.alexandra.fresco.suite.spdz2k.resource.Spdz2kResourcePool;
import java.util.ArrayList;
import java.util.List;

public class Spdz2kOrOfListProtocol<PlainT extends CompUInt<?, ?, PlainT>> extends
    Spdz2kNativeProtocol<SInt, PlainT> {

  private final DRes<List<DRes<SInt>>> bitsDef;
  private SInt res;
  private List<DRes<SInt>> nextRound;
  private List<DRes<SInt>> bitsA;
  private List<DRes<SInt>> bitsB;
  private List<Spdz2kTriple<PlainT, Spdz2kSIntBoolean<PlainT>>> triples;
  private List<Spdz2kSIntBoolean<PlainT>> epsilons;
  private List<Spdz2kSIntBoolean<PlainT>> deltas;
  private List<PlainT> openEpsilons;
  private List<PlainT> openDeltas;
  private SInt extraBit;

  public Spdz2kOrOfListProtocol(DRes<List<DRes<SInt>>> bits) {
    this.bitsDef = bits;
  }

  @Override
  public EvaluationStatus evaluate(int round, Spdz2kResourcePool<PlainT> resourcePool,
      Network network) {
    PlainT macKeyShare = resourcePool.getDataSupplier().getSecretSharedKey();
    CompUIntFactory<PlainT> factory = resourcePool.getFactory();
//    System.out.println("Running round " + round);

    if (round % 2 == 0) {
      if (nextRound == null) {
        nextRound = bitsDef.out();
      }
      final int nextRoundSize = nextRound.size();
      if (nextRoundSize == 1) {
        res = nextRound.get(0).out();
        return EvaluationStatus.IS_DONE;
      }

      bitsA = new ArrayList<>(nextRoundSize / 2);
      bitsB = new ArrayList<>(nextRoundSize / 2);
      for (int i = 0; i < nextRoundSize - 1; i += 2) {
        bitsA.add(nextRound.get(i));
        bitsB.add(nextRound.get(i + 1));
      }
      triples = new ArrayList<>(nextRoundSize / 2);
      epsilons = new ArrayList<>(nextRoundSize / 2);
      deltas = new ArrayList<>(nextRoundSize / 2);
      openEpsilons = new ArrayList<>(nextRoundSize / 2);
      openDeltas = new ArrayList<>(nextRoundSize / 2);
      // reset next round
      final boolean isOdd = nextRoundSize % 2 != 0;
      if (isOdd) {
        extraBit = nextRound.get(nextRoundSize - 1).out();
      } else {
        extraBit = null;
      }
      nextRound = new ArrayList<>(nextRoundSize / 2);
      for (int i = 0; i < bitsA.size(); i++) {
        Spdz2kTriple<PlainT, Spdz2kSIntBoolean<PlainT>> triple = resourcePool
            .getDataSupplier()
            .getNextBitTripleShares();
        triples.add(triple);

        Spdz2kSIntBoolean<PlainT> left = factory.toSpdz2kSIntBoolean(bitsA.get(i));
        Spdz2kSIntBoolean<PlainT> right = factory.toSpdz2kSIntBoolean(bitsB.get(i));

        epsilons.add(factory.toSpdz2kSIntBoolean(left).xor(triple.getLeft()));
        deltas.add(factory.toSpdz2kSIntBoolean(right).xor(triple.getRight()));
      }
      serializeAndSend(network, epsilons, deltas);
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else {
      receiveAndReconstruct(network, factory, resourcePool.getNoOfParties());

      OpenedValueStore<Spdz2kSIntArithmetic<PlainT>, PlainT> openedValueStore = resourcePool
          .getOpenedValueStore();

      for (int i = 0; i < bitsA.size(); i++) {
        Spdz2kTriple<PlainT, Spdz2kSIntBoolean<PlainT>> triple = triples.get(i);

        PlainT e = openEpsilons.get(i);
        openedValueStore.pushOpenedValue(epsilons.get(i).asArithmetic(), e.toArithmeticRep());
        PlainT d = openDeltas.get(i);
        openedValueStore.pushOpenedValue(deltas.get(i).asArithmetic(), d.toArithmeticRep());

        Spdz2kSIntBoolean<PlainT> prod = andAfterReceive(e, d, triple, macKeyShare, factory,
            resourcePool.getMyId());
        Spdz2kSIntBoolean<PlainT> xored = factory.toSpdz2kSIntBoolean(bitsA.get(i))
            .xor(factory.toSpdz2kSIntBoolean(bitsB.get(i)));

        nextRound.add(prod.xor(xored));
      }
      if (extraBit != null) {
        nextRound.add(extraBit);
      }
      return EvaluationStatus.HAS_MORE_ROUNDS;
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
  public SInt out() {
    return res;
  }

}
