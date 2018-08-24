package dk.alexandra.fresco.suite.tinytables.prepro.protocols;

import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.suite.tinytables.prepro.TinyTablesPreproResourcePool;

public abstract class TinyTablesPreproProtocol<OutputT>
    implements NativeProtocol<OutputT, TinyTablesPreproResourcePool> {

  protected int id;

  public int getId() {
    return id;
  }

}
