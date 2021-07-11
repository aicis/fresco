package dk.alexandra.fresco.suite.tinytables.ot;

import dk.alexandra.fresco.tools.ot.base.AbstractNaorPinkasOT;
import dk.alexandra.fresco.tools.ot.base.BigIntNaorPinkas;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.StrictBitVector;

public class TinyTablesNaorPinkasOt implements TinyTablesOt {

  private final int otherId;
  private final Drbg random;
  private AbstractNaorPinkasOT ot;

  /**
   * Constructs a Naor-Pinkas OT instance using pre-specified Diffie-Hellman parameters.
   *
   * @param otherId The ID of the other party
   * @param random The calling party's secure randomness generator
   */
  public TinyTablesNaorPinkasOt(int otherId, Drbg random) {
    this.otherId = otherId;
    this.random = random;
  }

  @Override
  public void init(Network network) {
    ot = new BigIntNaorPinkas(otherId, random, network);
  }

  @Override
  public void send(StrictBitVector messageZero, StrictBitVector messageOne) {
    ot.send(messageZero, messageOne);
  }

  @Override
  public StrictBitVector receive(boolean choiceBit) {
    return ot.receive(choiceBit);
  }

}