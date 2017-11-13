package dk.alexandra.fresco.suite.tinytables.prepro.protocols;

import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.sce.resources.ResourcePoolImpl;

public abstract class TinyTablesPreproProtocol<OutputT>
    implements NativeProtocol<OutputT, ResourcePoolImpl> {

  protected int id;

  public int getId() {
    return id;
  }

}
