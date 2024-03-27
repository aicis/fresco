package dk.alexandra.fresco.framework.configuration;

import dk.alexandra.fresco.framework.Party;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.util.ValidationUtils;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class NetworkConfigurationImpl implements NetworkConfiguration {

  private final int myId;

  private final Map<Integer, Party> parties;

  public NetworkConfigurationImpl(int myId, Map<Integer, Party> parties) {
    // Validation
    Objects.requireNonNull(parties);
    checkAddressesUnique(parties);
    ValidationUtils.assertValidId(myId);
    if (parties.get(myId) == null) {
      throw new RuntimeException(String.format("myId %d must be in the parties map: %s", myId, parties));
    }

    // Set fields
    this.myId = myId;
    this.parties = parties;
  }

  @Override
  public Party getParty(int id) {
    ValidationUtils.assertValidId(id);
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

  /**
   * Verifies that all party addresses are unique.
   */
  private static void checkAddressesUnique(Map<Integer, Party> parties) {
    Set<Pair<String, String>> addresses = new HashSet<>();
    for (Party party : parties.values()) {
      addresses.add(new Pair<>(party.getHostname(), Integer.toString(party.getPort())));
    }
    if (addresses.size() != parties.size()) {
      throw new IllegalArgumentException("Party addresses must be unique: " + parties);
    }
  }

}
