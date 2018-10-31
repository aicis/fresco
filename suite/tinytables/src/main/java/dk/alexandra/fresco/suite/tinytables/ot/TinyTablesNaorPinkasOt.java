package dk.alexandra.fresco.suite.tinytables.ot;

import javax.crypto.spec.DHParameterSpec;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.ot.base.NaorPinkasOt;

public class TinyTablesNaorPinkasOt implements TinyTablesOt {

  private final int otherId;
  private final Drbg random;
  private final DHParameterSpec params;
  private NaorPinkasOt ot;

  /**
   * Constructs a Naor-Pinkas OT instance using prespecified Diffie-Hellman parameters.
   *
   * @param otherId The ID of the other party
   * @param randBit The calling party's secure randomness generator
   * @param params The Diffie-Hellman parameters to use
   */
  public TinyTablesNaorPinkasOt(int otherId, Drbg random, DHParameterSpec params) {
    this.otherId = otherId;
    this.random = random;
    this.params = params;
  }

  @Override
  public void init(Network network) {
    ot = new NaorPinkasOt(otherId, random, network, params);
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
