package dk.alexandra.fresco.framework;

import dk.alexandra.fresco.framework.util.ValidationUtils;
import java.util.Objects;

/**
 * FRESCO's view of a MPC party.
 */
public class Party {

  private final int id;
  private final int port;
  private final String host;

  /**
   * Creates a new Party.
   *
   * @param id the 1-based id of the party
   * @param host the host machine (ip or dns)
   * @param port the tcp port to connect on
   */
  public Party(int id, String host, int port) {
    ValidationUtils.assertValidId(id);

    this.id = id;
    this.host = Objects.requireNonNull(host);
    this.port = port;
  }

  public String getHostname() {
    return this.host;
  }

  public int getPort() {
    return this.port;
  }

  public int getPartyId() {
    return this.id;
  }

  @Override
  public String toString() {
    return "Party(" + this.id + ", " + host + ":" + port + ")";
  }

}
