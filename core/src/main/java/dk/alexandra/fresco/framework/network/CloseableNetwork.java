package dk.alexandra.fresco.framework.network;

import java.io.Closeable;

/**
 * Interface for a closable network.
 */
public interface CloseableNetwork extends Network, Closeable {

  /*
   * Override the close method to NOT throw IOExceptions (this may be kind of a hack)
   */
  @Override
  void close();

}
