package dk.alexandra.fresco.suite.tinytables.ot.base;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.suite.tinytables.util.Util;
import edu.biu.scapi.comm.Channel;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * This class wraps an instance of {@link Network} (in the FRESCO sense) as an
 * instance of {@link Channel} (as in SCAPI).
 *
 * @author Jonas Lindstrøm (jonas.lindstrom@alexandra.dk)
 */
public class NetworkWrapper implements Channel {

  private Network network;
  private int myId;

  public NetworkWrapper(Network network, int myId) {
    this.network = network;
    this.myId = myId;
  }

  @Override
  public void close() {

  }

  @Override
  public boolean isClosed() {
    return false;
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
