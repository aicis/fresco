package dk.alexandra.fresco.suite.tinytables;

import java.util.function.Supplier;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.ot.base.DummyOt;
import dk.alexandra.fresco.tools.ot.base.Ot;

public class TinyTablesDummyOtAdapter implements Ot {
  private final int otherId;
  private final Supplier<Network> network;
  private DummyOt ot;

  public TinyTablesDummyOtAdapter(int otherId, Supplier<Network> network) {
    this.otherId = otherId;
    this.network = network;
  }

  @Override
  public void send(StrictBitVector messageZero, StrictBitVector messageOne) {
    if (ot == null) {
      ot = new DummyOt(otherId, network.get());
    }
    ot.send(messageZero, messageOne);
  }

  @Override
  public StrictBitVector receive(boolean choiceBit) {
    if (ot == null) {
      ot = new DummyOt(otherId, network.get());
    }
    return ot.receive(choiceBit);
  }

}
