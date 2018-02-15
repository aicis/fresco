package dk.alexandra.fresco.framework.sce.resources;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class TestResourcePoolImpl {

  @Test
  public void testResourcePoolImpl() {
    ResourcePoolImpl rp = new ResourcePoolImpl(0, 1);
    assertThat(rp.getMyId(), is(0));
    assertThat(rp.getNoOfParties(), is(1));
  }

}
