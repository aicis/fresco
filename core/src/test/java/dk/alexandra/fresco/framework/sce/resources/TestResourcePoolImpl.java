package dk.alexandra.fresco.framework.sce.resources;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class TestResourcePoolImpl {

  @Test
  public void testResourcePoolImpl() {
    ResourcePoolImpl rp = new ResourcePoolImpl(1, 1);
    assertThat(rp.getMyId(), is(1));
    assertThat(rp.getNoOfParties(), is(1));
  }

}
