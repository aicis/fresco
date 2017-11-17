package dk.alexandra.fresco.framework.configuration;

import dk.alexandra.fresco.framework.Party;
import java.util.Map;

public class NetworkConfigurationImpl implements NetworkConfiguration {

  private final int myId;

  private final Map<Integer, Party> parties;

  public NetworkConfigurationImpl(int myId, Map<Integer, Party> parties) {
    this.myId = myId;
    this.parties = parties;
  }


  @Override
  public Party getParty(int id) {
    return parties.get(id);
  }

  @Override
  public int getMyId() {
    return myId;
  }

  @Override
  public Party getMe() {
    return getParty(getMyId());
  }

  @Override
  public int noOfParties() {
    return parties.size();
  }

  @Override
  public String toString() {
    return "NetworkConfigurationImpl [myId=" + myId + ", parties="
        + parties + "]";
  }


}
