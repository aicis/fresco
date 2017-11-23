package dk.alexandra.fresco.tools.ot.base;

import java.util.Random;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.BitVector;
import dk.alexandra.fresco.framework.util.Pair;

public class DummyRot implements Rot<BitVector> {

  private Ot<BitVector> dummyOt;
  private int messageBitLength;
  private Random rand;

  public DummyRot(int otherId, Network network, Random rand, int bitLength) {
    super();
    this.rand = rand;
    this.messageBitLength = bitLength;
    this.dummyOt = new DummyOt(otherId, messageBitLength, network);
  }

  @Override
  public BitVector receive(Boolean choiceBit) {
    return this.dummyOt.receive(choiceBit);
  }

  @Override
  public Pair<BitVector, BitVector> send() {
    BitVector messageZero = new BitVector(this.messageBitLength, this.rand);
    BitVector messageOne = new BitVector(this.messageBitLength, this.rand);
    this.dummyOt.send(messageZero, messageOne);
    return new Pair<>(messageZero, messageOne);
  }

}
