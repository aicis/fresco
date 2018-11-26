package dk.alexandra.fresco.suite.tinytables.ot;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.ot.base.DummyOt;

/**
 * Adapter class for the generic dummy OT implementation for use . This adapter allows the user to
 * give a network supplier, rather than a direct network during construction.
 */
public class TinyTablesDummyOt implements TinyTablesOt {
  private final int otherId;
  private DummyOt ot;

  /**
   * Construct an insecure dummy OT object based on a real network.
   *
   * @param otherId
   *          The ID of the other party
   */
  public TinyTablesDummyOt(int otherId) {
    this.otherId = otherId;
  }

  @Override
  public void init(Network network) {
    ot = new DummyOt(otherId, network);
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
