package dk.alexandra.fresco.framework.sce.resources;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.security.SecureRandom;
import java.util.Random;
import org.junit.Test;

public class TestResourcePoolImpl {

  @Test
  public void testResourcePoolImpl() {
    ResourcePoolImpl rp = new ResourcePoolImpl(0, 1, new Random(), new SecureRandom());
    assertThat(rp.getMyId(), is(0));
    assertThat(rp.getNoOfParties(), is(1));
    assertThat(rp.getRandom(), instanceOf(Random.class));
    assertThat(rp.getSecureRandom(), instanceOf(SecureRandom.class));
  }

}
