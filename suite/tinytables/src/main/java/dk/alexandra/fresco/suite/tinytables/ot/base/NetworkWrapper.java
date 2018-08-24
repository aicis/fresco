package dk.alexandra.fresco.suite.tinytables.ot.base;

import dk.alexandra.fresco.framework.network.CloseableNetwork;
import dk.alexandra.fresco.framework.util.ExceptionConverter;
import dk.alexandra.fresco.suite.tinytables.util.Util;
import edu.biu.scapi.comm.Channel;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * This class wraps an instance of {@link CloseableNetwork} (in the FRESCO sense) as an instance of
 * {@link Channel} (as in SCAPI).
 *
 */
public class NetworkWrapper implements Channel {

  private final CloseableNetwork network;
  private final int myId;
  private boolean closed;

  /**
   * Wraps a FRESCO {@link CloseableNetwork} to implement a SCAPI  {@link Channel}.
   * @param network a network, note this assumed to be not yet closed
   * @param myId the id of this party
   */
  public NetworkWrapper(CloseableNetwork network, int myId) {
    this.network =  network;
    this.myId = myId;
    this.closed = false;
  }

  @Override
  public void close() {
    closed = true;
    ExceptionConverter.safe(() -> {
      network.close();
      return null;
    }, "Unable to close network");

  }

  @Override
  public boolean isClosed() {
    return closed;
  }

  @Override
  public Serializable receive() throws ClassNotFoundException, IOException {
    byte[] data = network.receive(Util.otherPlayerId(myId));
    ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
    return (Serializable) ois.readObject();
  }

  @Override
  public void send(Serializable otInputs) throws IOException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(bos);
    oos.writeObject(otInputs);
    oos.flush();
    oos.close();
    byte[] toSend = bos.toByteArray();
    network.send(Util.otherPlayerId(myId), toSend);
  }

}
