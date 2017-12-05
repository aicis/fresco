package dk.alexandra.fresco.framework.sce.resources;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import dk.alexandra.fresco.framework.util.HmacDrbg;
import java.security.NoSuchAlgorithmException;
import org.junit.Test;

public class TestResourcePoolImpl {

  @Test
  public void testResourcePoolImpl() throws NoSuchAlgorithmException {
    ResourcePoolImpl rp = new ResourcePoolImpl(0, 1, new HmacDrbg());
    assertThat(rp.getMyId(), is(0));
    assertThat(rp.getNoOfParties(), is(1));
    assertThat(rp.getRandomGenerator(), instanceOf(HmacDrbg.class));
  }

}
