package dk.alexandra.fresco.framework;

/**
 * FRESCO's view of a MPC party.
 */
public class Party {

  private final int id;
  private final int port;
  private final String host;
  //Secret shared key used to communicate with this party. Can be null
  private final String secretSharedKey;

  /**
   * Creates a new Party.
   *
   * @param id the 1-based id of the party
   * @param host the host machine (ip or dns)
   * @param port the tcp port to connect on
   */
  public Party(int id, String host, int port) {
    this(id, host, port, null);
  }

  /**
   * Creates a new Party.
   *
   * @param id the 1-based id of the party
   * @param host the host machine (ip or dns)
   * @param port the tcp port to connect on
   * @param secretSharedKey Base64 encoded aes key
   */
  public Party(int id, String host, int port, String secretSharedKey) {
    this.id = id;
    this.host = host;
    this.port = port;
    this.secretSharedKey = secretSharedKey;
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

  public String getSecretSharedKey() {
    return this.secretSharedKey;
  }

  @Override
  public String toString() {
    return "Party(" + this.id + ", " + host + ":" + port + ", ssKey: " + secretSharedKey + ")";
  }

}
