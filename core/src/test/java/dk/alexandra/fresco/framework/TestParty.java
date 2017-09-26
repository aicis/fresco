package dk.alexandra.fresco.framework;

import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Test;

public class TestParty {

  @Test
  public void testGetters() {
    String key = "mySharedKey";
    Party party = new Party(1, "hostname", 8080, key);
    party.setSecretSharedKey(key);
    Assert.assertThat(party.getSecretSharedKey(), Is.is(key));
    Assert.assertThat(party.getHostname(), Is.is("hostname"));
    Assert.assertThat(party.getPort(), Is.is(8080));
    Assert.assertThat(party.getPartyId(), Is.is(1));
    
  }
  
  @SuppressWarnings("deprecation")
  @Test
  public void testToString() {
    Party party = new Party(1, "hostname", 8080, "myshared key");
    Assert.assertThat(party.toString(), Is.is(String.class));
    party = new Party(2, "hostname2", 8081);
    Assert.assertThat(party.toString(), Is.is(String.class));
  }
  
}
