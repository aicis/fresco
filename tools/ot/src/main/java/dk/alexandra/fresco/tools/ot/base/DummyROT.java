package dk.alexandra.fresco.tools.ot.base;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.Pair;
import java.math.BigInteger;
import java.util.Random;

public class DummyROT implements ROT<BigInteger> {

  private OT<BigInteger> dummyOT;
  private int bitLength;
  private Random rand;

  public DummyROT(int otherID, Network network, Random rand, int bitLength) {
    super();
    this.rand = rand;
    this.bitLength = bitLength;
    this.dummyOT = new DummyOT(otherID, network);
  }

  @Override
  public BigInteger receive(Boolean choiceBit) {
    return this.dummyOT.receive(choiceBit);
  }

  @Override
  public Pair<BigInteger, BigInteger> send() {
    BigInteger messageZero = new BigInteger(this.bitLength, this.rand);
    BigInteger messageOne = new BigInteger(this.bitLength, this.rand);
    this.dummyOT.send(messageZero, messageOne);
    return new Pair<>(messageZero, messageOne);
  }

}
