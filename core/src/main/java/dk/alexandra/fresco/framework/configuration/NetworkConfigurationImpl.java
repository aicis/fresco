package dk.alexandra.fresco.framework.configuration;

import dk.alexandra.fresco.framework.Party;
import java.util.HashMap;
import java.util.Map;

public class NetworkConfigurationImpl implements NetworkConfiguration {

	private int myId;

	private Map<Integer, Party> parties = new HashMap<Integer, Party>();

	public NetworkConfigurationImpl(int myId, Map<Integer, Party> parties) {
		super();
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
