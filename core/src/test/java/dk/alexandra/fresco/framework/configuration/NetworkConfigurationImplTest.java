package dk.alexandra.fresco.framework.configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import dk.alexandra.fresco.framework.Party;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class NetworkConfigurationImplTest {
  
  private NetworkConfiguration netConf;
  private Party me;
  private final int myId = 3;
  private final int numParties = 4;

  /**
   * Set up a configuration with 4 parties.
   */
  @Before
  public void setUp() {
    Map<Integer, Party> parties = new HashMap<>(4);
    for (int i = 1; i < numParties + 1; i++) {
      Party p = new Party(i, "host" + i, 3000 + i);
      parties.put(i, p);
      this.me = (i == myId) ? p : me;
    }      
    this.netConf = new NetworkConfigurationImpl(myId, parties);
  }

  @Test
  public void testGetParty() {
    Party p2 = netConf.getParty(2);
    assertEquals("host2", p2.getHostname());
    assertEquals(2, p2.getPartyId());
    assertEquals(3002, p2.getPort());
    Party p10 = netConf.getParty(10);
    // TODO: below might not be good behavior
    assertEquals(null, p10); 
  }

  @Test
  public void testGetMyId() {
    assertEquals(me.getPartyId(), netConf.getMyId());
  }

  @Test
  public void testGetMe() {
    assertEquals(me.getPartyId(), netConf.getMe().getPartyId());
    assertEquals(me.getHostname(), netConf.getMe().getHostname());
    assertEquals(me.getPort(), netConf.getMe().getPort());
  }

  @Test
  public void testNoOfParties() {
    assertEquals(numParties, netConf.noOfParties());
  }

  @Test
  public void testToString() {
    String s = netConf.toString();
    assertTrue(s.startsWith("NetworkConfigurationImpl"));
    assertTrue(s.contains("myId=" + me.getPartyId()));
    for (int i = 1; i < netConf.noOfParties() + 1; i++) {
      assertTrue(s.contains(netConf.getParty(i).toString()));
    }
  }

}
