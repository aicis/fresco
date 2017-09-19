package dk.alexandra.fresco.framework;

import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Test;

public class TestParty {

  @Test
  public void testGetId() {
    Party party = new Party(1, "hostname", 8080, "myshared key");
    Assert.assertThat(party.getPartyId(), Is.is(1));
  }

  @Test
  public void testToString() {
    Party party = new Party(1, "hostname", 8080, "myshared key");
    Assert.assertThat(party.toString(), Is.isA(String.class));
    party = new Party(2, "hostname2", 8081);
    Assert.assertThat(party.toString(), Is.isA(String.class));
  }

}
